package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.github.yangziwen.zyftp.util.ResponseMessageVariableReplacer;

public class SYST implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String systemName = System.getProperty("os.name");

		if (StringUtils.isBlank(systemName)) {
			systemName = "UNKNOWN";
		} else {
			systemName = systemName.replaceAll(" ", "-").toUpperCase();
		}

		FtpResponse response = Command.createResponse(FtpResponse.REPLY_215_NAME_SYSTEM_TYPE, "SYST", request, session);
		response.setBasicMsg(systemName);
		return ResponseMessageVariableReplacer.replaceVariables(FtpResponse.REPLY_215_NAME_SYSTEM_TYPE, "SYST", request, response, session);
	}

}
