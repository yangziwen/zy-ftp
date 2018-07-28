package io.github.yangziwen.zyftp.command.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class OPTS_MLST implements Command {

	private final static String[] AVAILABLE_TYPES = { "Size", "Modify", "Type", "Perm" };

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {

		int spIndex = request.getArgument().indexOf(" ");

		String listTypes = spIndex == -1 ? "" : request.getArgument().substring(spIndex + 1);

		String[] types = StringUtils.isBlank(listTypes) ? ArrayUtils.EMPTY_STRING_ARRAY : listTypes.split(";");

		String[] validatedTypes = validateSelectedTypes(types);

		if (ArrayUtils.isEmpty(validatedTypes)) {
			return Command.createResponse(FtpReply.REPLY_501, "OPTS.MLST", request, session, listTypes);
		}

		session.setMlstOptionTypes(validatedTypes);

		return Command.createResponse(FtpReply.REPLY_200, "OPTS.MLST", request, session, StringUtils.join(validatedTypes, ";"));
	}

	private String[] validateSelectedTypes(String[] types) {
		return Arrays.stream(types)
				.filter(type -> ArrayUtils.contains(AVAILABLE_TYPES, type))
				.collect(Collectors.toList())
				.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
	}

}
