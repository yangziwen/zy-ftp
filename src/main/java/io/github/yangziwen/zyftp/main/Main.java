package io.github.yangziwen.zyftp.main;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			description = "specify the config file position",
			required = false)
	private File configFile = new File("conf/server.config");

	@Parameter(
			names = {"-l", "--log"},
			description = "specify the log file position",
			required = false)
	private File logFile = new File("log/zy-ftp.log");

	@Parameter(
			names = {"--passive-address"},
			description = "specify the passive address",
			required = false)
	private String passiveAddress;

	@Parameter(
			names = {"--passive-ports"},
			description = "specify the passive ports",
			required = false)
	private String passivePorts;

	private Main() {}

	public static void main(String[] args) throws Exception {

		Main main = new Main();

		System.setProperty("zy-ftp.log", main.logFile.getCanonicalPath());

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

		Logger logger = LoggerFactory.getLogger(this.getClass());

		if (!configFile.isFile()) {
			logger.error("configFile[{}] does not exist!", configFile.getCanonicalPath());
			System.exit(1);
		}

		logger.info("use {} as the config file", configFile.getCanonicalPath());

		logger.info("the log file position is {}", logFile.getCanonicalPath());

		FtpServerContext context = new FtpServerContext(configFile);

		if (StringUtils.isNotBlank(passiveAddress)) {
			context.getServerConfig().setPassiveAddress(passiveAddress);
		}
		if (StringUtils.isNotBlank(passivePorts)) {
			context.getServerConfig().setPassivePortsString(passivePorts);
			context.refresh();
		}

		FtpServer server = new FtpServer(context);

		server.start();

		logger.info("FtpServer started");

		addShutdownHook(server);
	}

	protected static void addShutdownHook(FtpServer server) {
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}

}
