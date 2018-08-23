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
			return createResponse(FtpReply.REPLY_501, request);
		}
		FtpRequest prevRequest = session.getCommandState().getRequest("RNFR");
		if (prevRequest == null || !prevRequest.hasArgument()) {
			return createResponse(FtpReply.REPLY_503, request);
		}
		FileView fromFile = session.getFileSystemView().getFile(prevRequest.getArgument());
		FileView toFile = session.getFileSystemView().getFile(request.getArgument());
		if (fromFile == null || toFile == null) {
			return Command.createResponse(FtpReply.REPLY_553, nameWithSuffix("invalid"), request);
		}
		request.attr("fromFile", fromFile.getVirtualPath());
		request.attr("toFile", toFile.getVirtualPath());
		if (!session.isWriteAllowed(toFile)) {
			return Command.createResponse(FtpReply.REPLY_553, nameWithSuffix("permission"), request);
		}
		if (!fromFile.doesExist()) {
			return Command.createResponse(FtpReply.REPLY_553, nameWithSuffix("missing"), request);
		}
		if (!fromFile.moveTo(toFile)) {
			return createResponse(FtpReply.REPLY_553, request);
		}
		return createResponse(FtpReply.REPLY_250, request);
	}

}
