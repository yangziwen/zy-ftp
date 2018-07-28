package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class RNFR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "RNFR", session);
		}
		String fileName = request.getArgument();
		FileView file = session.getFileSystemView().getFile(fileName);
		if (file == null) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RNFR", request, session, fileName);
		}
		return Command.createResponse(FtpResponse.REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION, "RNFR", request, session, fileName);
	}


}
