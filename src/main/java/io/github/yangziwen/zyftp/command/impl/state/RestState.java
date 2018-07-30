package io.github.yangziwen.zyftp.command.impl.state;

import java.util.HashMap;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class RestState extends AbstractCommandState<RestState> {

	public RestState() {
		super(new HashMap<>());
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		if ("RETR".equals(request.getCommand())) {
			return new RetrState(requestMap).putRequest("RETR", request);
		}
		if ("STOR".equals(request.getCommand())) {
			return new StorState(requestMap).putRequest("STOR", request);
		}
		return OtherState.INSTANCE.transferTo(request);
	}

}
