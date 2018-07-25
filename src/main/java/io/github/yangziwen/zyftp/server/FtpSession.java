package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.user.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class FtpSession {
	
	private static final AttributeKey<FtpSession> SESSION_KEY = AttributeKey.valueOf("ftp.session");
	
	private ChannelHandlerContext context;
	
	private User user;
	
	public FtpSession(ChannelHandlerContext context) {
		this.context = context;
	}
	
	public static FtpSession getOrCreateSession(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		if (!channel.hasAttr(SESSION_KEY)) {
			channel.attr(SESSION_KEY).setIfAbsent(new FtpSession(ctx));
		}
		return channel.attr(SESSION_KEY).get();
	}
	
}
