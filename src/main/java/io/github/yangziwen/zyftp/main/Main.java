package io.github.yangziwen.zyftp.main;

import java.io.File;

import io.github.yangziwen.zyftp.server.FtpServer;
import io.github.yangziwen.zyftp.server.FtpServerContext;

public class Main {

	private Main() {}

	public static void main(String[] args) throws Exception {

		FtpServer server = new FtpServer(new FtpServerContext(new File("conf/server.config")));

		server.start();

		System.out.println("FtpServer started");

		addShutdownHook(server);

	}

	protected static void addShutdownHook(FtpServer server) {
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

}
