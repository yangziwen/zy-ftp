package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class CWD implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String path = StringUtils.defaultString(request.getArgument(), "/");

		if (!session.isReadAllowed(path)) {
			return Command.createResponse(FtpReply.REPLY_550, "CWD", session);
		}

		boolean success = session.getFileSystemView().changeCurrentDirectory(path);

		if (success) {
			FileView currentDirectory = session.getFileSystemView().getCurrentDirectory();
			return Command.createResponse(FtpReply.REPLY_250, "CWD", request, session, currentDirectory.getVirtualPath());
		} else {
			return Command.createResponse(FtpReply.REPLY_550, "CWD", session);
		}
	}

}
