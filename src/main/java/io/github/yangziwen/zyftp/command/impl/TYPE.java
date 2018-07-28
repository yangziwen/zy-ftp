package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.DataType;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class TYPE implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "TYPE", session);
		}
		try {
			session.setDataType(DataType.from(request.getArgument().substring(0, 1)));
			return Command.createResponse(FtpReply.REPLY_200, "TYPE", session);
		} catch (Exception e) {
			return Command.createResponse(FtpReply.REPLY_504, "TYPE", request, session);
		}
	}

}
