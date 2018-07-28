package io.github.yangziwen.zyftp.command.impl.state;

import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpSession;

public interface CommandState {

	CommandState transferTo(FtpRequest request);

	default CommandState transferTo(FtpRequest request, FtpSession session) {
		CommandState state = transferTo(request);
		session.setCommandState(state);
		return state;
	}

	FtpRequest getRequest(String key);

}
