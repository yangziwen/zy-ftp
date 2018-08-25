package io.github.yangziwen.zyftp.main;

import java.io.File;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import io.github.yangziwen.zyftp.server.FtpServer;
import io.github.yangziwen.zyftp.server.FtpServerContext;

public class Main {

	@Parameter(
			names = {"-h", "--help"},
			description = "print this message", help = true)
	private boolean help;

	@Parameter(
			names = {"-c", "--config"},
			description = "specify the config file",
			required = false)
	private File configFile = new File("conf/server.config");

	private Main() {}

	public static void main(String[] args) throws Exception {

		Main main = new Main();

		JCommander commander = JCommander.newBuilder()
				.addObject(main)
				.build();

		commander.parse(args);

		if (main.help) {
			commander.usage();
			return;
		}

		main.run();
	}

	public void run() throws Exception {

		System.out.println("use " + configFile.getCanonicalPath() + " as the config file");

		FtpServer server = new FtpServer(new FtpServerContext(configFile));

		server.start();

		System.out.println("FtpServer started");

		addShutdownHook(server);
	}

	protected static void addShutdownHook(FtpServer server) {
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

}
