package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class MKD implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "MKD", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpReply.REPLY_550, "MKD.invalid", request, session, request.getArgument());
		}
		if (!file.isWritable()) {
			return Command.createResponse(FtpReply.REPLY_550, "MKD.permission", request, session, file.getVirtualPath());
		}
		if (file.doesExist()) {
			return Command.createResponse(FtpReply.REPLY_550, "MKD.exists", request, session, file.getVirtualPath());
		}
		if (!file.mkdir()) {
			return Command.createResponse(FtpReply.REPLY_550, "MKD", request, session, file.getVirtualPath());
		}
		return Command.createResponse(FtpReply.REPLY_257, "MKD", request, session, file.getVirtualPath());
	}

}
