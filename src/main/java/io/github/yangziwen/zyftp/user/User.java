package io.github.yangziwen.zyftp.user;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.filesystem.FileView;
import lombok.Data;

@Data
public class User {

	private String username;

	private String password;

	private int maxIdleTime;

	private String homeDirectory;

	public User() {}

	public User(String username) {
		this.username = username;
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getHomeDirectory() {
		if (StringUtils.isBlank(homeDirectory)) {
			homeDirectory =  new File(FileView.DEFAULT_HOME_DIRECTORY).getAbsolutePath();
		}
		return homeDirectory;
	}

}
