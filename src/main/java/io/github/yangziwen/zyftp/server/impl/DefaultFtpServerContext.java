package io.github.yangziwen.zyftp.server.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.yangziwen.zyftp.config.ConnectionConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemManager;
import io.github.yangziwen.zyftp.listener.Listener;
import io.github.yangziwen.zyftp.server.FtpServerContext;
import io.github.yangziwen.zyftp.user.UserManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultFtpServerContext implements FtpServerContext {

	private ConnectionConfig connectionConfig;
	
	private UserManager userManager;
	
	private FileSystemManager fileSystemManager;
	
	private ConcurrentMap<String, Listener> listeners = new ConcurrentHashMap<>();
	
	
}
