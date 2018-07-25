package io.github.yangziwen.zyftp.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FtpServerHandler extends SimpleChannelInboundHandler<FtpRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FtpRequest request) throws Exception {
		// TODO
	}
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	FtpSession.getOrCreateSession(ctx);
    	// TODO reply 220
    }

}
