package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class CWD implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String path = StringUtils.defaultString(request.getArgument(), "/");

		boolean success = session.getFileSystemView().changeCurrentDirectory(path);

		if (success) {
			FileView currentDirectory = session.getFileSystemView().getCurrentDirectory();
			return Command.createResponse(FtpResponse.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "CWD", request, session, currentDirectory.getVirtualPath());
		} else {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "CWD", session);
		}
	}

}
