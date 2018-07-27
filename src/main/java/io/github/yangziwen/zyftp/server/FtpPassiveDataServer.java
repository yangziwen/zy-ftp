package io.github.yangziwen.zyftp.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Promise;

public class FtpPassiveDataServer {

	private FtpSession session;

	private ServerBootstrap serverBootstrap;

	private ChannelFuture serverChannelFuture;

	private KeySetView<Channel, Boolean> clientChannels = ConcurrentHashMap.newKeySet();

	private AtomicBoolean running = new AtomicBoolean(false);

	public FtpPassiveDataServer(FtpSession session) {
		this.session = session;
		this.serverBootstrap = new ServerBootstrap();
		session.setPassiveDataServer(this);
	}

	public Channel getServerChannel() {
		if (serverChannelFuture == null) {
			return null;
		}
		return serverChannelFuture.channel();
	}

	public ChannelFuture start(Integer port) throws Exception {
		EventLoopGroup bossEventLoopGroup = session.getBossEventLoopGroup();
		EventLoopGroup workerEventLoopGroup = session.getWorkerEventLoopGroup();
		this.serverChannelFuture = this.serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
	        .option(ChannelOption.SO_REUSEADDR, true)
	        .childOption(ChannelOption.TCP_NODELAY, true)
	        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
	        .childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					channel.pipeline()
						.addLast(new ChannelInboundHandlerAdapter());
					clientChannels.add(channel);
				}
			}).bind(port).addListener(f -> {
				running.compareAndSet(false, true);
			});
		;
		return this.serverChannelFuture;
	}

	public Promise<Boolean> writeAndFlushData(FtpDataWriter writer) {
		Promise<Boolean> promise = session.getWorkerEventLoopGroup().next().newPromise();
		if (writer == null || CollectionUtils.isEmpty(clientChannels)) {
			shutdown().addListener(f -> promise.setSuccess(false));
		} else {
			writer.writeAndFlushData(clientChannels.iterator().next()).addListener(f -> {
				promise.setSuccess(true);
			});
		}
		return promise;
	}

	public Promise<Void> shutdown() {
		Promise<Void> promise = session.getBossEventLoopGroup().next().newPromise();
		if (!running.compareAndSet(true, false)) {
			promise.setFailure(new RuntimeException("data server is not running"));
		} else {
			closeClientChannels(clientChannels).addListener(f1 -> {
				serverChannelFuture.channel().close().addListener(f2 -> {
					promise.setSuccess(null);
				});
			});
		}
		return promise;
	}

	public boolean isRunnning() {
		return running.get();
	}

	private Promise<Void> closeClientChannels(KeySetView<Channel, Boolean> clientChannels) {
		Promise<Void> promise = session.getWorkerEventLoopGroup().next().newPromise();
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

}
