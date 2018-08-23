package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class RNFR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return createResponse(FtpReply.REPLY_501, request);
		}
		String fileName = request.getArgument();
		FileView file = session.getFileSystemView().getFile(fileName);
		request.attr("fileName", fileName);
		if (!session.isWriteAllowed(file)) {
			return createResponse(FtpReply.REPLY_550, request);
		}
		return createResponse(FtpReply.REPLY_350, request);
	}


}
