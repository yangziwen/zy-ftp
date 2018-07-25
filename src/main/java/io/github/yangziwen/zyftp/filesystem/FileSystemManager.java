package io.github.yangziwen.zyftp.filesystem;

import io.github.yangziwen.zyftp.user.User;

public interface FileSystemManager {
	
	FileSystemView createFileSystemView(User user);

}
