package io.github.yangziwen.zyftp.server.codec;

import io.github.yangziwen.zyftp.server.FtpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.CharsetUtil;

public class FtpRequestDecoder extends LineBasedFrameDecoder {

	public FtpRequestDecoder() {
		super(4096);
	}

	@Override
	protected FtpRequest decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
		if (frame == null) {
			return null;
		}
		String line = frame.toString(CharsetUtil.UTF_8);
		String[] arr = line.split(" ", 2);
		return new FtpRequest(arr[0].trim(), arr.length > 1 ? arr[1].trim() : null);
	}

}
