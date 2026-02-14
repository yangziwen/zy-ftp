package io.github.yangziwen.zyftp.command.impl;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class SYST implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		String systemName = System.getProperty("os.name");

		if (StringUtils.isBlank(systemName)) {
			systemName = "UNKNOWN";
		} else if (systemName.startsWith("Mac OS X")) {
			systemName = "UNIX Type: L8";
		} else {
			systemName = systemName.replaceAll(" ", "-").toUpperCase();
		}
		return createResponse(FtpReply.REPLY_215, request.attr("systemName", systemName));
	}

}
