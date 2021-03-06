package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class ACCT implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		return createResponse(FtpReply.REPLY_202, request);
	}

}
