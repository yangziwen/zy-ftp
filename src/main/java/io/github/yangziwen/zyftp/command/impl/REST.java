package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class REST implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "REST", session);
		}
		long skipLen = 0L;
		try {
			skipLen = Long.parseLong(request.getArgument());
		} catch (NumberFormatException ex) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "REST.invalid", session);
		}
		if (skipLen < 0L) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "REST.negetive", session);
		}
		return Command.createResponse(FtpResponse.REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION, "REST", session);
	}

}
