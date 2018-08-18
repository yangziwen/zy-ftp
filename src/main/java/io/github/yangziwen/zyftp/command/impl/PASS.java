package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.user.User;

public class PASS implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		User user = session.getUser();

		if (user == null || StringUtils.isBlank(user.getUsername())) {
			return createFailedResponse(FtpReply.REPLY_503, "PASS", request, session);
		}

		if (session.isLoggedIn()) {
			return Command.createResponse(FtpReply.REPLY_202, "PASS", request, session);
		}

		boolean isAnonymous = FtpSession.isAnonymous(user);

		boolean tooManyLoggedInUsers = FtpSession.TOTAL_LOGIN_USER_COUNTER.get() >= session.getServerConfig().getMaxLogins();

		if (isAnonymous) {
			tooManyLoggedInUsers |= FtpSession.ANONYMOUS_LOGIN_USER_COUNTER.get() >= session.getServerConfig().getMaxAnonymousLogins();
		}

		if (tooManyLoggedInUsers) {
			return createFailedResponse(FtpReply.REPLY_421, "USER.anonymous", request, session);
		}

		String username = user.getUsername();

		String password = request.getArgument();

		// TODO authenticate and get the valid user instance

		User authenticatedUser = new User(username, password, session.getServerConfig());

		session.login(authenticatedUser);

		// TODO initiate file system view

		return Command.createResponse(FtpReply.REPLY_230, "PASS", request, session);
	}

	private FtpResponse createFailedResponse(FtpReply reply, String subId, FtpRequest request, FtpSession session) {
		return Command.createResponse(reply, subId, request, session).flushedPromise(session.newChannelPromise().addListener(future -> {
			session.logout();
		}));
	}

}
