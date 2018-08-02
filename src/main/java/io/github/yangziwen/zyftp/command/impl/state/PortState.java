package io.github.yangziwen.zyftp.command.impl.state;

import java.util.Collections;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class PortState extends AbstractCommandState<PortState> {

	public PortState() {
		super(Collections.emptyMap());
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		return OtherState.INSTANCE.transferTo(request);
	}

}
