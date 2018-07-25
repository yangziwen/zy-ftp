package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class NOOP implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		return Command.createResponse(FtpResponse.REPLY_200_COMMAND_OKAY, "NOOP", session);
	}

}
