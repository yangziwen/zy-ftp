package io.github.yangziwen.zyftp.command.impl.state;

import java.util.Map;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class RenameToState extends AbstractCommandState<RenameToState> {

	public RenameToState(Map<String, FtpRequest> requestMap) {
		super(requestMap);
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		return OtherState.INSTANCE.transferTo(request);
	}

}
