package io.github.yangziwen.zyftp.filesystem;

import io.github.yangziwen.zyftp.user.User;

public class FileSystemView {

	private User user;

	private FileView homeDirectory;

	private FileView currentDirectory;

	public FileSystemView(User user) {
		this.user = user;
		this.homeDirectory = new FileView(user, "/");
		this.currentDirectory = this.homeDirectory;
	}

	public FileView getHomeDirectory() {
		return homeDirectory;
	}

	public FileView getCurrentDirectory() {
		return currentDirectory;
	}

	public boolean changeCurrentDirectory(String dir) {
		FileView newDirectory = new FileView(user, dir);
		if (newDirectory.isLegalFile()) {
			this.currentDirectory = newDirectory;
			return true;
		}
		return false;
	}

	public FileView getFile(String filePath) {
		FileView file = new FileView(user, filePath);
		return file.isLegalFile() ? file : null;
	}

}
