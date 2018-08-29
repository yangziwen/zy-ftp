package io.github.yangziwen.zyftp.config;

import static io.github.yangziwen.zyftp.util.ConfigUtil.getBooleanOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getIntOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getStringOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.parseBytes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.typesafe.config.Config;

import io.github.yangziwen.zyftp.config.FtpUserConfig.Permission.Type;
import lombok.Data;

@Data
public class FtpUserConfig {

	private static final int DEFAULT_MAX_LOGINS = 20;

	private String username;

	private String encryptedPassword;

	private String homeDirectory;

	private int maxLogins;

	private int maxUploadConnectionsPerSession;

	private int maxDownloadConnectionsPerSession;

	private long uploadBytesPerSecond;

	private long downloadBytesPerSecond;

	private boolean enabled;

	private List<Permission> readPermissions = new ArrayList<>();

	private List<Permission> writePermissions = new ArrayList<>();

	public static List<FtpUserConfig> loadConfig(Config globalConfig, FtpServerConfig serverConfig) {

		List<FtpUserConfig> configs = new ArrayList<>();

		for (Config userConfig : globalConfig.getConfigList("users"))  {

			FtpUserConfig config = new FtpUserConfig();

			config.setUsername(userConfig.getString("username"));
			config.setEncryptedPassword(getStringOrDefault(userConfig, "encrypted-password", ""));
			String homeDirectoryPath = getStringOrDefault(userConfig, "home-directory", serverConfig.getDefaultHomeDirectory());
			config.setHomeDirectory(new File(homeDirectoryPath).getAbsolutePath());
			config.setEnabled(getBooleanOrDefault(userConfig, "enabled", false));
			config.setMaxLogins(getIntOrDefault(userConfig, "max-logins", DEFAULT_MAX_LOGINS));

			config.setMaxUploadConnectionsPerSession(
					getIntOrDefault(userConfig, "max-upload-connections-per-session", serverConfig.getDefaultMaxUploadConnectionsPerSession()));

			config.setMaxDownloadConnectionsPerSession(
					getIntOrDefault(userConfig, "max-download-connections-per-session", serverConfig.getDefaultMaxDownloadConnectionsPerSession()));

			String uploadBytesPerSecondValue = getStringOrDefault(userConfig, "upload-bytes-per-second", "");
			config.setUploadBytesPerSecond(parseBytes(uploadBytesPerSecondValue, serverConfig.getDefaultDownloadBytesPerSecond()));

			String downloadBytesPerSecondValue = getStringOrDefault(userConfig, "download-bytes-per-second", "");
			config.setDownloadBytesPerSecond(parseBytes(downloadBytesPerSecondValue, serverConfig.getDefaultDownloadBytesPerSecond()));

			for (Config permissionConfig : userConfig.getConfigList("readPermissions")) {
				Permission permission = loadPermissionConfig(permissionConfig);
				if (permission != null) {
					config.getReadPermissions().add(permission);
				}
			}

			for (Config permissionConfig : userConfig.getConfigList("writePermissions")) {
				Permission permission = loadPermissionConfig(permissionConfig);
				if (permission != null) {
					config.getWritePermissions().add(permission);
				}
			}

			configs.add(config);

		}

		return configs;

	}

	private static Permission loadPermissionConfig(Config permissionConfig) {
		String pattern = getStringOrDefault(permissionConfig, "pattern", "");
		Type type = Permission.Type.from(getStringOrDefault(permissionConfig, "type", null));
		if (StringUtils.isBlank(pattern) || type == null) {
			return null;
		}
		return new Permission(pattern, type);
	}

	public boolean isReadAllowed(String path) {
		return isAllowed(path, readPermissions);
	}

	public boolean isWriteAllowed(String path) {
		return isAllowed(path, writePermissions);
	}

	private boolean isAllowed(String path, List<Permission> permissions) {
		if (StringUtils.isBlank(path)) {
			return false;
		}
		if (permissions.stream().filter(p -> p.getType() == Type.DENY).anyMatch(p -> p.isMatched(path))) {
			return false;
		}
		if (permissions.stream().filter(p -> p.getType() == Type.ALLOW).anyMatch(p -> p.isMatched(path))) {
			return true;
		}
		return false;
	}

	public boolean authenticate(String password) {
		if (StringUtils.isBlank(encryptedPassword)) {
			return true;
		}
		return DigestUtils.sha1Hex(username + password).equals(encryptedPassword);
	}

	public static class Permission {

		private String pattern;

		private Type type;

		private Pattern regexPattern;

		public Permission(String pattern, Type type) {
			this.pattern = Objects.requireNonNull(pattern);
			this.type = Objects.requireNonNull(type);
			this.regexPattern = Pattern.compile(pattern);
		}

		public boolean isMatched(String path) {
			return regexPattern.matcher(path).matches();
		}

		public String getPattern() {
			return pattern;
		}

		public Type getType() {
			return type;
		}

		public static enum Type {

			ALLOW, DENY;

			public static Type from(String value) {
				if (StringUtils.isBlank(value)) {
					return null;
				}
				try {
					return Type.valueOf(Objects.requireNonNull(value).toUpperCase());
				} catch (Exception e) {
					return null;
				}
			}
		}

	}


}
