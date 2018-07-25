package io.github.yangziwen.zyftp.main;

import java.io.File;

import io.github.yangziwen.zyftp.server.FtpServer;
import io.github.yangziwen.zyftp.server.FtpServerFactory;

public class CommandLine {
	
	private CommandLine() {}
	
	public static void main(String[] args) {
		
		CommandLine cli = new CommandLine();
		
		FtpServer server = cli.createServer(args);
		
		if (server == null) {
			return;
		}
		
		server.start();
		
		System.out.println("FtpServer started");
		
		addShutdownHook(server);
		
	}
	
	protected FtpServer createServer(String[] args) {
		if (args.length == 0 || "-default".equals(args[0]) || "--default".equals(args[0])) {
			return new FtpServerFactory().createServer();
		}
		if ("-?".equals(args[0]) || "--help".equals(args[0])) {
			usage();
			return null;
		}
		if (args.length == 1) {
			return createServerByXml(new File(args[0]));
		}
		return null;
	}
	
	protected void usage() {
		System.out.println("print some info");
	}
	
	protected FtpServer createServerByXml(File xmlConfig) {
		// TODO
		return null;
	}
	
	protected static void addShutdownHook(FtpServer server) {
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

}
