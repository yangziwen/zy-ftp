package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class HELP implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		if (request.hasArgument()) {

			String cmd = request.getArgument().toUpperCase();

			FtpResponse response = Command.createResponse(FtpResponse.REPLY_214_HELP_MESSAGE, cmd, request, session);

			if (!StringUtils.isBlank(response.getMessage())) {
				return response;
			}

		}

		return Command.createResponse(FtpResponse.REPLY_214_HELP_MESSAGE, session);
	}

}
