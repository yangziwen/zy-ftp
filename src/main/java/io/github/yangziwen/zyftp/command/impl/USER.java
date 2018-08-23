package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.config.FtpUserConfig;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.user.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class USER implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String username = request.getArgument();
		if (StringUtils.isBlank(username)) {
			return createFailedResponse(FtpReply.REPLY_501, request);
		}

		if (session.isLoggedIn()) {
			User user = session.getUser();
			if (user == null || !username.equals(user.getUsername()) || user.isEnabled()) {
				return createFailedResponse(FtpReply.REPLY_530, nameWithSuffix("invalid"), request);
			}
			if (username.equals(user.getUsername())) {
				return createResponse(FtpReply.REPLY_230, request);
			}
		}

		FtpUserConfig userConfig = session.getUserConfig(username);
		if (userConfig == null || !userConfig.isEnabled()) {
			log.warn("user[{}] does not exist", username);
			return createFailedResponse(FtpReply.REPLY_530, nameWithSuffix("invalid"), request);
		}

		if (FtpSession.getLoggedInUserTotalCount() >= session.getServerConfig().getMaxLogins()) {
			log.warn("too many logins of users");
			return createFailedResponse(FtpReply.REPLY_421, nameWithSuffix("login"), request);
		}

		if (FtpSession.getLoggedInUserCount(username) >= userConfig.getMaxLogins()) {
			log.warn("too many logins of user[{}]", username);
			return createFailedResponse(FtpReply.REPLY_421, nameWithSuffix("login"), request);
		}

		session.preLogin(username);

		return createResponse(FtpReply.REPLY_331, request);
	}

	private FtpResponse createFailedResponse(FtpReply reply, String subId, FtpRequest request) {
		return Command.createResponse(reply, subId, request)
				.flushedPromise(request.getSession().newChannelPromise().addListener(future -> {
			request.getSession().getChannel().close();
		}));
	}

	private FtpResponse createFailedResponse(FtpReply reply, FtpRequest request) {
		return createFailedResponse(reply, name(), request);
	}

}
