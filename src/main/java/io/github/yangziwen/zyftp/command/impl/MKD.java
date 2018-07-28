package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class MKD implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "MKD", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD.invalid", request, session, request.getArgument());
		}
		if (!file.isWritable()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD.permission", request, session, file.getVirtualPath());
		}
		if (file.doesExist()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD.exists", request, session, file.getVirtualPath());
		}
		if (!file.mkdir()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD", request, session, file.getVirtualPath());
		}
		return Command.createResponse(FtpResponse.REPLY_257_PATHNAME_CREATED, "MKD", request, session, file.getVirtualPath());
	}

}
