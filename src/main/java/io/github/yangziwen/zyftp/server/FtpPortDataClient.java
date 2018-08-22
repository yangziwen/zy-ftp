package io.github.yangziwen.zyftp.server;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpPortDataClient implements FtpDataConnection {

	private FtpSession session;

	private InetSocketAddress address;

	private Bootstrap bootstrap;

	private ChannelFuture channelFuture;

	private Promise<Void> connectedPromise;

	private AtomicBoolean connected = new AtomicBoolean(false);

	private AtomicReference<UploadFileInfo> uploadFileInfoRef = new AtomicReference<>();

	public FtpPortDataClient(FtpSession session, InetSocketAddress address) {
		this.session = session;
		this.address = address;
		this.bootstrap = new Bootstrap();
		connectedPromise = session.newPromise();
	}

	public ChannelFuture connect() {
		EventLoopGroup eventLoopGroup = session.getChannel().eventLoop();
		this.channelFuture = this.bootstrap.group(eventLoopGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.handler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel channel) throws Exception {
						if (session.isDataConnectionSecured()) {
							channel.pipeline().addFirst(session.createClientSslContext().newHandler(channel.alloc(), address.getHostString(), address.getPort()));
						}
						channel.pipeline()
							.addLast(new ChannelTrafficShapingHandler(session.getDownloadBytesPerSecond(), session.getUploadBytesPerSecond(), 500))
							.addLast(new IdleStateHandler(0, 0, session.getServerConfig().getDataConnectionMaxIdleSeconds(), TimeUnit.SECONDS))
							.addLast(new ChunkedWriteHandler())
							.addLast(new PortDataClientHandler());
						if (session.isDataConnectionSecured()) {
							channel.pipeline().get(SslHandler.class).handshakeFuture().addListener(f -> {
								if (f.isSuccess()) {
									connectedPromise.setSuccess(null);
								} else {
									connectedPromise.setFailure(f.cause());
								}
							});
						} else {
							connectedPromise.setSuccess(null);
						}
					}
				})
				.connect(address).addListener(f -> {
					connected.compareAndSet(false, true);
					log.info("port data client[{}] of session[{}] is connected", this, session);
				});
		return this.channelFuture;
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
			else if (!connected.get()) {
				promise.setFailure(new IllegalStateException("there is no connection to the remote client"));
			}
			else {
				writer.writeAndFlushData(channelFuture.channel()).addListener(f2 -> {
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
		Promise<Void> promise = session.newPromise();
		if (!connected.compareAndSet(true, false)) {
			return promise.setFailure(new RuntimeException("data client does not connect"));
		}
		if (uploadFileInfoRef.get() != null && uploadFileInfoRef.get().isValid()) {
			uploadFileInfoRef.get().close();
		}
		channelFuture.channel().close().addListener(f -> {
			log.info("port data client[{}] of session[{}] is closed", this, session);
			session.removePortDataClient(this);
			promise.setSuccess(null);
		});
		return promise;
	}

	public boolean isConnected() {
		return connected.get();
	}

	@Override
	public FtpSession getSession() {
		return session;
	}

	@Override
	public ChannelFuture getCloseFuture() {
		return channelFuture.channel().closeFuture();
	}

	@Override
	public String toString() {
		String[] array = super.toString().split("\\.");
		return array[array.length - 1];
	}

	class PortDataClientHandler extends ChannelDuplexHandler {

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
				FtpPortDataClient.this.close();
			}
		}


		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (FtpSession.isAllIdleStateEvent(evt)) {
				ctx.channel().close().addListener(f -> {
					FtpPortDataClient.this.close();
				});
			}
		}

		@Override
		public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			promise.addListener(f -> {
				FtpPortDataClient.this.close();
			});
		}


	}

}
