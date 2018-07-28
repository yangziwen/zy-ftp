package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class RNTO implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "RNTO", session);
		}
		FtpRequest prevRequest = session.getCommandState().getRequest("RNFR");
		if (prevRequest == null || !prevRequest.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_503, "RNTO", session);
		}
		FileView fromFile = session.getFileSystemView().getFile(prevRequest.getArgument());
		FileView toFile = session.getFileSystemView().getFile(request.getArgument());
		if (fromFile == null || toFile == null) {
			return Command.createResponse(FtpReply.REPLY_553, "RNTO.invalid", session);
		}
		if (!fromFile.doesExist()) {
			return Command.createResponse(FtpReply.REPLY_553, "RNTO.missing", session);
		}
		if (!fromFile.moveTo(toFile)) {
			return Command.createResponse(FtpReply.REPLY_553, "RNTO", session);
		}
		return Command.createResponse(FtpReply.REPLY_250, "RNTO", session);
	}

}
