package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class SIZE implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "SIZE", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null || !file.doesExist()) {
			return Command.createResponse(FtpReply.REPLY_550, "SIZE.missing", session);
		}
		if (!file.isFile()) {
			return Command.createResponse(FtpReply.REPLY_550, "SIZE.invalid", session);
		}
		return Command.createResponse(FtpReply.REPLY_213, "SIZE", request, session, String.valueOf(file.getSize()));
	}

}
