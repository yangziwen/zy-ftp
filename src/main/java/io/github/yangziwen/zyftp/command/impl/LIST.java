package io.github.yangziwen.zyftp.command.impl;

import java.io.IOException;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.command.impl.listing.DirectoryLister;
import io.github.yangziwen.zyftp.command.impl.listing.LISTFileFormatter;
import io.github.yangziwen.zyftp.command.impl.listing.ListArgument;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpDataConnection;
import io.github.yangziwen.zyftp.server.FtpDataWriter;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpServerHandler;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LIST implements Command {

	private static final LISTFileFormatter LIST_FILE_FORMATER = new LISTFileFormatter();

	private final DirectoryLister directoryLister = new DirectoryLister();

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!session.isLatestDataConnectionReady()) {
			return new FtpResponse(FtpReply.REPLY_503.getCode(), "PORT or PASV must be issued first");
		}
		FtpResponse response = createResponse(FtpReply.REPLY_150, request);
		response.setFlushedPromise(session.newChannelPromise().addListener(f -> {
			doSendFileList(session, request);
		}));
		return response;
	}

	private void doSendFileList(FtpSession session, FtpRequest request) {
		try {
			ListArgument parsedArg = ListArgument.parse(request.getArgument());
			String content = directoryLister.listFiles(parsedArg, session.getFileSystemView(), LIST_FILE_FORMATER);
			FtpDataConnection dataConnection = session.getLatestDataConnection();
			Promise<Void> promise = dataConnection.writeAndFlushData(new FtpDataWriter() {
				@Override
				public ChannelFuture writeAndFlushData(Channel channel) {
					byte[] bytes = content.getBytes(CharsetUtil.UTF_8);
					if (bytes.length == 0) {
						bytes = " ".getBytes(CharsetUtil.UTF_8);
					}
					ByteBuf buffer = channel.alloc().buffer(bytes.length).writeBytes(bytes);
					return channel.writeAndFlush(buffer);
				}
			});
			promise.addListener(f1 -> {
				if (!promise.isSuccess()) {
					log.error("failed to send list data", promise.cause());
					FtpServerHandler.sendResponse(createResponse(FtpReply.REPLY_551, request), session.getContext());
					dataConnection.close();
					return;
				}
				FtpServerHandler.sendResponse(createResponse(FtpReply.REPLY_226, request), session.getContext())
					.addListener(f2 -> dataConnection.close());
			});
		} catch (IOException e) {
			log.error("failed to get the file list in directory of {}",
					session.getFileSystemView().getCurrentDirectory().getVirtualPath(), e);
			FtpServerHandler.sendResponse(createResponse(FtpReply.REPLY_425, request), session.getContext());
		}
	}

}
