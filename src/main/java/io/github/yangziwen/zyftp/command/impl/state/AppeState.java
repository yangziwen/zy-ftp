package io.github.yangziwen.zyftp.command.impl.state;

import java.util.HashMap;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class AppeState extends AbstractCommandState<AppeState> {

	public AppeState() {
		super(new HashMap<>());
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		return OtherState.INSTANCE.transferTo(request);
	}

}
