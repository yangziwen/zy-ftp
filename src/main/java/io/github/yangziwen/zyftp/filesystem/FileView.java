package io.github.yangziwen.zyftp.filesystem;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.user.User;
import lombok.Getter;

@Getter
public class FileView {

	public static final String DEFAULT_HOME_DIRECTORY = "res/";

	private User user;

	private File file;

	private String virtualPath;

	private String realPath;

	public FileView(User user, String path) {
		this.virtualPath = FilenameUtils.getPath(path);
		this.user = user;
		this.file = new File(user.getHomeDirectory(), StringUtils.stripStart(this.virtualPath, "/"));
		this.realPath = FilenameUtils.getPath(file.getAbsolutePath());
	}

	public boolean doesExist() {
		return this.file.exists() && isLegalFile();
	}

	public boolean isLegalFile() {
		if (user == null || file == null) {
			return false;
		}
		if (StringUtils.isBlank(user.getHomeDirectory())) {
			return false;
		}
		if (!realPath.startsWith(user.getHomeDirectory())) {
			return false;
		}
		return true;
	}

}
