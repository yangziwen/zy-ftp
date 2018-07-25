package io.github.yangziwen.zyftp.listener;

import io.github.yangziwen.zyftp.server.FtpServerContext;

public interface Listener {
	
	void start(FtpServerContext context);
	
	void stop();
	
	boolean isStopped();
	
	void suspend();
	
	void resume();
	
	boolean isSuspended();
	
	int getPort();
	
	String getServerAddress();
	
	int getIdleTimeout();

}
