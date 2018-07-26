package io.github.yangziwen.zyftp.command;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.github.yangziwen.zyftp.command.impl.HELP;
import io.github.yangziwen.zyftp.command.impl.NOOP;
import io.github.yangziwen.zyftp.command.impl.PASS;
import io.github.yangziwen.zyftp.command.impl.QUIT;
import io.github.yangziwen.zyftp.command.impl.SYST;
import io.github.yangziwen.zyftp.command.impl.USER;

public interface CommandFactory {

	static Map<String, Command> COMMANDS = ImmutableMap.<String, Command>builder()
			.put("HELP", new HELP())
			.put("QUIT", new QUIT())
			.put("NOOP", new NOOP())
			.put("USER", new USER())
			.put("PASS", new PASS())
			.put("SYST", new SYST())
			.build();

	static Command getCommand(String name) {
		return COMMANDS.get(name);
	}

}
