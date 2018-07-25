package io.github.yangziwen.zyftp.command.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.user.User;

public class USER implements Command {

	private static final String ANONYMOUS = "anonymous";

	private static final AtomicInteger ANONYMOUS_USER_COUNTER = new AtomicInteger();

	private static final AtomicInteger TOTAL_USER_COUNTER = new AtomicInteger();

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String username = request.getArgument();
		if (StringUtils.isBlank(username)) {
			return createFailedResponse(FtpResponse.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "USER", request, session);
		}

		if (session.isLoggedIn()) {
			User user = session.getUser();
			if (user == null || !username.equals(user.getUsername())) {
				return createFailedResponse(FtpResponse.REPLY_530_INVALID_USER, "USER.invalid", request, session);
			}
			if (username.equals(user.getUsername())) {
				return Command.createResponse(FtpResponse.REPLY_230_USER_LOGGED_IN, "USER", request, session);
			}
		}

		boolean isAnonymous = ANONYMOUS.equals(username);

		if (isAnonymous && !session.getConnectionConfig().isAnonymousEnabled()) {
			return createFailedResponse(FtpResponse.REPLY_530_INVALID_USER, "USER.anonymous", request, session);
		}

		boolean tooManyLoggedInUsers = TOTAL_USER_COUNTER.get() >= session.getConnectionConfig().getMaxLogins();

		if (isAnonymous) {
			tooManyLoggedInUsers |= ANONYMOUS_USER_COUNTER.get() >= session.getConnectionConfig().getMaxAnonymousLogins();
		}

		if (tooManyLoggedInUsers) {
			return createFailedResponse(FtpResponse.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "USER.anonymous", request, session);
		}

		// TODO user login limit check

		session.setUser(new User(username));

		String subId = isAnonymous ? "USER.anonymous" : "USER";

		return Command.createResponse(FtpResponse.REPLY_331_USER_NAME_OKAY_NEED_PASSWORD, subId, request, session);
	}

	private FtpResponse createFailedResponse(int code, String subId, FtpRequest request, FtpSession session) {
		return Command.createResponse(code, subId, request, session).flushedPromise(session.getChannel().newPromise().addListener(future -> {
			session.getChannel().close();
		}));
	}

}
