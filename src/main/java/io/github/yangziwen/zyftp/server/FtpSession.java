package io.github.yangziwen.zyftp.server;

import io.netty.channel.ChannelHandlerContext;

public class FtpSession {
	
	private ChannelHandlerContext context;
	
	public FtpSession(ChannelHandlerContext context) {
		this.context = context;
	}
	
}
