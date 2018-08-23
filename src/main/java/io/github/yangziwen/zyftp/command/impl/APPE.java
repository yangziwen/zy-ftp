package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.command.impl.state.AppeState;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpServerHandler;
import io.github.yangziwen.zyftp.server.FtpSession;

public class APPE implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return createResponse(FtpReply.REPLY_501, request);
		}

		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return createResponse(FtpReply.REPLY_550, request);
		}
		if (!session.isWriteAllowed(file)) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("permission"), request);
		}
		if (file.doesExist() && !file.isFile()) {
			return Command.createResponse(FtpReply.REPLY_550, nameWithSuffix("invalid"), request);
		}

		if (!session.isLatestDataConnectionReady()) {
			return createResponse(FtpReply.REPLY_425, request);
		}

		if (file.getSize() > 0) {
			FtpRequest restRequest = new FtpRequest("REST", String.valueOf(file.getSize()));
			((AppeState) session.getCommandState()).putRequest(restRequest);
		}

		FtpResponse response = createResponse(FtpReply.REPLY_150, request);
		response.setFlushedPromise(session.newChannelPromise().addListener(f -> {
			doReceiveFileContent(request, file);
		}));
		return response;
	}

	private void doReceiveFileContent(FtpRequest request, FileView file) {
		request.getSession().getLatestDataConnection().getCloseFuture().addListener(f -> {
			FtpServerHandler.sendResponse(createResponse(FtpReply.REPLY_226, request), request.getSession().getContext());
		});
	}

}
