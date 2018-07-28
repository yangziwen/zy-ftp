package io.github.yangziwen.zyftp.server;

import java.util.concurrent.atomic.AtomicInteger;

import io.github.yangziwen.zyftp.command.impl.state.CommandState;
import io.github.yangziwen.zyftp.command.impl.state.OtherState;
import io.github.yangziwen.zyftp.common.DataConnectionType;
import io.github.yangziwen.zyftp.common.DataType;
import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.config.FtpServerConfig.ConnectionConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemView;
import io.github.yangziwen.zyftp.user.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;

public class FtpSession {

	private static final AttributeKey<FtpSession> SESSION_KEY = AttributeKey.valueOf("ftp.session");

	public static final String ANONYMOUS = "anonymous";

	public static final AtomicInteger ANONYMOUS_LOGIN_USER_COUNTER = new AtomicInteger();

	public static final AtomicInteger TOTAL_LOGIN_USER_COUNTER = new AtomicInteger();

	private ChannelHandlerContext context;

	private FtpServerContext serverContext;

	private CommandState commandState;

	private User user;

	private boolean loggedIn;

	private FileSystemView fileSystemView;

	private DataType dataType = DataType.BINARY;

	private DataConnectionType dataConnectionType;

	private String[] mlstOptionTypes;

	private FtpDataWriter dataWriter;

	private FtpPassiveDataServer passiveDataServer;

	public FtpSession(ChannelHandlerContext context, FtpServerContext serverContext) {
		this.context = context;
		this.serverContext = serverContext;
		this.commandState = OtherState.INSTANCE;
	}

	public CommandState getCommandState() {
		return commandState;
	}

	public void setCommandState(CommandState commandState) {
		this.commandState = commandState;
	}

	public User getUser() {
		return this.user;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public ChannelHandlerContext getContext() {
		return context;
	}

	public FtpServerContext getServerContext() {
		return serverContext;
	}

	public Channel getChannel() {
		return context.channel();
	}

	public EventLoopGroup getBossEventLoopGroup() {
		return getServerContext().getServer().getBossEventLoopGroup();
	}

	public EventLoopGroup getWorkerEventLoopGroup() {
		return getServerContext().getServer().getWorkerEventLoopGroup();
	}

	public FtpServerConfig getServerConfig() {
		return serverContext.getServerConfig();
	}

	public ConnectionConfig getConnectionConfig() {
		return getServerConfig().getConnectionConfig();
	}

	public FileSystemView getFileSystemView() {
		return fileSystemView;
	}

	public void setMlstOptionTypes(String[] types) {
		this.mlstOptionTypes = types;
	}

	public String[] getMlstOptionTypes() {
		return mlstOptionTypes;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public DataConnectionType getDataConnectionType() {
		return dataConnectionType;
	}

	public void setDataConnectionType(DataConnectionType dataConnectionType) {
		this.dataConnectionType = dataConnectionType;
	}

	public void setDataWriter(FtpDataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}

	public FtpDataWriter getDataWriter() {
		return dataWriter;
	}

	public void setPassiveDataServer(FtpPassiveDataServer passiveDataServer) {
		this.passiveDataServer = passiveDataServer;
	}

	public FtpPassiveDataServer getPassiveDataServer() {
		return passiveDataServer;
	}

	public Promise<Boolean> writeAndFlushData(FtpDataWriter writer) {
		if (dataConnectionType == DataConnectionType.PASV) {
			return passiveDataServer.writeAndFlushData(writer);
		} else {
			// TODO PORT
			return getWorkerEventLoopGroup().next().<Boolean>newPromise().setSuccess(false);
		}
	}

	public void preLogin(String username) {
		this.user = new User(username);
		this.loggedIn = false;
	}

	public void login(User user) {
		this.user = user;
		this.fileSystemView = new FileSystemView(user);
		if (isAnonymous(user)) {
			ANONYMOUS_LOGIN_USER_COUNTER.incrementAndGet();
		}
		TOTAL_LOGIN_USER_COUNTER.incrementAndGet();
		this.loggedIn = true;
	}

	public void logout() {
		if (isAnonymous(this.user)) {
			ANONYMOUS_LOGIN_USER_COUNTER.decrementAndGet();
		}
		TOTAL_LOGIN_USER_COUNTER.decrementAndGet();
		this.user = null;
		this.fileSystemView = null;
		this.loggedIn = false;
	}

	private Promise<Void> shutdownPassiveDataServer() {
		if (passiveDataServer != null) {
			return passiveDataServer.shutdown();
		}
		// TODO PORT
		return getWorkerEventLoopGroup().next().<Void>newPromise().setFailure(null);
	}

	public Promise<Void> shutdownDataConnection() {
		return shutdownPassiveDataServer();
	}

	public void destroy() {
		shutdownPassiveDataServer();
	}

	public boolean isDataConnectionReady() {
		if (dataConnectionType == DataConnectionType.PASV) {
			return passiveDataServer != null && passiveDataServer.isRunnning();
		}
		// TODO PORT
		return false;
	}

	public static boolean isAnonymous(User user) {
		return user != null && isAnonymous(user.getUsername());
	}

	public static boolean isAnonymous(String username) {
		return ANONYMOUS.equals(username);
	}

	public static FtpSession getOrCreateSession(ChannelHandlerContext ctx, FtpServerContext serverContext) {
		Channel channel = ctx.channel();
		if (!channel.hasAttr(SESSION_KEY)) {
			channel.attr(SESSION_KEY).setIfAbsent(new FtpSession(ctx, serverContext));
		}
		return channel.attr(SESSION_KEY).get();
	}

}
