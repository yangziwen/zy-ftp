package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.channel.ChannelPromise;

public class QUIT implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		FtpResponse response = Command.createResponse(FtpResponse.REPLY_221_CLOSING_CONTROL_CONNECTION, "QUIT", session);
		ChannelPromise flushedPromise = session.getContext().newPromise();
		flushedPromise.addListener(future -> {
			if (session.isLoggedIn()) {
				session.logout();
			}
			session.getChannel().close().addListener(closeFuture -> {
				// TODO close data connection
			});
		});
		response.setFlushedPromise(flushedPromise);
		return response;
	}

}
