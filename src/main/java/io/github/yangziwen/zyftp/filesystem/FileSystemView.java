package io.github.yangziwen.zyftp.filesystem;

import org.apache.commons.io.FilenameUtils;

import io.github.yangziwen.zyftp.user.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		try {
			FileView newDirectory = new FileView(user, FilenameUtils.concat(currentDirectory.getVirtualPath(), dir));
			if (newDirectory.isLegalFile() && newDirectory.isDirectory()) {
				this.currentDirectory = newDirectory;
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("failed to change current directory from {} to {}", currentDirectory.getVirtualPath(), dir, e);
			return false;
		}
	}

	public FileView getFile(String filePath) {
		FileView file = new FileView(user, FilenameUtils.concat(getCurrentDirectory().getVirtualPath(), filePath));
		return file.isLegalFile() ? file : null;
	}

}
