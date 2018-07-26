package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.DataType;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class TYPE implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "TYPE", session);
		}
		try {
			session.setDataType(DataType.from(request.getArgument().substring(0, 1)));
			return Command.createResponse(FtpResponse.REPLY_200_COMMAND_OKAY, "TYPE", session);
		} catch (Exception e) {
			return Command.createResponse(FtpResponse.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER, "TYPE", request, session);
		}
	}

}
