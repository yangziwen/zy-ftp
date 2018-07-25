package io.github.yangziwen.zyftp.command;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.github.yangziwen.zyftp.command.impl.NOOP;
import io.github.yangziwen.zyftp.command.impl.QUIT;

public interface CommandFactory {
	
	static Map<String, Command> COMMANDS = ImmutableMap.<String, Command>builder()
			.put("QUIT", new QUIT())
			.put("NOOP", new NOOP())
			.build();
	
	static Command getCommand(String name) {
		return COMMANDS.get(name);
	}

}
