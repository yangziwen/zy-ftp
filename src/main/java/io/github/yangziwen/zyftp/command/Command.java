package io.github.yangziwen.zyftp.command;

import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public interface Command {

	FtpResponse execute(FtpSession session, FtpRequest request);

	static FtpResponse createResponse(int code, FtpSession session) {
		return createResponse(code, null, session);
	}

	static FtpResponse createResponse(int code, String subId, FtpSession session) {
		String message = session.getServerContext().getMessageResource().getMessage(code, subId);
		return new FtpResponse(code, message);
	}
}
