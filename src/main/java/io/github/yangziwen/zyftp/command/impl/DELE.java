package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class DELE implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "DELE", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null || !file.isFile()) {
			return Command.createResponse(FtpReply.REPLY_550, "DELE.invalid", request, session, request.getArgument());
		}
		if (!file.delete()) {
			return Command.createResponse(FtpReply.REPLY_450, "DELE", request, session, file.getVirtualPath());
		}
		return Command.createResponse(FtpReply.REPLY_250, "DELE", request, session, file.getVirtualPath());
	}

}
