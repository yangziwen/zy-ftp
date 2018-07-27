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

	public boolean isFile() {
		return file.isFile();
	}

	public long getSize() {
		return file.length();
	}

	public long getLastModified() {
		return file.lastModified();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public boolean isReadable() {
		return file.canRead();
	}

	public boolean isWritable() {
		return file.canWrite();
	}

    public int getLinkCount() {
        return file.isDirectory() ? 3 : 1;
    }

    public String getOwnerName() {
        return "user";
    }

    public String getGroupName() {
        return "group";
    }

    public String getName() {

        // root - the short name will be '/'
        if (virtualPath.equals("/")) {
            return "/";
        }

        // strip the last '/'
        String shortName = virtualPath;
        int filelen = virtualPath.length();
        if (shortName.charAt(filelen - 1) == '/') {
            shortName = shortName.substring(0, filelen - 1);
        }

        // return from the last '/'
        int slashIndex = shortName.lastIndexOf('/');
        if (slashIndex != -1) {
            shortName = shortName.substring(slashIndex + 1);
        }
        return shortName;
    }


}
