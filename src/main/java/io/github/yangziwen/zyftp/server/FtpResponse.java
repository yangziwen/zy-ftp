package io.github.yangziwen.zyftp.server;

import io.netty.channel.ChannelPromise;
import lombok.Data;

@Data
public class FtpResponse {
	
	public static final int CODE_200_COMMAND_OKAY = 200;
	
	public static final int CODE_220_SERVICE_READY = 220;
	
	public static final int CODE_221_CLOSING_CONTROL_CONNECTION = 221;
	
	public static final int CODE_502_COMMAND_NOT_IMPLEMENTED = 502;
	
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
	
	public void notifyFlushed() {
		if (flushedPromise != null) {
			flushedPromise.setSuccess();
		}
	}

}
