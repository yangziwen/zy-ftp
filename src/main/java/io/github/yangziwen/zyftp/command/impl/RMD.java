package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class RMD implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "RMD", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpReply.REPLY_550, "RMD.permission", request, session, request.getArgument());
		}
		if (!file.isDirectory()) {
			return Command.createResponse(FtpReply.REPLY_550, "RMD.invalid", request, session, file.getVirtualPath());
		}
		if (session.getFileSystemView().getCurrentDirectory().hasParent(file)) {
			return Command.createResponse(FtpReply.REPLY_450, "RMD.busy", request, session, file.getVirtualPath());
		}
		if (!file.delete()) {
			return Command.createResponse(FtpReply.REPLY_450, "RMD", request, session, file.getVirtualPath());
		}
		return Command.createResponse(FtpReply.REPLY_250, "RMD", request, session, file.getVirtualPath());
	}

}
