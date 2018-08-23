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

		if (!request.hasArgument()) {
			return createFailedResponse(FtpReply.REPLY_501, request);
		}

		if (user == null || StringUtils.isBlank(user.getUsername())) {
			return createFailedResponse(FtpReply.REPLY_503, request);
		}

		if (session.isLoggedIn()) {
			return createResponse(FtpReply.REPLY_202, request);
		}

		if (FtpSession.getLoggedInUserTotalCount() >= session.getServerConfig().getMaxLogins()) {
			return createFailedResponse(FtpReply.REPLY_421, nameWithSuffix("login"), request);
		}

		if (FtpSession.getLoggedInUserCount(user.getUsername()) >= user.getUserConfig().getMaxLogins()) {
			return createFailedResponse(FtpReply.REPLY_421, nameWithSuffix("login"), request);
		}

		user.setPassword(request.getArgument());

		if (!user.authenticate()) {
			return createFailedResponse(FtpReply.REPLY_530, request);
		}

		session.login(user);

		return createResponse(FtpReply.REPLY_230, request);
	}

	private FtpResponse createFailedResponse(FtpReply reply, String subId, FtpRequest request) {
		return Command.createResponse(reply, subId, request)
				.flushedPromise(request.getSession().newChannelPromise()
						.addListener(future -> request.getSession().logout()));
	}

	private FtpResponse createFailedResponse(FtpReply reply, FtpRequest request) {
		return createFailedResponse(reply, name(), request);
	}

}
