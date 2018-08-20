package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class RNFR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "RNFR", session);
		}
		String fileName = request.getArgument();
		FileView file = session.getFileSystemView().getFile(fileName);
		if (!session.isWriteAllowed(file)) {
			return Command.createResponse(FtpReply.REPLY_550, "RNFR", request, session, fileName);
		}
		return Command.createResponse(FtpReply.REPLY_350, "RNFR", request, session, fileName);
	}


}
