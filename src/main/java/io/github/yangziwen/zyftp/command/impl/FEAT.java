package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class FEAT implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		return Command.createResponse(FtpResponse.REPLY_211_SYSTEM_STATUS_REPLY, "FEAT", session);
	}

}
