package io.github.yangziwen.zyftp.command.impl;

import java.net.InetSocketAddress;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.DataConnectionType;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpPassiveDataServer;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.util.concurrent.Promise;

public class PASV implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Promise<FtpResponse> executeAsync(FtpSession session, FtpRequest request) {
		Promise<Integer> portPromise = session.getServerContext().getPassivePorts().borrowPort();
		Promise<FtpResponse> promise = session.getContext().channel().eventLoop().newPromise();
		portPromise.addListener(f1 -> {
			int port = portPromise.get();
			FtpPassiveDataServer passiveDataServer = new FtpPassiveDataServer(session);
			passiveDataServer.start(port).addListener(f2 -> {
				InetSocketAddress address = (InetSocketAddress) passiveDataServer.getServerChannel().localAddress();
				String addrStr = encode(session, address);
				FtpResponse response = Command.createResponse(FtpReply.REPLY_227, "PASV", request, session, addrStr);
				session.setDataConnectionType(DataConnectionType.PASV);
				promise.setSuccess(response);
			});
		});
		return promise;
	}

    public static String encode(FtpSession session, InetSocketAddress address) {
        int servPort = address.getPort();
        return session.getServerConfig().getPassiveAddress().replaceAll("\\.", ",") + ","
        		+ (servPort >> 8) + "," + (servPort & 0xFF);
    }

}
