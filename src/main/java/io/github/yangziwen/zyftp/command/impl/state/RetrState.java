package io.github.yangziwen.zyftp.command.impl.state;

import java.util.Map;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class RetrState extends AbstractCommandState<RetrState> {

	public RetrState(Map<String, FtpRequest> requestMap) {
		super(requestMap);
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		return null;
	}

}
