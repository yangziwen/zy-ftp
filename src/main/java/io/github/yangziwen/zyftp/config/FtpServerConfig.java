package io.github.yangziwen.zyftp.config;

import static io.github.yangziwen.zyftp.util.ConfigUtil.getBooleanOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getConfig;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getIntOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getString;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getStringOrDefault;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Data
public class FtpServerConfig {

	private static final Pattern BYTES_PATTERN = Pattern.compile("(^[1-9]\\d*)(b|k|kb|m|mb|g|gb|t|tb)?$",
			Pattern.CASE_INSENSITIVE);

	private SocketAddress localAddress;

	private int maxIdleSeconds;

	private int dataConnectionMaxIdleSeconds;

	private String passiveAddress;

	private String passivePortsString;

	private boolean anonymousEnabled;

	private int maxAnonymousLogins;

	private int maxLogins;

	private long defaultUploadBytesPerSecond;

	private long defaultDownloadBytesPerSecond;

	private FtpServerConfig() {
	}

	public static FtpServerConfig loadConfig(File configFile) {

		FtpServerConfig config = new FtpServerConfig();

		Config globalConfig = ConfigFactory.parseFile(configFile);

		Config serverConfig = getConfig(globalConfig, "server");
		config.setMaxIdleSeconds(getIntOrDefault(serverConfig, "max-idle-seconds", 120));
		config.setDataConnectionMaxIdleSeconds(getIntOrDefault(serverConfig, "data-connection-max-idle-seconds", 30));

		Config localConfig = getConfig(serverConfig, "local");
		config.setLocalAddress(new InetSocketAddress(
				getStringOrDefault(localConfig, "ip", "0.0.0.0"),
				getIntOrDefault(localConfig, "port", 21)));

		Config passiveConfig = getConfig(serverConfig, "passive");
		config.setPassiveAddress(getStringOrDefault(passiveConfig, "address", "127.0.0.1"));
		config.setPassivePortsString(getStringOrDefault(passiveConfig, "ports", "40000-50000"));

		Config connectionConfig = getConfig(serverConfig, "connection");
		config.setMaxLogins(getIntOrDefault(connectionConfig, "max-logins", 50));
		config.setAnonymousEnabled(getBooleanOrDefault(connectionConfig, "anonymous-enabeld", true));
		config.setMaxAnonymousLogins(getIntOrDefault(connectionConfig, "max-anonymous-logins", 20));
		config.setDefaultUploadBytesPerSecond(parseBytes(getString(connectionConfig, "default-upload-bytes-per-second"), 500 * 1024L));
		config.setDefaultDownloadBytesPerSecond(parseBytes(getString(connectionConfig, "default-download-bytes-per-second"), 500 * 1024L));

		return config;
	}

	private static long parseBytes(String value, long defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		Matcher matcher = BYTES_PATTERN.matcher(StringUtils.strip(value));
		if (!matcher.matches()) {
			return defaultValue;
		}
		long bytes = Long.valueOf(matcher.group(1));
		if (StringUtils.isBlank(matcher.group(2))) {
			return bytes;
		}
		String unit = StringUtils.stripEnd(matcher.group(2).toLowerCase(), "b");
		if (StringUtils.isBlank(unit)) {
			return Long.valueOf(matcher.group(1));
		}
		String[] units = {"k", "m", "g", "t"};
		int index = ArrayUtils.indexOf(units, unit);
		for (int i = 0; i <= index; i++) {
			bytes *= 1024;
		}
		return bytes;
	}

}
