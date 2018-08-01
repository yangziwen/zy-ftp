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
			return Command.createResponse(FtpReply.REPLY_501, "STOR", session);
		}

		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpReply.REPLY_550, "STOR.invalid", request, session, request.getArgument());
		}

		if (!session.isLatestDataConnectionReady()) {
			return Command.createResponse(FtpReply.REPLY_425, "STOR", request, session, file.getVirtualPath());
		}

		long offset = parseOffset(session.getCommandState().getRequest("REST"));
		FtpResponse response = Command.createResponse(FtpReply.REPLY_150, "STOR", session);
		response.setFlushedPromise(session.newChannelPromise().addListener(f -> {
			doReceiveFileContent(session, request, file, offset);
		}));
		return response;
	}

	private long parseOffset(FtpRequest request) {
		if (request == null || !request.hasArgument()) {
			return 0L;
		}
		try {
			return Long.parseLong(request.getArgument());
		} catch (Exception e) {
			return 0L;
		}
	}

	private void doReceiveFileContent(FtpSession session, FtpRequest request, FileView file, long offset) {
		session.getLatestPassiveDataServer().getCloseFuture().addListener(f -> {
			FtpServerHandler.sendResponse(Command.createResponse(FtpReply.REPLY_226, "STOR", session), session.getContext());
		});
	}

}
