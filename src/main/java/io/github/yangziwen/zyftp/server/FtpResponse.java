package io.github.yangziwen.zyftp.server;

import io.netty.channel.ChannelPromise;
import lombok.Data;

@Data
public class FtpResponse {
	
	public static final int REPLY_200_COMMAND_OKAY = 200;
	
	public static final int REPLY_220_SERVICE_READY = 220;
	
	public static final int REPLY_221_CLOSING_CONTROL_CONNECTION = 221;
	
	public static final int REPLY_230_USER_LOGGED_IN = 230;
	
    public static final int REPLY_331_USER_NAME_OKAY_NEED_PASSWORD = 331;
	
    public static final int REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION = 421;
	
	public static final int REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS = 501;
	
	public static final int REPLY_502_COMMAND_NOT_IMPLEMENTED = 502;
	
	public static final int REPLY_530_INVALID_USER = 530;
	
	private int code;
	
	private String message;
	
	private String basicMsg;
	
	private ChannelPromise flushedPromise;
	
	public FtpResponse(int code) {
		this.code = code;
	}
	
	public FtpResponse(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public FtpResponse flushedPromise(ChannelPromise promise) {
		this.flushedPromise = promise;
		return this;
	}
	
	public void notifyFlushed() {
		if (flushedPromise != null) {
			flushedPromise.setSuccess();
		}
	}

}
