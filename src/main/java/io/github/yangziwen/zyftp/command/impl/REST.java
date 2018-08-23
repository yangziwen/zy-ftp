package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class REST implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return createResponse(FtpReply.REPLY_501, request);
		}
		long skipLen = 0L;
		try {
			skipLen = Long.parseLong(request.getArgument());
		} catch (NumberFormatException ex) {
			return Command.createResponse(FtpReply.REPLY_501, nameWithSuffix("invalid"), request);
		}
		if (skipLen < 0L) {
			return Command.createResponse(FtpReply.REPLY_501, nameWithSuffix("negetive"), request);
		}
		return createResponse(FtpReply.REPLY_350, request);
	}

}
