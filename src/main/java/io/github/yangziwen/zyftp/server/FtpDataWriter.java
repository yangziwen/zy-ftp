package io.github.yangziwen.zyftp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * The data writer
 * 
 * @author yangziwen
 */
public interface FtpDataWriter {

	ChannelFuture writeAndFlushData(Channel ctx);

}
