package io.github.yangziwen.zyftp.command;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.util.concurrent.Promise;

public interface Command {

	Logger log = LoggerFactory.getLogger(Command.class);

	String[] NON_AUTHENTICATED_COMMANDS = {"USER", "PASS", "AUTH", "QUIT", "PROT", "PBSZ" };

	FtpResponse execute(FtpSession session, FtpRequest request);

	default String name() {
		return this.getClass().getSimpleName();
	}

	default String nameWithSuffix(String suffix) {
		return name() + "." + suffix;
	}

	default Promise<FtpResponse> executeAsync(FtpSession session, FtpRequest request) {
		log.info("session[{}] receive request [{}]", session, request);
		Promise<FtpResponse> promise = session.newPromise();
		promise.setSuccess(execute(session, request));
		return promise;
	}

	default boolean canRunWithoutLogin() {
		return ArrayUtils.contains(NON_AUTHENTICATED_COMMANDS, getClass().getSimpleName());
	}

	default FtpResponse createResponse(FtpReply reply, FtpRequest request) {
		return Command.createResponse(reply, name().replaceAll("_", "."), request);
	}

	static FtpResponse createResponse(FtpReply reply, FtpSession session) {
		return createResponse(reply, "", new FtpRequest(session));
	}

	static FtpResponse createResponse(FtpReply reply, String subId, FtpRequest request) {
		String message = request.getSession().getServerContext().getMessageManager().render(reply.getMessageKey(subId), request);
		return new FtpResponse(reply.getCode(), message);
	}

}
