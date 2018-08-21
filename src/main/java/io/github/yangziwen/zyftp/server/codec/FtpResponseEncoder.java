package io.github.yangziwen.zyftp.server.codec;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.server.FtpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * The ftp response encoder
 *
 * @author yangziwen
 */
public class FtpResponseEncoder extends MessageToByteEncoder<FtpResponse> {

	@Override
	protected void encode(ChannelHandlerContext ctx, FtpResponse response, ByteBuf out) throws Exception {
		String line = String.valueOf(response.getCode());
		if (StringUtils.isNotBlank(response.getMessage())) {
			if ("FEAT".equals(response.getCommand())) {
				line += "-" + response.getMessage();
			} else {
				line += " " + response.getMessage();
			}
		}
		line += "\r\n";
		out.writeBytes(line.getBytes(CharsetUtil.UTF_8));
	}

}
