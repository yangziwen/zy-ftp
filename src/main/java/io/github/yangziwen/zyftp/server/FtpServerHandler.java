package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.command.CommandFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

public class FtpServerHandler extends SimpleChannelInboundHandler<FtpRequest> {

	private FtpServerContext serverContext;

	public FtpServerHandler(FtpServerContext serverContext) {
		this.serverContext = serverContext;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FtpRequest request) throws Exception {
		FtpSession session = FtpSession.getOrCreateSession(ctx, serverContext);
		Command command = CommandFactory.getCommand(request.getCommand());
		FtpResponse response = null;
		if (command != null) {
			Promise<FtpResponse> promise = command.executeAsync(session, request);
			promise.addListener(f -> {
				sendResponse(promise.get(), ctx);
			});
		} else {
			response = Command.createResponse(FtpResponse.REPLY_502_COMMAND_NOT_IMPLEMENTED, "not.implemented", request, session);
			sendResponse(response, ctx);
		}
	}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	FtpSession session = FtpSession.getOrCreateSession(ctx, serverContext);
    	FtpResponse response = Command.createResponse(FtpResponse.REPLY_220_SERVICE_READY, session);
    	sendResponse(response, ctx);
    }

    private void sendResponse(FtpResponse response, ChannelHandlerContext ctx) {
    	ctx.writeAndFlush(response).addListener(future -> {
    		if (response.getFlushedPromise() != null) {
    			response.getFlushedPromise().setSuccess();
    		}
    	});
    }

}
