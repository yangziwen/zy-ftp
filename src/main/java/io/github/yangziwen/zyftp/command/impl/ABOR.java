package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.util.concurrent.Promise;

public class ABOR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Promise<FtpResponse> executeAsync(FtpSession session, FtpRequest request) {
		Promise<FtpResponse> promise = session.getContext().channel().eventLoop().newPromise();
		session.getPassiveDataServer().shutdown().addListener(f -> {
			FtpResponse response = Command.createResponse(FtpResponse.REPLY_226_CLOSING_DATA_CONNECTION, "ABOR", session);
			promise.setSuccess(response);
		});
		return promise;
	}

}
