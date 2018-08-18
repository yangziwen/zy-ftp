package io.github.yangziwen.zyftp.user;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileView;
import lombok.Data;

@Data
public class User {

	private String username;

	private String password;

	private int maxIdleTime;

	private String homeDirectory;

	private long uploadBytesPerSecond;

	private long downloadBytesPerSecond;

	public User() {}

	public User(String username, FtpServerConfig serverConfig) {
		this.username = username;
		this.uploadBytesPerSecond = serverConfig.getDefaultUploadBytesPerSecond();
		this.downloadBytesPerSecond = serverConfig.getDefaultDownloadBytesPerSecond();
	}

	public User(String username, String password, FtpServerConfig serverConfig) {
		this(username, serverConfig);
		this.password = password;

	}

	public String getHomeDirectory() {
		if (StringUtils.isBlank(homeDirectory)) {
			homeDirectory =  new File(FileView.DEFAULT_HOME_DIRECTORY).getAbsolutePath();
		}
		return homeDirectory;
	}

}
