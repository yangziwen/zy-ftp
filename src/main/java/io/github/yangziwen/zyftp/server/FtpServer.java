package io.github.yangziwen.zyftp.server;

public interface FtpServer {
	
	void start();
	
	void stop();
	
	boolean isStopped();
	
	void suspend();
	
	void consume();
	
	boolean isSuspended();

}
