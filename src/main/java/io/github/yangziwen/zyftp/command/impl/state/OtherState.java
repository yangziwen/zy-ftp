package io.github.yangziwen.zyftp.command.impl.state;

import java.util.Collections;

import io.github.yangziwen.zyftp.server.FtpRequest;

public class OtherState extends AbstractCommandState<OtherState> {

	public static final OtherState INSTANCE = new OtherState();

	private OtherState() {
		super(Collections.emptyMap());
	}

	@Override
	public CommandState transferTo(FtpRequest request) {
		if ("RNFR".equals(request.getCommand()) && request.hasArgument()) {
			return new RenameFromState().putRequest(request);
		}
		if ("REST".equals(request.getCommand()) && request.hasArgument()) {
			return new RestState().putRequest(request);
		}
		if ("STOR".equals(request.getCommand())) {
			return new StorState().putRequest(request);
		}
		if ("PORT".equals(request.getCommand())) {
			return new PortState();
		}
		if ("APPE".equals(request.getCommand())) {
			return new AppeState().putRequest(request);
		}
		return this;
	}

}
