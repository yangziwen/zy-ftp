package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.config.FtpUserConfig;
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
			if (user == null || !username.equals(user.getUsername()) || user.isEnabled()) {
				return createFailedResponse(FtpReply.REPLY_530, "USER.invalid", request, session);
			}
			if (username.equals(user.getUsername())) {
				return Command.createResponse(FtpReply.REPLY_230, "USER", request, session);
			}
		}

		FtpUserConfig userConfig = session.getUserConfig(username);
		if (userConfig == null || !userConfig.isEnabled()) {
			log.warn("user[{}] does not exist", username);
			return createFailedResponse(FtpReply.REPLY_530, "USER.invalid", request, session);
		}

		if (FtpSession.getLoggedInUserTotalCount() >= session.getServerConfig().getMaxLogins()) {
			log.warn("too many logins of users");
			return createFailedResponse(FtpReply.REPLY_421, "USER.login", request, session);
		}

		if (FtpSession.getLoggedInUserCount(username) >= userConfig.getMaxLogins()) {
			log.warn("too many logins of user[{}]", username);
			return createFailedResponse(FtpReply.REPLY_421, "USER.login", request, session);
		}

		session.preLogin(username);

		return Command.createResponse(FtpReply.REPLY_331, "USER", request, session);
	}

	private FtpResponse createFailedResponse(FtpReply reply, String subId, FtpRequest request, FtpSession session) {
		return Command.createResponse(reply, subId, request, session).flushedPromise(session.newChannelPromise().addListener(future -> {
			session.getChannel().close();
		}));
	}

}
