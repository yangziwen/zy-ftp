package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpServerHandler;
import io.github.yangziwen.zyftp.server.FtpSession;

public class STOR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		if (!request.hasArgument()) {
			return createResponse(FtpReply.REPLY_501, request);
		}

		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("invalid"), request);
		}
		request.attr("filePath", file.getVirtualPath());
		if (!session.isWriteAllowed(file)) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("permission"), request);
		}

		if (!session.isLatestDataConnectionReady()) {
			return createResponse(FtpReply.REPLY_425, request);
		}

		if (!session.increaseUploadConnections()) {
			session.getLatestDataConnection().close();
			return createResponse(FtpReply.REPLY_425, request);
		}

		FtpResponse response = createResponse(FtpReply.REPLY_150, request);
		response.setFlushedPromise(session.newChannelPromise().addListener(f -> {
			doReceiveFileContent(session, request, file);
		}));
		return response;
	}

	private void doReceiveFileContent(FtpSession session, FtpRequest request, FileView file) {
		session.getLatestDataConnection().getCloseFuture().addListener(f -> {
			session.decreaseUploadConnections();
			FtpServerHandler.sendResponse(createResponse(FtpReply.REPLY_226, request), session.getContext());
		});
	}

}
