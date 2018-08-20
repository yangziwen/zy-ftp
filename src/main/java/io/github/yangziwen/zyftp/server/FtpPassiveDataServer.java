package io.github.yangziwen.zyftp.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;

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
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.concurrent.Promise;
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

	// if any client connect to the passive data server, this promise will be set as success
	private Promise<Void> connectedPromise;

	private AtomicBoolean running = new AtomicBoolean(false);

	private AtomicReference<UploadFileInfo> uploadFileInfoRef = new AtomicReference<>();

	public FtpPassiveDataServer(FtpSession session) {
		this.session = session;
		this.serverBootstrap = new ServerBootstrap();
		connectedPromise = session.newPromise();
	}

	@Override
	public FtpSession getSession() {
		return session;
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
					if (session.isDataConnectionSecured()) {
						channel.pipeline().addFirst(session.createServerSslContext().newHandler(channel.alloc()));
					}
					channel.pipeline()
						.addLast(new ChannelTrafficShapingHandler(session.getDownloadBytesPerSecond(), session.getUploadBytesPerSecond(), 500))
						.addLast(new IdleStateHandler(0, 0, session.getServerConfig().getDataConnectionMaxIdleSeconds()))
						.addLast(new ChunkedWriteHandler())
						.addLast(new PassiveDataServerHandler());
					if (clientChannels.add(channel)) {
						connectedPromise.setSuccess(null);
					}
				}
			}).bind(port).addListener(f -> {
				running.compareAndSet(false, true);
				// the passive server will be stopped if any channel disconnect from the server
				// so we only need to consider the case that no client connect to the server
				session.getChannel().eventLoop().schedule(() -> {
					if (CollectionUtils.isEmpty(clientChannels)) {
						close();
					}
				}, session.getServerConfig().getDataConnectionMaxIdleSeconds(), TimeUnit.SECONDS);
				log.info("passive data server[{}] of session[{}] is started and listening port[{}]", this, session, port);
			});
		;
		return this.serverChannelFuture;
	}

	@Override
	public Promise<Void> writeAndFlushData(FtpDataWriter writer) {
		Promise<Void> promise = session.newPromise();
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
					} else {
						promise.setSuccess(null);
					}
				});
			}
		});
		return promise;
	}

	@Override
	public Promise<Void> close() {
		if (session.isDataConnectionSecured()) {
			Promise<Void> promise = session.newPromise();
			session.getChannel().eventLoop().schedule(() -> {
				doClose(promise);
			}, 0L, TimeUnit.MILLISECONDS);
			return promise;
		} else {
			return doClose(session.newPromise());
		}
	}

	private Promise<Void> doClose(Promise<Void> promise) {
		if (!running.compareAndSet(true, false)) {
			return promise.setFailure(new RuntimeException("data server is not running"));
		}
		if (uploadFileInfoRef.get() != null && uploadFileInfoRef.get().isValid()) {
			uploadFileInfoRef.get().close();
		}
		closeClientChannels(clientChannels).addListener(f1 -> {
			serverChannelFuture.channel().close().addListener(f2 -> {
				log.info("passive data server[{}] of session[{}] is stopped", this, session);
				session.removePassiveDataServer(this);
				promise.setSuccess(null);
			});
		});
		return promise;
	}

	public boolean isRunning() {
		return running.get();
	}

	private Promise<Void> closeClientChannels(KeySetView<Channel, Boolean> clientChannels) {
		Promise<Void> promise = session.newPromise();
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

	@Override
	public ChannelFuture getCloseFuture() {
		return serverChannelFuture.channel().closeFuture();
	}

	@Override
	public String toString() {
		String[] array = super.toString().split("\\.");
		return array[array.length - 1];
	}

	class PassiveDataServerHandler extends ChannelDuplexHandler {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (uploadFileInfoRef.get() == null) {
				uploadFileInfoRef.compareAndSet(null, new UploadFileInfo(session));
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
				FtpPassiveDataServer.this.close();
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
					FtpPassiveDataServer.this.close();
				});
			}
		}

		@Override
		public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			promise.addListener(f -> {
				FtpPassiveDataServer.this.close();
			});
		}

	}

}
