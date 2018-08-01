package io.github.yangziwen.zyftp.filesystem;

import io.github.yangziwen.zyftp.user.User;

/**
 * The file system manager
 *
 * @author yangziwen
 */
public interface FileSystemManager {

	/**
	 * Create the file system view for the user
	 * @param user
	 * @return
	 */
	FileSystemView createFileSystemView(User user);

}
