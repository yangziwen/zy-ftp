package io.github.yangziwen.zyftp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtil {

	private static final Pattern BYTES_PATTERN = Pattern.compile("(^[1-9]\\d*)(b|k|kb|m|mb|g|gb|t|tb)?$",
			Pattern.CASE_INSENSITIVE);

	private ConfigUtil() {}

	public static Config getConfig(Config config, String path) {
		return hasPath(config, path) ? config.getConfig(path) : ConfigFactory.empty();
	}

	public static boolean getBooleanOrDefault(Config config, String path, boolean defaultValue) {
		return hasPath(config, path) ? config.getBoolean(path) : defaultValue;
	}

	public static int getIntOrDefault(Config config, String path, int defaultValue) {
		return hasPath(config, path) ? config.getInt(path): defaultValue;
	}

	public static String getStringOrDefault(Config config, String path, String defaultValue) {
		return hasPath(config, path) ? config.getString(path) : defaultValue;
	}

	public static String getString(Config config, String path) {
		return getStringOrDefault(config, path, null);
	}

	private static boolean hasPath(Config config, String path) {
		return config != null && config.hasPath(path);
	}

	public static long parseBytes(String value, long defaultValue) {
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
