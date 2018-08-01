package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemManager;
import io.github.yangziwen.zyftp.message.MessageResource;
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

	private MessageResource messageResource;

	private PassivePorts passivePorts;

	public FtpServerContext() {
		this.serverConfig = new FtpServerConfig();
		this.messageResource = new MessageResource();
		this.passivePorts = new PassivePorts(serverConfig.getPassivePortsString());
	}

	public void setServer(FtpServer server) {
		this.server = server;
	}

}
