package io.github.yangziwen.zyftp.command.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.DataConnectionType;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpPortDataClient;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class PORT implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "PORT", session);
		}
		InetSocketAddress address = null;
		try {
			address = decode(request.getArgument());
		} catch (Exception e) {
			return Command.createResponse(FtpReply.REPLY_501, "PORT", session);
		}
		session.addPortDataClient(new FtpPortDataClient(session, address));
		session.setDataConnectionType(DataConnectionType.PORT);
		return Command.createResponse(FtpReply.REPLY_200, "PORT", session);
	}

	public static InetSocketAddress decode(String str)
            throws UnknownHostException {
        StringTokenizer st = new StringTokenizer(str, ",");
        if (st.countTokens() != 6) {
            throw new IllegalStateException("Illegal amount of tokens");
        }

        StringBuilder sb = new StringBuilder();
        try {
            sb.append(convertAndValidateNumber(st.nextToken()));
            sb.append('.');
            sb.append(convertAndValidateNumber(st.nextToken()));
            sb.append('.');
            sb.append(convertAndValidateNumber(st.nextToken()));
            sb.append('.');
            sb.append(convertAndValidateNumber(st.nextToken()));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e.getMessage());
        }

        InetAddress dataAddr = InetAddress.getByName(sb.toString());

        // get data server port
        int dataPort = 0;
        try {
            int hi = convertAndValidateNumber(st.nextToken());
            int lo = convertAndValidateNumber(st.nextToken());
            dataPort = (hi << 8) | lo;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid data port: " + str);
        }

        return new InetSocketAddress(dataAddr, dataPort);
    }

    private static int convertAndValidateNumber(String s) {
        int i = Integer.parseInt(s);
        if (i < 0) {
            throw new IllegalArgumentException("Token can not be less than 0");
        } else if (i > 255) {
            throw new IllegalArgumentException(
                    "Token can not be larger than 255");
        }

        return i;
    }

}
