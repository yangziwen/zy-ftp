package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class PROT implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "PROT", session);
		}
		String procType = request.getArgument();
		if ("C".equals(procType)) {
			session.setDataConnectionSecured(false);
			return Command.createResponse(FtpReply.REPLY_200, "PROT", session);
		} else if ("P".equals(procType)) {
			session.setDataConnectionSecured(true);
			return Command.createResponse(FtpReply.REPLY_200, "PROT", session);
		}
		return Command.createResponse(FtpReply.REPLY_504, "PROT", session);
	}

}
