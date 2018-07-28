package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class RNTO implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "RNTO", session);
		}
		FtpRequest prevRequest = session.getCommandState().getRequest("RNFR");
		if (prevRequest == null || !prevRequest.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "RNTO", session);
		}
		FileView fromFile = session.getFileSystemView().getFile(prevRequest.getArgument());
		FileView toFile = session.getFileSystemView().getFile(request.getArgument());
		if (fromFile == null || toFile == null) {
			return Command.createResponse(FtpResponse.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO.invalid", session);
		}
		if (!fromFile.doesExist()) {
			return Command.createResponse(FtpResponse.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO.missing", session);
		}
		if (!fromFile.moveTo(toFile)) {
			return Command.createResponse(FtpResponse.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED, "RNTO", session);
		}
		return Command.createResponse(FtpResponse.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "RNTO", session);
	}

}
