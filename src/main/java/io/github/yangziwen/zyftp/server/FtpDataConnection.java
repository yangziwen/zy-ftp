package io.github.yangziwen.zyftp.server;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Promise;

/**
 * The ftp data connection
 * Could be a passive data server or a port data client
 *
 * @author yangziwen
 */
public interface FtpDataConnection {

	Promise<Void> writeAndFlushData(FtpDataWriter writer);

	Promise<Void> close();

	FtpSession getSession();

	ChannelFuture getCloseFuture();

	/**
	 * Returns the error that occurred during upload (receiving data from client),
	 * or null if the upload completed successfully or no upload happened on this connection.
	 */
	Throwable getUploadError();

}
