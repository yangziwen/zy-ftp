package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.user.User;

public class PASS implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		User user = session.getUser();

		if (user == null || StringUtils.isBlank(user.getUsername())) {
			return createFailedResponse(FtpResponse.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "PASS", request, session);
		}

		if (session.isLoggedIn()) {
			return Command.createResponse(FtpResponse.REPLY_202_COMMAND_NOT_IMPLEMENTED, "PASS", request, session);
		}

		boolean isAnonymous = FtpSession.isAnonymous(user);

		boolean tooManyLoggedInUsers = FtpSession.TOTAL_LOGIN_USER_COUNTER.get() >= session.getConnectionConfig().getMaxLogins();

		if (isAnonymous) {
			tooManyLoggedInUsers |= FtpSession.ANONYMOUS_LOGIN_USER_COUNTER.get() >= session.getConnectionConfig().getMaxAnonymousLogins();
		}

		if (tooManyLoggedInUsers) {
			return createFailedResponse(FtpResponse.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, "USER.anonymous", request, session);
		}

		String username = user.getUsername();

		String password = request.getArgument();

		// TODO authenticate and get the valid user instance

		User authenticatedUser = new User(username, password);

		session.login(authenticatedUser);

		// TODO initiate file system view

		return Command.createResponse(FtpResponse.REPLY_230_USER_LOGGED_IN, "PASS", request, session);
	}

	private FtpResponse createFailedResponse(int code, String subId, FtpRequest request, FtpSession session) {
		return Command.createResponse(code, subId, request, session).flushedPromise(session.getChannel().newPromise().addListener(future -> {
			session.logout();
		}));
	}

}
