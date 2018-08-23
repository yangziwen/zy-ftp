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
			return createResponse(FtpReply.REPLY_501, request);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("invalid"), request);
		}
		request.attr("dirPath", file.getVirtualPath());
		if (!session.isWriteAllowed(file)) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("permission"), request);
		}
		if (!file.isWritable()) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("permission"), request);
		}
		if (file.doesExist()) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("exists"), request);
		}
		if (!file.mkdir()) {
			return createResponse(FtpReply.REPLY_550, request);
		}
		return createResponse(FtpReply.REPLY_257, request);
	}

}
