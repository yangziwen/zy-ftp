package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class PWD implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		FileView file = session.getFileSystemView().getCurrentDirectory();
		return Command.createResponse(FtpReply.REPLY_257, "PWD", request, session, file.getVirtualPath());
	}

}
