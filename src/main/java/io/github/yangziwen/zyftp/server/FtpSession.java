package io.github.yangziwen.zyftp.server;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.yangziwen.zyftp.command.impl.state.CommandState;
import io.github.yangziwen.zyftp.command.impl.state.OtherState;
import io.github.yangziwen.zyftp.common.DataConnectionType;
import io.github.yangziwen.zyftp.common.DataType;
import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemView;
import io.github.yangziwen.zyftp.user.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;

/**
 * The ftp session
 *
 * @author yangziwen
 */
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

	private KeySetView<FtpPassiveDataServer, Boolean> passiveDataServers = ConcurrentHashMap.newKeySet();

	private AtomicReference<FtpPassiveDataServer> latestPassiveDataServer = new AtomicReference<>();

	private KeySetView<FtpPortDataClient, Boolean> portDataClients = ConcurrentHashMap.newKeySet();

	private AtomicReference<FtpPortDataClient> latestPortDataClient = new AtomicReference<FtpPortDataClient>();

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

	public long getDownloadBytesPerSecond() {
		return this.user.getDownloadBytesPerSecond();
	}

	public long getUploadBytesPerSecond() {
		return this.user.getUploadBytesPerSecond();
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public <V> Promise<V> newPromise() {
		return this.getChannel().eventLoop().newPromise();
	}

	public ChannelPromise newChannelPromise() {
		return this.getContext().newPromise();
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public ChannelHandlerContext getContext() {
		return context;
	}

	public Channel getChannel() {
		return context.channel();
	}

	public FtpServerContext getServerContext() {
		return serverContext;
	}

	public FtpServerConfig getServerConfig() {
		return serverContext.getServerConfig();
	}

	public FileSystemView getFileSystemView() {
		return fileSystemView;
	}

	public String[] getMlstOptionTypes() {
		return mlstOptionTypes;
	}

	public void setMlstOptionTypes(String[] types) {
		this.mlstOptionTypes = types;
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

	public void addPortDataClient(FtpPortDataClient portDataClient) {
		portDataClients.add(portDataClient);
		latestPortDataClient.set(portDataClient);
	}

	public void removePortDataClient(FtpPortDataClient portDataClient) {
		portDataClients.remove(portDataClient);
		latestPortDataClient.compareAndSet(portDataClient, null);
	}

	public FtpPortDataClient getLatestPortDataClient() {
		return latestPortDataClient.get();
	}

	public void addPassiveDataServer(FtpPassiveDataServer passiveDataServer) {
		passiveDataServers.add(passiveDataServer);
		latestPassiveDataServer.set(passiveDataServer);
	}

	public void removePassiveDataServer(FtpPassiveDataServer passiveDataServer) {
		passiveDataServers.remove(passiveDataServer);
		latestPassiveDataServer.compareAndSet(passiveDataServer, null);
	}

	public FtpPassiveDataServer getLatestPassiveDataServer() {
		return latestPassiveDataServer.get();
	}

	public FtpDataConnection getLatestDataConnection() {
		if (dataConnectionType == DataConnectionType.PASV) {
			return latestPassiveDataServer.get();
		}
		if (dataConnectionType == DataConnectionType.PORT) {
			return latestPortDataClient.get();
		}
		return null;
	}

	public boolean isLatestDataConnectionReady() {
		if (dataConnectionType == DataConnectionType.PASV) {
			FtpPassiveDataServer latestPassiveDataServer = this.latestPassiveDataServer.get();
			return latestPassiveDataServer != null && latestPassiveDataServer.isRunning();
		}
		if (dataConnectionType == DataConnectionType.PORT) {
			FtpPortDataClient latestPortDataClient = this.latestPortDataClient.get();
			return latestPortDataClient != null && latestPortDataClient.isConnected();
		}
		return false;
	}

	public boolean hasRunningDataConnection() {
		if (dataConnectionType == DataConnectionType.PASV) {
			return passiveDataServers.stream().anyMatch(FtpPassiveDataServer::isRunning);
		}
		if (dataConnectionType == DataConnectionType.PORT) {
			return portDataClients.stream().anyMatch(FtpPortDataClient::isConnected);
		}
		return false;
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

	public Promise<Void> closeDataConnections() {
		Promise<Void> finishedPromise = newPromise();
		List<Promise<Void>> promises = Stream.concat(passiveDataServers.stream(), portDataClients.stream())
				.map(FtpDataConnection::close)
				.collect(Collectors.toList());
		AtomicInteger counter = new AtomicInteger(promises.size());
		promises.forEach(promise -> {
			promise.addListener(f -> {
				if (counter.decrementAndGet() == 0) {
					finishedPromise.setSuccess(null);
				}
			});
		});
		return finishedPromise;
	}

	public void destroy() {
		if (isLoggedIn()) {
			logout();
		}
		closeDataConnections();
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

	public static boolean isAllIdleStateEvent(Object evt) {
		if (!IdleStateEvent.class.isInstance(evt)) {
			return false;
		}
		IdleStateEvent event = (IdleStateEvent) evt;
		return event.state() == IdleState.ALL_IDLE;
	}

	@Override
	public String toString() {
		String[] array = super.toString().split("\\.");
		return array[array.length - 1];
	}

}
