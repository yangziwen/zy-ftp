package io.github.yangziwen.zyftp.server;

import io.netty.util.concurrent.Promise;

/**
 * The ftp data connection
 * Could be a passive data server or a port data client
 *
 * @author yangziwen
 */
public interface FtpDataConnection {

	Promise<FtpDataConnection> writeAndFlushData(FtpDataWriter writer);

	Promise<Void> stop();

}
