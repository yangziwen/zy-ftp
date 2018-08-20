package io.github.yangziwen.zyftp.config;

import static io.github.yangziwen.zyftp.util.ConfigUtil.getConfig;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getIntOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getString;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getStringOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.parseBytes;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Data
public class FtpServerConfig {

	private SocketAddress localAddress;

	private int maxLogins;

	private int maxIdleSeconds;

	private int dataConnectionMaxIdleSeconds;

	private String passiveAddress;

	private String passivePortsString;

	private long defaultUploadBytesPerSecond;

	private long defaultDownloadBytesPerSecond;

	private ConcurrentMap<String, FtpUserConfig> userConfigs = new ConcurrentHashMap<>();

	private FtpServerConfig() {
	}

	public static FtpServerConfig loadConfig(File configFile) {

		FtpServerConfig config = new FtpServerConfig();

		Config globalConfig = ConfigFactory.parseFile(configFile);

		Config serverConfig = getConfig(globalConfig, "server");
		config.setMaxLogins(getIntOrDefault(serverConfig, "max-logins", 100));
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
		config.setDefaultUploadBytesPerSecond(parseBytes(getString(connectionConfig, "default-upload-bytes-per-second"), 500 * 1024L));
		config.setDefaultDownloadBytesPerSecond(parseBytes(getString(connectionConfig, "default-download-bytes-per-second"), 500 * 1024L));

		for (FtpUserConfig userConfig : FtpUserConfig.loadConfig(globalConfig, config)) {
			config.userConfigs.put(userConfig.getUsername(), userConfig);
		}

		return config;
	}


}
