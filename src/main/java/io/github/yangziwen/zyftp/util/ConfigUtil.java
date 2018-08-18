package io.github.yangziwen.zyftp.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtil {

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

	private static boolean hasPath(Config config, String path) {
		return config != null && config.hasPath(path);
	}

}
