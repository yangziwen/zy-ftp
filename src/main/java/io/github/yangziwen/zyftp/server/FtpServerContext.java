package io.github.yangziwen.zyftp.server;

import java.io.File;

import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemManager;
import io.github.yangziwen.zyftp.message.MessageManager;
import io.github.yangziwen.zyftp.user.UserManager;
import io.github.yangziwen.zyftp.util.PassivePorts;
import lombok.Getter;

/**
 * The ftp server context
 *
 * @author yangziwen
 */
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

	public void setServer(FtpServer server) {
		this.server = server;
	}

}
