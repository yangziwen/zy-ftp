package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
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
			return createResponse(FtpReply.REPLY_250, request.attr("newPath", currentDirectory.getVirtualPath()));
		} else {
			return createResponse(FtpReply.REPLY_550, request);
		}
	}

}
