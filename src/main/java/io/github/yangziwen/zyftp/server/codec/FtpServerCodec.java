package io.github.yangziwen.zyftp.server.codec;

import io.netty.channel.CombinedChannelDuplexHandler;

/**
 * The ftp server codec
 * decode the request and encode the response
 *
 * @author yangziwen
 */
public class FtpServerCodec extends CombinedChannelDuplexHandler<FtpRequestDecoder, FtpResponseEncoder> {

	public FtpServerCodec() {
		super(new FtpRequestDecoder(), new FtpResponseEncoder());
	}

}
