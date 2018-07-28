package io.github.yangziwen.zyftp.command.impl;

import java.io.IOException;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.command.impl.listing.DirectoryLister;
import io.github.yangziwen.zyftp.command.impl.listing.FileFormatter;
import io.github.yangziwen.zyftp.command.impl.listing.ListArgument;
import io.github.yangziwen.zyftp.command.impl.listing.MLSTFileFormatter;
import io.github.yangziwen.zyftp.server.FtpDataWriter;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpServerHandler;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MLSD implements Command {

    private final DirectoryLister directoryLister = new DirectoryLister();

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!session.isDataConnectionReady()) {
			return Command.createResponse(FtpResponse.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, null, request, session, "PORT or PASV must be issued first");
		}
		FtpResponse response = Command.createResponse(FtpResponse.REPLY_150_FILE_STATUS_OKAY, "MLSD", session);
		response.setFlushedPromise(session.getContext().newPromise().addListener(f -> {
			doSendFileList(session, request);
		}));
		return response;
	}

	private void doSendFileList(FtpSession session, FtpRequest request) {
		try {
			ListArgument parsedArg = ListArgument.parse(request.getArgument());
			FileFormatter formatter = new MLSTFileFormatter(session.getMlstOptionTypes());
			String content = directoryLister.listFiles(parsedArg, session.getFileSystemView(), formatter);
			session.writeAndFlushData(new FtpDataWriter() {
				@Override
				public ChannelFuture writeAndFlushData(Channel channel) {
					byte[] bytes = content.getBytes(CharsetUtil.UTF_8);
					ByteBuf buffer = channel.alloc().buffer(bytes.length).writeBytes(bytes);
					return channel.writeAndFlush(buffer);
				}
			}).addListener(f -> {
				FtpServerHandler.sendResponse(Command.createResponse(FtpResponse.REPLY_226_CLOSING_DATA_CONNECTION, "MLSD", session), session.getContext())
					.addListener(f2 -> {
						session.shutdownDataConnection();
					});
			});
		} catch (IOException e) {
			log.error("failed to get the file list in directory of {}",
					session.getFileSystemView().getCurrentDirectory().getVirtualPath(), e);
			FtpServerHandler.sendResponse(Command.createResponse(FtpResponse.REPLY_425_CANT_OPEN_DATA_CONNECTION, "MLSD", session), session.getContext());
		}
	}

}
