package io.github.yangziwen.zyftp.server;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The ftp request
 *
 * @author yangziwen
 */
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
		return StringUtils.isNotBlank(argument);
	}

	@Override
	public String toString() {
		return command + " : "
				+ ("PASS".equals(command) ? StringUtils.repeat("*", StringUtils.length(argument)) : argument);
	}

}
