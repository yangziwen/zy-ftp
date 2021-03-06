package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class FEAT implements Command {

	// the response format should be modified to support filezilla
	// seems the FEAT negotiation of lftp and filezilla are quite different
	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		return createResponse(FtpReply.REPLY_211, request);
	}

}
