package io.github.yangziwen.zyftp.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;

public class ResponseMessageVariableReplacer {

	public static final String OUTPUT_CODE = "output.code";

	public static final String OUTPUT_MSG = "output.msg";

	public static final String REQUEST_ARG = "request.arg";

	public static final String REQUEST_CMD = "request.cmd";

	public static final String REQUEST_LINE = "request.line";

	public static final String SERVER_IP = "server.ip";

	public static final String SERVER_PORT = "server.port";

	private ResponseMessageVariableReplacer() {}

	public static FtpResponse replaceVariables(int code, String subId, FtpRequest request, FtpResponse response,
			FtpSession session) {
		String message = response.getMessage();
		if (StringUtils.isBlank(message)) {
			return response;
		}
		int startIndex = 0;
		int openIndex = message.indexOf('{', startIndex);
		if (openIndex == -1) {
			return response;
		}

		int closeIndex = message.indexOf('}', startIndex);
		if ((closeIndex == -1) || (openIndex > closeIndex)) {
			return response;
		}

		StringBuilder sb = new StringBuilder(128);
		sb.append(message.substring(startIndex, openIndex));
		while (true) {
			String varName = message.substring(openIndex + 1, closeIndex);
			sb.append(getVariableValue(session, request, code, response.getBasicMsg(), varName));

			startIndex = closeIndex + 1;
			openIndex = message.indexOf('{', startIndex);
			if (openIndex == -1) {
				sb.append(message.substring(startIndex));
				break;
			}

			closeIndex = message.indexOf('}', startIndex);
			if ((closeIndex == -1) || (openIndex > closeIndex)) {
				sb.append(message.substring(startIndex));
				break;
			}
			sb.append(message.substring(startIndex, openIndex));
		}
		response.setMessage(sb.toString());
		return response;
	}

	private static String getVariableValue(FtpSession session, FtpRequest request, int code, String basicMsg,
			String varName) {

		String varVal = null;

		// all output variables
		if (varName.startsWith("output.")) {
			varVal = getOutputVariableValue(session, code, basicMsg, varName);
		}

		// all server variables
		else if (varName.startsWith("server.")) {
			varVal = getServerVariableValue(session, varName);
		}

		// all request variables
		else if (varName.startsWith("request.")) {
			varVal = getRequestVariableValue(session, request, varName);
		}

		// TODO
		// all statistical variables
		// else if (varName.startsWith("stat.")) {
		// varVal = getStatisticalVariableValue(session, context, varName);
		// }

		// TODO
		// all client variables
		// else if (varName.startsWith("client.")) {
		// varVal = getClientVariableValue(session, varName);
		// }

		if (varVal == null) {
			varVal = "";
		}
		return varVal;
	}

	/**
	 * Get output variable value.
	 */
	private static String getOutputVariableValue(FtpSession session, int code, String basicMsg, String varName) {

		if (varName.equals(OUTPUT_CODE)) {
			return String.valueOf(code);
		}

		else if (varName.equals(OUTPUT_MSG)) {
			if (basicMsg == null) {
				return "{" + varName + "}";
			}
			return basicMsg;
		}

		return null;
	}

	/**
	 * Get server variable value.
	 */
	private static String getServerVariableValue(FtpSession session, String varName) {

		String varVal = null;

		SocketAddress localSocketAddress = session.getServerContext().getServerConfig().getLocalAddress();

		if (localSocketAddress instanceof InetSocketAddress) {
			InetSocketAddress localInetSocketAddress = (InetSocketAddress) localSocketAddress;
			// server address
			if (varName.equals(SERVER_IP)) {

				InetAddress addr = localInetSocketAddress.getAddress();

				if (addr != null) {
					varVal = addr.getHostAddress();
				}
			}

			// server port
			else if (varName.equals(SERVER_PORT)) {
				varVal = String.valueOf(localInetSocketAddress.getPort());
			}
		}

		return varVal;
	}

	/**
	 * Get request variable value.
	 */
	private static String getRequestVariableValue(FtpSession session, FtpRequest request, String varName) {
		if (request == null) {
			return null;
		}

		if (varName.equals(REQUEST_LINE)) {
			return request.getRequestLine();
		}

		if (varName.equals(REQUEST_CMD)) {
			return request.getCommand();
		}

		if (varName.equals(REQUEST_ARG)) {
			return request.getArgument();
		}

		return null;
	}

}
