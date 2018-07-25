package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.user.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class FtpSession {
	
	private static final AttributeKey<FtpSession> SESSION_KEY = AttributeKey.valueOf("ftp.session");
	
	private ChannelHandlerContext context;
	
	private FtpServerContext serverContext;
	
	private User user;
	
	public FtpSession(ChannelHandlerContext context, FtpServerContext serverContext) {
		this.context = context;
		this.serverContext = serverContext;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public ChannelHandlerContext getContext() {
		return context;
	}
	
	public FtpServerContext getServerContext() {
		return serverContext;
	}
	
	public Channel getChannel() {
		return context.channel();
	}
	
	public static FtpSession getOrCreateSession(ChannelHandlerContext ctx, FtpServerContext serverContext) {
		Channel channel = ctx.channel();
		if (!channel.hasAttr(SESSION_KEY)) {
			channel.attr(SESSION_KEY).setIfAbsent(new FtpSession(ctx, serverContext));
		}
		return channel.attr(SESSION_KEY).get();
	}
	
}
