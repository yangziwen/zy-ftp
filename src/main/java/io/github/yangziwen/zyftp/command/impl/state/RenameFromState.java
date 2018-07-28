package io.github.yangziwen.zyftp.command.impl.state;

import org.apache.commons.collections4.map.HashedMap;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class RenameFromState extends AbstractCommandState<RenameFromState> {

	public RenameFromState() {
		super(new HashedMap<>());
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		if ("RNTO".equals(request.getCommand())) {
			return new RenameToState(requestMap).putRequest("RNTO", request);
		}
		return OtherState.INSTANCE.transferTo(request);
	}

}
