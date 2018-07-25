package io.github.yangziwen.zyftp.filesystem;

public interface FileSystemView {
	
	FileView getHomeDirectory();
	
	FileView getCurrentDirectory();
	
	boolean changeCurrentDirectory(String directory);
	
	FileView getFile(String filePath);
	
}
