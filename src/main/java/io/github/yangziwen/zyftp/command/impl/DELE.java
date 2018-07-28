package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class DELE implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "DELE", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null || !file.isFile()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "DELE.invalid", request, session, request.getArgument());
		}
		if (!file.delete()) {
			return Command.createResponse(FtpResponse.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, "DELE", request, session, file.getVirtualPath());
		}
		return Command.createResponse(FtpResponse.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "DELE", request, session, file.getVirtualPath());
	}

}
