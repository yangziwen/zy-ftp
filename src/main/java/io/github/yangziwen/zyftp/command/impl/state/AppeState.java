package io.github.yangziwen.zyftp.command.impl.state;

import java.util.HashMap;
import java.util.Map;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class AppeState extends AbstractCommandState<AppeState> {

	public AppeState() {
		this(new HashMap<>());
	}

	public AppeState(Map<String, FtpRequest> requestMap) {
		super(requestMap);
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		return OtherState.INSTANCE.transferTo(request);
	}

}
