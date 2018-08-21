package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.command.CommandFactory;
import io.github.yangziwen.zyftp.command.impl.state.CommandState;
import io.github.yangziwen.zyftp.command.impl.state.PortState;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * The ftp server handler
 *
 * @author yangziwen
 */
@Slf4j
public class FtpServerHandler extends SimpleChannelInboundHandler<FtpRequest> {

	private FtpServerContext serverContext;

	public FtpServerHandler(FtpServerContext serverContext) {
		this.serverContext = serverContext;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FtpRequest request) throws Exception {
		FtpSession session = FtpSession.getOrCreateSession(ctx, serverContext);
		CommandState prevState = session.getCommandState();
		prevState.transferTo(request, session);
		if (PortState.class.isInstance(prevState)) {
			session.getLatestPortDataClient().connect().addListener(f -> {
				processRequest(request, session);
			});
		} else {
			processRequest(request, session);
		}

	}

	private void processRequest(FtpRequest request, FtpSession session) {
		Command command = CommandFactory.getCommand(request.getCommand());
		if (command != null) {
			Promise<FtpResponse> promise = command.executeAsync(session, request);
			promise.addListener(f -> {
				if (promise.get() != null) {
					FtpResponse response = promise.get();
					response.setCommand(request.getCommand());
					sendResponse(response, session.getContext());
				}
			});
		} else {
			FtpResponse response = Command.createResponse(FtpReply.REPLY_502, "not.implemented", request, session);
			response.setCommand(request.getCommand());
			sendResponse(response, session.getContext());
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		FtpSession session = FtpSession.getOrCreateSession(ctx, serverContext);
		FtpResponse response = Command.createResponse(FtpReply.REPLY_220, session);
		sendResponse(response, ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		FtpSession session = FtpSession.getOrCreateSession(ctx, serverContext);
		log.info("session[{}] lost connection", session);
		if (session.isLoggedIn()) {
			session.logout();
		}
		session.getChannel().close().addListener(closeFuture -> {
			session.destroy();
		});
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (FtpSession.isAllIdleStateEvent(evt)) {
			FtpSession session = FtpSession.getOrCreateSession(ctx, serverContext);
			if (session.hasRunningDataConnection()) {
				return;
			}
			log.info("idle connection of session[{}] is timeout", session);
			if (session.isLoggedIn()) {
				session.logout();
			}
			session.getChannel().close().addListener(closeFuture -> {
				session.destroy();
			});
		}
	}

	public static ChannelFuture sendResponse(FtpResponse response, ChannelHandlerContext ctx) {
		return ctx.writeAndFlush(response).addListener(f -> response.notifyFlushed());
	}

}
