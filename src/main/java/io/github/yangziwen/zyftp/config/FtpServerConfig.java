package io.github.yangziwen.zyftp.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import lombok.Data;

@Data
public class FtpServerConfig {
	
	private SocketAddress localAddress;
	
	private ConnectionConfig connectionConfig;
	
	public FtpServerConfig() {
		this.localAddress = new InetSocketAddress("0.0.0.0", 8121);
		this.connectionConfig = new ConnectionConfig();
	}

	@Data
	public static class ConnectionConfig {
		
		private boolean isAnonymousEnabled = true;
		
		private int maxAnonymousLogins = 20;
		
		private int maxLogins = 50;
		
	}

}
