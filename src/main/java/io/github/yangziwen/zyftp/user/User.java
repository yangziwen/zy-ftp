package io.github.yangziwen.zyftp.user;

import io.github.yangziwen.zyftp.config.FtpUserConfig;
import lombok.Data;

@Data
public class User {

	public static final String ANONYMOUS = "anonymous";

	private String username;

	private String password;

	private FtpUserConfig userConfig;

	public User() {}

	public User(String username, FtpUserConfig userConfig) {
		this.username = username;
		this.userConfig = userConfig;
	}

	public User(String username, String password, FtpUserConfig serverConfig) {
		this(username, serverConfig);
		this.password = password;
	}

	public long getUploadBytesPerSecond() {
		return userConfig.getUploadBytesPerSecond();
	}

	public long getDownloadBytesPerSecond() {
		return userConfig.getDownloadBytesPerSecond();
	}

	public String getHomeDirectory() {
		return userConfig.getHomeDirectory();
	}

	public boolean isEnabled() {
		return userConfig != null && userConfig.isEnabled();
	}

	public boolean isAnonymous() {
		return ANONYMOUS.equals(username);
	}

	public boolean authenticate() {
		return userConfig.authenticate(password);
	}

}
