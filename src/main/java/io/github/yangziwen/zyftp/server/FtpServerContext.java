package io.github.yangziwen.zyftp.server;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemManager;
import io.github.yangziwen.zyftp.message.MessageManager;
import io.github.yangziwen.zyftp.user.UserManager;
import io.github.yangziwen.zyftp.util.PassivePorts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The ftp server context
 *
 * @author yangziwen
 */
@Slf4j
@Getter
public class FtpServerContext {

	private FtpServer server;

	private FtpServerConfig serverConfig;

	private UserManager userManager;

	private FileSystemManager fileSystemManager;

	private MessageManager messageManager;

	private PassivePorts passivePorts;

	/**
	 * 当前服务端实例下，各用户名对应的已登录会话集合。
	 * 从 FtpSession 的 static 字段迁移至此，保证多个 FtpServer 实例的登录状态相互隔离。
	 */
	private final ConcurrentMap<String, Set<FtpSession>> loggedInUserSessions = new ConcurrentHashMap<>();

	public FtpServerContext(File configFile) {
		this.serverConfig = FtpServerConfig.loadConfig(configFile);
		this.messageManager = new MessageManager();
		this.passivePorts = new PassivePorts(serverConfig.getPassivePortsString());
	}

	public FtpServerContext refresh() {
		if (passivePorts != null) {
			passivePorts.destroy().addListener(f -> {
				if (!f.isSuccess()) {
					log.error("failed to destroy the PassivePorts instance", f.cause());
				}
			});
		}
		this.passivePorts = new PassivePorts(serverConfig.getPassivePortsString());
		return this;
	}

	public void setServer(FtpServer server) {
		this.server = server;
	}

	/**
	 * 注册一个已登录会话，返回是否新增成功（若会话已存在则返回 false）。
	 */
	public boolean registerLogin(String username, FtpSession session) {
		if (StringUtils.isBlank(username) || session == null) {
			return false;
		}
		return loggedInUserSessions
				.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
				.add(session);
	}

	/**
	 * 注销一个已登录会话，返回是否移除成功。
	 */
	public boolean unregisterLogin(String username, FtpSession session) {
		if (StringUtils.isBlank(username) || session == null) {
			return false;
		}
		Set<FtpSession> sessions = loggedInUserSessions.get(username);
		boolean removed = sessions != null && sessions.remove(session);
		if (removed && sessions.isEmpty()) {
			loggedInUserSessions.remove(username, sessions);
		}
		return removed;
	}

	/**
	 * 返回指定用户名当前已登录的会话数。
	 */
	public int getLoggedInUserCount(String username) {
		if (StringUtils.isBlank(username)) {
			return 0;
		}
		Set<FtpSession> sessions = loggedInUserSessions.get(username);
		return sessions == null ? 0 : sessions.size();
	}

	/**
	 * 返回当前服务端实例下所有已登录会话的总数。
	 */
	public int getLoggedInUserTotalCount() {
		return loggedInUserSessions.values().stream().collect(Collectors.summingInt(Set::size));
	}

}
