package io.github.yangziwen.zyftp.server;

import io.netty.channel.ChannelPromise;
import lombok.Data;

/**
 * The ftp response
 *
 * @author yangziwen
 */
@Data
public class FtpResponse {

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
