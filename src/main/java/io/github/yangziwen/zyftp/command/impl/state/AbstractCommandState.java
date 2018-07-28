package io.github.yangziwen.zyftp.command.impl.state;

import java.util.Map;

import io.github.yangziwen.zyftp.server.FtpRequest;

public abstract class AbstractCommandState<T> implements CommandState {

	protected final Map<String, FtpRequest> requestMap;

	public AbstractCommandState(Map<String, FtpRequest> requestMap) {
		this.requestMap = requestMap;
	}

	@SuppressWarnings("unchecked")
	public T putRequest(String key, FtpRequest request) {
		requestMap.put(key, request);
		return (T) this;
	}

	public FtpRequest getRequest(String key) {
		return requestMap.get(key);
	}

}
