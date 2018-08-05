package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
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
			return Command.createResponse(FtpReply.REPLY_501, "APPE", session);
		}

		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null) {
			return Command.createResponse(FtpReply.REPLY_550, "APPE", session);
		}
		if (file.doesExist() && !file.isFile()) {
			return Command.createResponse(FtpReply.REPLY_550, "APPE.invalid", session);
		}

		if (!session.isLatestDataConnectionReady()) {
			return Command.createResponse(FtpReply.REPLY_425, "APPE", session);
		}

		FtpResponse response = Command.createResponse(FtpReply.REPLY_150, "APPE", session);
		response.setFlushedPromise(session.newChannelPromise().addListener(f -> {
			doReceiveFileContent(session, request, file);
		}));
		return response;
	}

	private void doReceiveFileContent(FtpSession session, FtpRequest request, FileView file) {
		session.getLatestDataConnection().getCloseFuture().addListener(f -> {
			FtpServerHandler.sendResponse(Command.createResponse(FtpReply.REPLY_226, "APPE", session), session.getContext());
		});
	}

}
