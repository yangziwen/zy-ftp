package io.github.yangziwen.zyftp.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.user.User;

/**
 * The file view
 * mapping the virtual path to the real file
 *
 * @author yangziwen
 */
public class FileView {

	public static final String DEFAULT_HOME_DIRECTORY = "res/";

	private User user;

	private File file;

	private String virtualPath;

	private String realPath;

	public String getVirtualPath() {
		return virtualPath;
	}

	public String getRealPath() {
		return realPath;
	}

	public User getUser() {
		return user;
	}

	public File getRealFile() {
		return file;
	}

	public FileView(User user, String path) {
		if (StringUtils.isBlank(path)) {
			path = "/";
		}
		this.virtualPath = "/".equals(path) ? "/" : StringUtils.stripEnd(path, "/");
		this.user = user;
		this.file = new File(user.getHomeDirectory(), StringUtils.stripStart(this.virtualPath, "/"));
		this.realPath = file.getAbsolutePath();
	}

	public boolean doesExist() {
		return this.file.exists();
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

	public boolean isHidden() {
		return file.isHidden();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public boolean isReadable() {
		return file.canRead();
	}

	public boolean isWritable() {
		// TODO
		return true;
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

    public List<FileView> listFiles() {
    	if (!file.isDirectory()) {
    		return Collections.emptyList();
    	}
    	File[] files = file.listFiles();
    	if (ArrayUtils.isEmpty(files)) {
    		return Collections.emptyList();
    	}
    	return Arrays.stream(files)
    		.map(file -> file.getName())
    		.map(name -> new FileView(user, FilenameUtils.concat(virtualPath, name)))
    		.collect(Collectors.toList());
    }

    public boolean hasParent(FileView file) {
    	if (file == null) {
    		return false;
    	}
    	return virtualPath.startsWith(file.getVirtualPath());
    }

    public boolean mkdir() {
    	return file.mkdir();
    }

    public boolean delete() {
    	return file.delete();
    }

    public boolean moveTo(FileView dest) {
    	if (dest == null || dest.doesExist()) {
    		return false;
    	}
    	return getRealFile().renameTo(dest.getRealFile());
    }

    @Override
    public boolean equals(Object other) {
    	if (!FileView.class.isInstance(other)) {
    		return false;
    	}
    	return StringUtils.equals(realPath, FileView.class.cast(other).realPath);
    }

}
