package io.github.yangziwen.zyftp.command.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class OPTS implements Command {

	private static final Map<String, Command> SUB_COMMANDS = ImmutableMap.<String, Command>builder()
			.put("OPTS_MLST", new OPTS_MLST())
			.put("OPTS_UTF8", new OPTS_UTF8())
			.build();

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (StringUtils.isBlank(request.getArgument())) {
			return createResponse(FtpReply.REPLY_501, request);
		}
		String subCmd = request.getArgument().split("\\s", 2)[0];
		Command command = SUB_COMMANDS.get(name() + "_" + subCmd);
		if (command == null) {
			return createResponse(FtpReply.REPLY_502, request);
		}
		return command.execute(session, request);
	}

}
