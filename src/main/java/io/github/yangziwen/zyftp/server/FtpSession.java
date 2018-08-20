package io.github.yangziwen.zyftp.server;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.command.impl.state.CommandState;
import io.github.yangziwen.zyftp.command.impl.state.OtherState;
import io.github.yangziwen.zyftp.common.DataConnectionType;
import io.github.yangziwen.zyftp.common.DataType;
import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.config.FtpUserConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemView;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.user.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * The ftp session
 *
 * @author yangziwen
 */
@Slf4j
public class FtpSession {

	private static final AttributeKey<FtpSession> SESSION_KEY = AttributeKey.valueOf("ftp.session");

	private static final ConcurrentMap<String, Set<FtpSession>> LOGGED_IN_USER_SESSION_MAP = new ConcurrentHashMap<>();

	private static AtomicReference<SelfSignedCertificate> sslCertificateRef = new AtomicReference<SelfSignedCertificate>();

	private ChannelHandlerContext context;

	private FtpServerContext serverContext;

	private CommandState commandState;

	private User user;

	private boolean loggedIn;

	private FileSystemView fileSystemView;

	private DataType dataType = DataType.BINARY;

	private DataConnectionType dataConnectionType;

	private boolean dataConnectionSecured;

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

	public FtpUserConfig getUserConfig(String username) {
		return getServerConfig().getUserConfigs().get(username);
	}

	public boolean isWriteAllowed(FileView file) {
		return file != null && isWriteAllowed(file.getVirtualPath());
	}

	public boolean isWriteAllowed(String path) {
		return user != null && user.isWriteAllowed(path);
	}

	public boolean isReadAllowed(FileView file) {
		return file != null && isReadAllowed(file.getVirtualPath());
	}

	public boolean isReadAllowed(String path) {
		return user != null && user.isReadAllowed(path);
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

	public boolean isDataConnectionSecured() {
		return dataConnectionSecured;
	}

	public void setDataConnectionSecured(boolean dataConnectionSecured) {
		this.dataConnectionSecured = dataConnectionSecured;
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
		this.user = new User(username, getUserConfig(username));
		this.loggedIn = false;
	}

	public void login(User user) {
		this.user = user;
		this.fileSystemView = new FileSystemView(user);
		this.loggedIn = true;
		ensureSessionSet(user.getUsername(), LOGGED_IN_USER_SESSION_MAP).add(this);
	}

	public void logout() {
		ensureSessionSet(user.getUsername(), LOGGED_IN_USER_SESSION_MAP).remove(this);
		this.user = null;
		this.fileSystemView = null;
		this.loggedIn = false;
	}

	public static int getLoggedInUserCount(String username) {
		if (StringUtils.isBlank(username) || !LOGGED_IN_USER_SESSION_MAP.containsKey(username)) {
			return 0;
		}
		return LOGGED_IN_USER_SESSION_MAP.get(username).size();
	}

	public static int getLoggedInUserTotalCount() {
		return LOGGED_IN_USER_SESSION_MAP.values().stream().collect(Collectors.summingInt(Set::size));
	}

	private static Set<FtpSession> ensureSessionSet(String username, ConcurrentMap<String, Set<FtpSession>> sessionMap) {
		if (!sessionMap.containsKey(username)) {
			sessionMap.putIfAbsent(username, ConcurrentHashMap.newKeySet());
		}
		return sessionMap.get(username);
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
		log.info("session[{}] is destroyed", this);
	}

	public static boolean isAnonymous(User user) {
		return user != null && user.isAnonymous();
	}

	public static FtpSession getOrCreateSession(ChannelHandlerContext ctx, FtpServerContext serverContext) {
		Channel channel = ctx.channel();
		if (!channel.hasAttr(SESSION_KEY)) {
			FtpSession newSession = new FtpSession(ctx, serverContext);
			if (null == channel.attr(SESSION_KEY).setIfAbsent(newSession)) {
				log.info("session[{}] is created", newSession);
			}
		}
		return channel.attr(SESSION_KEY).get();
	}

	private static SelfSignedCertificate getOrCreateCertificate() {
		try {
			if (sslCertificateRef.get() == null) {
				sslCertificateRef.compareAndSet(null, new SelfSignedCertificate());
			}
			return sslCertificateRef.get();
		} catch (CertificateException e) {
			log.error("failed to create ssl certificate", e);
			return null;
		}
	}

	public SslContext createClientSslContext() {
		try {
			return SslContextBuilder.forClient()
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();
		} catch (SSLException e) {
			log.error("failed to create client ssl context", e);
			return null;
		}
	}

	public SslContext createServerSslContext() {
		try {
			SelfSignedCertificate certificate = getOrCreateCertificate();
			if (certificate == null) {
				return null;
			}
			return SslContextBuilder.forServer(certificate.certificate(), certificate.privateKey()).build();
		} catch (SSLException e) {
			log.error("failed to create server ssl context", e);
			return null;
		}
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
