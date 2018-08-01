package io.github.yangziwen.zyftp.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.github.yangziwen.zyftp.command.impl.state.CommandState;
import io.github.yangziwen.zyftp.command.impl.state.StorState;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Promise;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The data server of the passive mode
 *
 * @author yangziwen
 */
@Slf4j
public class FtpPassiveDataServer implements FtpDataConnection {

	private FtpSession session;

	private ServerBootstrap serverBootstrap;

	private ChannelFuture serverChannelFuture;

	private KeySetView<Channel, Boolean> clientChannels = ConcurrentHashMap.newKeySet();

	private Promise<Void> connectedPromise;

	private AtomicBoolean running = new AtomicBoolean(false);

	private AtomicReference<UploadFileInfo> uploadFileInfoRef = new AtomicReference<>();

	public FtpPassiveDataServer(FtpSession session) {
		this.session = session;
		this.serverBootstrap = new ServerBootstrap();
		session.addPassiveDataServer(this);
		connectedPromise = session.getContext().newPromise();
	}

	public Channel getServerChannel() {
		if (serverChannelFuture == null) {
			return null;
		}
		return serverChannelFuture.channel();
	}

	public ChannelFuture start(Integer port) throws Exception {
		EventLoopGroup eventLoopGroup = session.getChannel().eventLoop();
		this.serverChannelFuture = this.serverBootstrap.group(eventLoopGroup, eventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
	        .option(ChannelOption.SO_REUSEADDR, true)
	        .childOption(ChannelOption.TCP_NODELAY, true)
	        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
	        .childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					if (clientChannels.add(channel)) {
						connectedPromise.setSuccess(null);
					}
					channel.pipeline()
						.addLast(new IdleStateHandler(0, 0, session.getServerConfig().getDataConnectionMaxIdleSeconds()))
						.addLast(new PassiveDataServerHandler());
				}
			}).bind(port).addListener(f -> {
				running.compareAndSet(false, true);
				session.getChannel().eventLoop().schedule(() -> {
					shutdown();
				}, session.getServerConfig().getDataConnectionMaxIdleSeconds(), TimeUnit.SECONDS);
			});
		;
		return this.serverChannelFuture;
	}

	@Override
	public Promise<FtpDataConnection> writeAndFlushData(FtpDataWriter writer) {
		Promise<FtpDataConnection> promise = session.getChannel().eventLoop().newPromise();
		if (writer == null) {
			return promise.setFailure(new NullPointerException("writer cannot be null"));
		}
		connectedPromise.addListener(f1 -> {
			if (!f1.isSuccess()) {
				promise.setFailure(f1.cause());
			}
			else if (CollectionUtils.isEmpty(clientChannels)) {
				promise.setFailure(new IllegalStateException("there is no available channel connected to passive server[" + this + "]"));
			}
			else {
				writer.writeAndFlushData(clientChannels.iterator().next()).addListener(f2 -> {
					if (!f2.isSuccess()) {
						promise.setFailure(f2.cause());
					}
					promise.setSuccess(this);
				});
			}
		});
		return promise;
	}

	public Promise<Void> shutdown() {
		Promise<Void> promise = session.getContext().newPromise();
		if (!running.compareAndSet(true, false)) {
			promise.setFailure(new RuntimeException("data server is not running"));
		} else {
			if (uploadFileInfoRef.get() != null && uploadFileInfoRef.get().isValid()) {
				uploadFileInfoRef.get().close();
			}
			closeClientChannels(clientChannels).addListener(f1 -> {
				serverChannelFuture.channel().close().addListener(f2 -> {
					log.info("passive data server of session{} is shut down", session);
					session.removePassiveDataServer(this);
					promise.setSuccess(null);
				});
			});
		}
		return promise;
	}

	public boolean isRunning() {
		return running.get();
	}

	private Promise<Void> closeClientChannels(KeySetView<Channel, Boolean> clientChannels) {
		Promise<Void> promise = session.getContext().newPromise();
		AtomicInteger counter = new AtomicInteger(clientChannels.size());
		clientChannels.forEach(channel -> {
			channel.close().addListener(future -> {
				if (counter.decrementAndGet() == 0) {
					promise.setSuccess(null);
				}
			});
		});
		return promise;
	}

	public ChannelFuture getCloseFuture() {
		return serverChannelFuture.channel().closeFuture();
	}

	@Data
	class UploadFileInfo {

		private long offset;

		private RandomAccessFile file;

		private long receivedTotalBytes;

		public UploadFileInfo() {
			this.offset = -1;
			this.file = null;
			CommandState state = session.getCommandState();
			if (!StorState.class.isInstance(state)) {
				return;
			}
			FtpRequest restRequest = state.getRequest("REST");
			this.offset = restRequest == null ? 0 : NumberUtils.toLong(restRequest.getArgument());
			this.file = getUploadFile(session, state);
		}

		public boolean isValid() {
			return offset >= 0 && file != null;
		}

		public long getAndAddOffset(long delta) {
			long offset = this.offset;
			this.offset += delta;
			return offset;
		}

		private RandomAccessFile getUploadFile(FtpSession session, CommandState commandState) {
	    	String fileName = commandState.getRequest("STOR").getArgument();
	    	FileView fileView = session.getFileSystemView().getFile(fileName);
	    	if (fileView == null) {
	    		return null;
	    	}
	    	try {
				return new RandomAccessFile(fileView.getRealFile(), "rw");
			} catch (Exception e) {
				return null;
			}
	    }

		public FileChannel getFileChannel() {
			return file != null ? file.getChannel() : null;
		}

		public void close() {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	class PassiveDataServerHandler extends ChannelDuplexHandler {

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    	if (uploadFileInfoRef.get() == null) {
	    		uploadFileInfoRef.compareAndSet(null, new UploadFileInfo());
	    	}
	    	UploadFileInfo uploadFileInfo = uploadFileInfoRef.get();
	    	if (!uploadFileInfo.isValid()) {
	    		return;
	    	}
	        ByteBuf buffer = (ByteBuf) msg;
	        int length = 0;
	        while ((length = buffer.readableBytes()) > 0) {
	        	buffer.readBytes(uploadFileInfo.getFileChannel(), uploadFileInfo.getAndAddOffset(length), length);
	        }
	    }

	    @Override
	    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	    	UploadFileInfo uploadFileInfo = uploadFileInfoRef.get();
	    	if (uploadFileInfo == null) {
	    		return;
	    	}
	    	if (uploadFileInfo.getOffset() > uploadFileInfo.getReceivedTotalBytes()) {
	    		uploadFileInfo.setReceivedTotalBytes(uploadFileInfo.getOffset());
	    	} else {
		    	shutdown();
	    	}
	    }

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (FtpSession.isAllIdleStateEvent(evt)) {
				ChannelFuture future = ctx.channel().close();
				clientChannels.remove(future.channel());
				if (!clientChannels.isEmpty()) {
					return;
				}
				future.addListener(f1 -> {
					shutdown();
				});
			}
	    }

		@Override
	    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
	        promise.addListener(f -> {
		    	shutdown();
	        });
	    }

	}

}
