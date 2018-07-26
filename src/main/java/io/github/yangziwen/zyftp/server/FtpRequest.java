package io.github.yangziwen.zyftp.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FtpRequest {

	private String command;

	private String argument;

	public String getRequestLine() {
		return command + " " + argument;
	}

	public boolean hasArgument() {
		return argument != null;
	}

}
