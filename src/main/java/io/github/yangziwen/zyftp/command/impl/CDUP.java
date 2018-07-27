package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class CDUP implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		boolean success = session.getFileSystemView().changeCurrentDirectory("..");

		if (success) {
			FileView currentDirectory = session.getFileSystemView().getCurrentDirectory();
			return Command.createResponse(FtpResponse.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "CDUP", request, session, currentDirectory.getVirtualPath());
		} else {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "CDUP", session);
		}
	}

}
