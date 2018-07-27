package io.github.yangziwen.zyftp.command.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.github.yangziwen.zyftp.command.Command;
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
				String addrStr = encode(address);
				FtpResponse response = Command.createResponse(FtpResponse.REPLY_227_ENTERING_PASSIVE_MODE, "PASV", request, session, addrStr);
				promise.setSuccess(response);
			});
		});
		return promise;
	}

    public static String encode(InetSocketAddress address) {
        InetAddress servAddr = address.getAddress();
        int servPort = address.getPort();
        return servAddr.getHostAddress().replace('.', ',') + ','
                + (servPort >> 8) + ',' + (servPort & 0xFF);
    }

}
