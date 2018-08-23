package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.channel.ChannelPromise;

public class QUIT implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		FtpResponse response = createResponse(FtpReply.REPLY_221, request);
		ChannelPromise flushedPromise = session.newChannelPromise();
		flushedPromise.addListener(future -> {
			session.getChannel().close().addListener(closeFuture -> {
				session.destroy();
			});
		});
		response.setFlushedPromise(flushedPromise);
		return response;
	}

}
