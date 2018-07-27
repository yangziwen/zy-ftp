package io.github.yangziwen.zyftp.command;

import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.util.ResponseMessageVariableReplacer;
import io.netty.util.concurrent.Promise;

public interface Command {

	FtpResponse execute(FtpSession session, FtpRequest request);

	default Promise<FtpResponse> executeAsync(FtpSession session, FtpRequest request) {
		Promise<FtpResponse> promise = session.getContext().channel().eventLoop().newPromise();
		promise.setSuccess(execute(session, request));
		return promise;
	}

	static FtpResponse createResponse(int code, FtpSession session) {
		return createResponse(code, null, session);
	}

	static FtpResponse createResponse(int code, String subId, FtpSession session) {
		return createResponse(code, subId, null, session);
	}

	static FtpResponse createResponse(int code, String subId, FtpRequest request, FtpSession session) {
		return createResponse(code, subId, request, session, null);
	}

	static FtpResponse createResponse(int code, String subId, FtpRequest request, FtpSession session, String basicMsg) {
		String message = session.getServerContext().getMessageResource().getMessage(code, subId);
		FtpResponse response = new FtpResponse(code, message);
		response.setBasicMsg(basicMsg);
		return ResponseMessageVariableReplacer.replaceVariables(code, subId, request, response, session);
	}

}
