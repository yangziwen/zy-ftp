package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpDataWriter;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpServerHandler;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

public class RETR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "RETR", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null || !file.doesExist()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.missing", request, session, file.getVirtualPath());
		}
		if (!file.isFile()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.invalid", request, session, file.getVirtualPath());
		}
		if (!file.isReadable()) {
			return Command.createResponse(FtpResponse.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RETR.permission", request, session, file.getVirtualPath());
		}
		if (!session.isDataConnectionReady()) {
			return Command.createResponse(FtpResponse.REPLY_425_CANT_OPEN_DATA_CONNECTION, "RETR", request, session, file.getVirtualPath());
		}
		FtpResponse response = Command.createResponse(FtpResponse.REPLY_150_FILE_STATUS_OKAY, "RETR", session);
		response.setFlushedPromise(session.getContext().newPromise().addListener(f -> {
			doSendFileContent(session, request, file);
		}));
		return response;
	}

	private void doSendFileContent(FtpSession session, FtpRequest request, FileView file) {
		FileRegion region = new DefaultFileRegion(file.getRealFile(), 0, file.getSize());
		session.writeAndFlushData(new FtpDataWriter() {
			@Override
			public ChannelFuture writeAndFlushData(Channel ctx) {
				return ctx.writeAndFlush(region);
			}
		}).addListener(f -> {
			FtpServerHandler.sendResponse(Command.createResponse(FtpResponse.REPLY_226_CLOSING_DATA_CONNECTION, "RETR", session), session.getContext())
				.addListener(f2 -> {
					session.shutdownDataConnection();
				});
		});
	}

}
