package io.github.yangziwen.zyftp.command;

import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public interface Command {
	
	public FtpResponse execute(FtpSession session, FtpRequest request);

}
