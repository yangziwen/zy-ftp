package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

public class AUTH implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "AUTH", session);
		}
		if (session.getChannel().pipeline().get(SslHandler.class) != null) {
			return Command.createResponse(FtpReply.REPLY_534, "AUTH", session);
		}
		String authType = request.getArgument().toUpperCase();
		if ("TLS-C".equals(authType)) {
			authType = "TLS";
		} else if ("TLS-P".equals(authType)) {
			authType = "SSL";
		}
		SslContext sslContext = session.createServerSslContext();
		if (sslContext == null) {
			return Command.createResponse(FtpReply.REPLY_431, "AUTH", session);
		}
		return Command.createResponse(FtpReply.REPLY_234, "AUTH." + authType, session)
				.flushedPromise(session.newChannelPromise().addListener(f -> {
					session.getChannel().pipeline().addFirst(sslContext.newHandler(session.getChannel().alloc()));
				}));
	}

}
