package io.github.yangziwen.zyftp.command;

public interface CommandFactory {
	
	Command getCommand(String name);

}
