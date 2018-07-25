package io.github.yangziwen.zyftp.server.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import io.github.yangziwen.zyftp.server.FtpServer;
import io.github.yangziwen.zyftp.server.FtpServerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultFtpServer implements FtpServer {
	
	private FtpServerContext serverContext;
	
	private AtomicBoolean started = new AtomicBoolean(false);
	
	private AtomicBoolean suspended = new AtomicBoolean(false);
	
	public DefaultFtpServer(FtpServerContext serverContext) {
		this.serverContext = serverContext;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void consume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

}
