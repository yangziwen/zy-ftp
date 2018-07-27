package io.github.yangziwen.zyftp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public interface FtpDataWriter {

	ChannelFuture writeAndFlushData(Channel ctx);

}
