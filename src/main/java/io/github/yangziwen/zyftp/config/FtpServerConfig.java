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

	private static final int DEFAULT_MAX_LOGINS = 100;

	private static final int DEFAULT_MAX_IDLE_SECONDS = 120;

	private static final int DEFAULT_DATA_CONNECTION_MAX_IDLE_SECONDS = 30;

	private static final String DEFAULT_LOCAL_IP = "0.0.0.0";

	private static final int DEFAULT_LOCAL_PORT = 21;

	private static final String DEFAULT_PASSIVE_ADDRESS = "127.0.0.1";

	private static final String DEFAULT_PASSIVE_PORTS = "40000-50000";

	// 在实际协议中，每个session只能开启一个上传下载通道
	// 客户端是通过同时登录多次，实现的多个文件并行下载
	// 即实际通过一个账户的最大登录数限制并行下载数即可
	private static final int DEFAULT_MAX_UPLOAD_CONNECTIONS_PER_SESSION = 10;

	private static final int DEFAULT_MAX_DOWNLOAD_CONNECTIONS_PER_SESSION = 10;

	private static final long DEFAULT_UPLOAD_BYTES_PER_SECOND = 500 * 1024L;

	private static final long DEFAULT_DOWNLOAD_BYTES_PER_SECOND = 500 * 1024L;

	private static final String DEFAULT_HOME_DIRECTORY = "res/";

	private SocketAddress localAddress;

	private int maxLogins;

	private int maxIdleSeconds;

	private int dataConnectionMaxIdleSeconds;

	private String passiveAddress;

	private String passivePortsString;

	private int defaultMaxUploadConnectionsPerSession;

	private int defaultMaxDownloadConnectionsPerSession;

	private long defaultUploadBytesPerSecond;

	private long defaultDownloadBytesPerSecond;

	private String defaultHomeDirectory;

	private ConcurrentMap<String, FtpUserConfig> userConfigs = new ConcurrentHashMap<>();

	private FtpServerConfig() {
	}

	public static FtpServerConfig loadConfig(File configFile) {

		FtpServerConfig config = new FtpServerConfig();

		Config globalConfig = ConfigFactory.parseFile(configFile);

		Config serverConfig = getConfig(globalConfig, "server");
		config.setMaxLogins(getIntOrDefault(serverConfig, "max-logins", DEFAULT_MAX_LOGINS));
		config.setMaxIdleSeconds(getIntOrDefault(serverConfig, "max-idle-seconds", DEFAULT_MAX_IDLE_SECONDS));
		config.setDataConnectionMaxIdleSeconds(getIntOrDefault(
				serverConfig, "data-connection-max-idle-seconds", DEFAULT_DATA_CONNECTION_MAX_IDLE_SECONDS));

		Config localConfig = getConfig(serverConfig, "local");
		config.setLocalAddress(new InetSocketAddress(
				getStringOrDefault(localConfig, "ip", DEFAULT_LOCAL_IP),
				getIntOrDefault(localConfig, "port", DEFAULT_LOCAL_PORT)));

		Config passiveConfig = getConfig(serverConfig, "passive");
		config.setPassiveAddress(getStringOrDefault(passiveConfig, "address", DEFAULT_PASSIVE_ADDRESS));
		config.setPassivePortsString(getStringOrDefault(passiveConfig, "ports", DEFAULT_PASSIVE_PORTS));

		Config connectionConfig = getConfig(serverConfig, "connection");
		config.setDefaultMaxUploadConnectionsPerSession(getIntOrDefault(
				connectionConfig, "default-max-upload-connections-per-session", DEFAULT_MAX_UPLOAD_CONNECTIONS_PER_SESSION));
		config.setDefaultMaxDownloadConnectionsPerSession(getIntOrDefault(
				connectionConfig, "default-max-download-connections-per-session", DEFAULT_MAX_DOWNLOAD_CONNECTIONS_PER_SESSION));
		config.setDefaultUploadBytesPerSecond(parseBytes(
				getString(connectionConfig, "default-upload-bytes-per-second"), DEFAULT_UPLOAD_BYTES_PER_SECOND));
		config.setDefaultDownloadBytesPerSecond(parseBytes(
				getString(connectionConfig, "default-download-bytes-per-second"), DEFAULT_DOWNLOAD_BYTES_PER_SECOND));

		config.setDefaultHomeDirectory(getStringOrDefault(serverConfig, "default-home-directory", DEFAULT_HOME_DIRECTORY));
		File defaultHomeDir = new File(config.getDefaultHomeDirectory());
		if (defaultHomeDir.isFile()) {
			throw new IllegalStateException(String.format(
					"the specified home directory [%s] should not be a file", config.getDefaultHomeDirectory()));
		}
		if (!defaultHomeDir.exists()) {
			defaultHomeDir.mkdirs();
		}

		for (FtpUserConfig userConfig : FtpUserConfig.loadConfig(globalConfig, config)) {
			config.userConfigs.put(userConfig.getUsername(), userConfig);
		}

		return config;
	}


}
