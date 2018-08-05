package io.github.yangziwen.zyftp.command.impl.state;

import java.util.Map;

import io.github.yangziwen.zyftp.server.FtpRequest;

public abstract class AbstractCommandState<T> implements CommandState {

	protected final Map<String, FtpRequest> requestMap;

	public AbstractCommandState(Map<String, FtpRequest> requestMap) {
		this.requestMap = requestMap;
	}

	@SuppressWarnings("unchecked")
	public T putRequest(FtpRequest request) {
		requestMap.put(request.getCommand(), request);
		return (T) this;
	}

	@Override
	public FtpRequest getRequest(String command) {
		return requestMap.get(command);
	}

}
