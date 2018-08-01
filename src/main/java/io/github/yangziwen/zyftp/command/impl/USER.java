package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.user.User;

public class USER implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String username = request.getArgument();
		if (StringUtils.isBlank(username)) {
			return createFailedResponse(FtpReply.REPLY_501, "USER", request, session);
		}

		if (session.isLoggedIn()) {
			User user = session.getUser();
			if (user == null || !username.equals(user.getUsername())) {
				return createFailedResponse(FtpReply.REPLY_530, "USER.invalid", request, session);
			}
			if (username.equals(user.getUsername())) {
				return Command.createResponse(FtpReply.REPLY_230, "USER", request, session);
			}
		}

		boolean isAnonymous = FtpSession.isAnonymous(username);

		if (isAnonymous && !session.getConnectionConfig().isAnonymousEnabled()) {
			return createFailedResponse(FtpReply.REPLY_530, "USER.anonymous", request, session);
		}

		boolean tooManyLoggedInUsers = FtpSession.TOTAL_LOGIN_USER_COUNTER.get() >= session.getConnectionConfig().getMaxLogins();

		if (isAnonymous) {
			tooManyLoggedInUsers |= FtpSession.ANONYMOUS_LOGIN_USER_COUNTER.get() >= session.getConnectionConfig().getMaxAnonymousLogins();
		}

		if (tooManyLoggedInUsers) {
			return createFailedResponse(FtpReply.REPLY_421, "USER.anonymous", request, session);
		}

		// TODO user login limit check

		session.preLogin(username);

		String subId = isAnonymous ? "USER.anonymous" : "USER";

		return Command.createResponse(FtpReply.REPLY_331, subId, request, session);
	}

	private FtpResponse createFailedResponse(FtpReply reply, String subId, FtpRequest request, FtpSession session) {
		return Command.createResponse(reply, subId, request, session).flushedPromise(session.newChannelPromise().addListener(future -> {
			session.getChannel().close();
		}));
	}

}
