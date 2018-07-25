package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.server.impl.DefaultFtpServer;
import io.github.yangziwen.zyftp.server.impl.DefaultFtpServerContext;

// seems useless
public class FtpServerFactory {
	
	private FtpServerContext serverContext;
	
	public FtpServerFactory() {
		this.serverContext = new DefaultFtpServerContext();
	}
	
	public FtpServer createServer() {
		return new DefaultFtpServer(serverContext);
	}

}
