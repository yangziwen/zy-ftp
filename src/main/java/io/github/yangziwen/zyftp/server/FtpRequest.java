package io.github.yangziwen.zyftp.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The ftp request
 *
 * @author yangziwen
 */
@Data
public class FtpRequest {

	private String command;

	private String argument;

	private FtpSession session;

	private Map<String, Object> attr = new HashMap<>();

	public FtpRequest() {}

	public FtpRequest(FtpSession session) {
		this.session = session;
	}

	public FtpRequest(String command, String argument) {
		this.command = command;
		this.argument = argument;
	}

	public String getRequestLine() {
		return command + " " + argument;
	}

	public boolean hasArgument() {
		return StringUtils.isNotBlank(argument);
	}

	@SuppressWarnings("unchecked")
	public <T> T attr(String key) {
		return (T) attr.get(key);
	}

	public FtpRequest attr(String key, Object value) {
		attr.put(key, value);
		return this;
	}

	@Override
	public String toString() {
		return command + " : "
				+ ("PASS".equals(command) ? StringUtils.repeat("*", StringUtils.length(argument)) : argument);
	}

}
