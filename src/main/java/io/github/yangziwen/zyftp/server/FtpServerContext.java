package io.github.yangziwen.zyftp.server;

import java.io.File;

import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemManager;
import io.github.yangziwen.zyftp.message.MessageManager;
import io.github.yangziwen.zyftp.user.UserManager;
import io.github.yangziwen.zyftp.util.PassivePorts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The ftp server context
 *
 * @author yangziwen
 */
@Slf4j
@Getter
public class FtpServerContext {

	private FtpServer server;

	private FtpServerConfig serverConfig;

	private UserManager userManager;

	private FileSystemManager fileSystemManager;

	private MessageManager messageManager;

	private PassivePorts passivePorts;

	public FtpServerContext(File configFile) {
		this.serverConfig = FtpServerConfig.loadConfig(configFile);
		this.messageManager = new MessageManager();
		this.passivePorts = new PassivePorts(serverConfig.getPassivePortsString());
	}

	public FtpServerContext refresh() {
		if (passivePorts != null) {
			passivePorts.destroy().addListener(f -> {
				if (!f.isSuccess()) {
					log.error("failed to destroy the PassivePorts instance", f.cause());
				}
			});
		}
		this.passivePorts = new PassivePorts(serverConfig.getPassivePortsString());
		return this;
	}

	public void setServer(FtpServer server) {
		this.server = server;
	}

}
