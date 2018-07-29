package io.github.yangziwen.zyftp.command;

import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.util.ResponseMessageVariableReplacer;
import io.netty.util.concurrent.Promise;

public interface Command {

	FtpResponse execute(FtpSession session, FtpRequest request);

	default Promise<FtpResponse> executeAsync(FtpSession session, FtpRequest request) {
		Promise<FtpResponse> promise = session.getChannel().eventLoop().newPromise();
		promise.setSuccess(execute(session, request));
		return promise;
	}

	static FtpResponse createResponse(FtpReply reply, FtpSession session) {
		return createResponse(reply, null, session);
	}

	static FtpResponse createResponse(FtpReply reply, String subId, FtpSession session) {
		return createResponse(reply, subId, null, session);
	}

	static FtpResponse createResponse(FtpReply reply, String subId, FtpRequest request, FtpSession session) {
		return createResponse(reply, subId, request, session, null);
	}

	static FtpResponse createResponse(FtpReply reply, String subId, FtpRequest request, FtpSession session, String basicMsg) {
		String message = session.getServerContext().getMessageResource().getMessage(reply.getCode(), subId);
		FtpResponse response = new FtpResponse(reply.getCode(), message);
		response.setBasicMsg(basicMsg);
		return ResponseMessageVariableReplacer.replaceVariables(reply.getCode(), subId, request, response, session);
	}

}
