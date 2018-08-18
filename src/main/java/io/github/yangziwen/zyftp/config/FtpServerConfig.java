package io.github.yangziwen.zyftp.config;

import static io.github.yangziwen.zyftp.util.ConfigUtil.getBooleanOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getConfig;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getIntOrDefault;
import static io.github.yangziwen.zyftp.util.ConfigUtil.getStringOrDefault;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Data
public class FtpServerConfig {

	public static final long DEFAULT_UPLOAD_BYTES_PER_SECOND = 200 * 1024L;

	public static final long DEFAULT_DOWNLOAD_BYTES_PER_SECOND = 200 * 1024L;

	private SocketAddress localAddress;

	private int maxIdleSeconds;

	private int dataConnectionMaxIdleSeconds;

	private String passiveAddress;

	private String passivePortsString;

	private boolean anonymousEnabled;

	private int maxAnonymousLogins;

	private int maxLogins;

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
				getIntOrDefault(localConfig, "port", 8121)));

		Config passiveConfig = getConfig(serverConfig, "passive");
		config.setPassiveAddress(getStringOrDefault(passiveConfig, "address", "127.0.0.1"));
		config.setPassivePortsString(getStringOrDefault(passiveConfig, "ports", "40000-50000"));

		Config connectionConfig = getConfig(serverConfig, "connection");
		config.setMaxLogins(getIntOrDefault(connectionConfig, "max-logins", 50));
		config.setAnonymousEnabled(getBooleanOrDefault(connectionConfig, "anonymous-enabeld", true));
		config.setMaxAnonymousLogins(getIntOrDefault(connectionConfig, "max-anonymous-logins", 20));

		return config;
	}

}
