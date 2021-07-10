package io.github.yangziwen.zyftp.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

/**
 * The passive ports class
 * allocate random ports for passive data servers
 *
 * @author yangziwen
 */
public class PassivePorts {

	private static final int MAX_PORT = 65535;

	private static final Integer MAX_PORT_INTEGER = Integer.valueOf(MAX_PORT);

	private List<Integer> freeList;

	private Set<Integer> usedList;

	private Random r = new Random();

	private String passivePortsString;

	private DefaultEventExecutor executor = new DefaultEventExecutor();

	public PassivePorts(Set<Integer> passivePorts) {
		if (passivePorts == null) {
			throw new NullPointerException("passivePorts can not be null");
		} else if (passivePorts.isEmpty()) {
			passivePorts = new HashSet<Integer>();
			passivePorts.add(0);
		}
		this.freeList = new ArrayList<Integer>(passivePorts);
		this.usedList = new HashSet<Integer>(passivePorts.size());
	}

	public PassivePorts(final String passivePorts) {
		this(parse(passivePorts));
		this.passivePortsString = passivePorts;
	}

	public String getPassivePortsString() {
		return passivePortsString;
	}

	public Promise<Integer> borrowPort() {
		Promise<Integer> promise = executor.newPromise();
		if (executor.inEventLoop()) {
			promise.setSuccess(doBorrowPort());
		} else {
			executor.execute(() -> promise.setSuccess(doBorrowPort()));
		}
		return promise;
	}

	private Integer doBorrowPort() {
		List<Integer> freeCopy = new ArrayList<Integer>(freeList);
		while (freeCopy.size() > 0) {
			int index = r.nextInt(freeCopy.size());
			Integer port = freeCopy.get(index);
			if (checkPortUnbound(port)) {
				freeList.remove(index);
				usedList.add(port);
				return port;
			} else {
				freeCopy.remove(index);
			}
		}
		return -1;
	}

	public Promise<Void> returnPort(Integer port) {
		Promise<Void> promise = executor.newPromise();
		if (executor.inEventLoop()) {
			doReturnPort(port);
			promise.setSuccess(null);
		} else {
			executor.execute(() -> {
				doReturnPort(port);
				promise.setSuccess(null);
			});
		}
		return promise;
	}

	private void doReturnPort(Integer port) {
		usedList.remove(port);
		freeList.add(port);
	}

	/**
	 * Parse a string containing passive ports
	 *
	 * @param portsString
	 *			A string of passive ports, can contain a single port (as an
	 *			integer), multiple ports seperated by commas (e.g.
	 *			123,124,125) or ranges of ports, including open ended ranges
	 *			(e.g. 123-125, 30000-, -1023). Combinations for single ports
	 *			and ranges is also supported.
	 * @return A list of Integer objects, based on the parsed string
	 * @throws IllegalArgumentException
	 *			 If any of of the ports in the string is invalid (e.g. not an
	 *			 integer or too large for a port number)
	 */
	private static Set<Integer> parse(final String portsString) {
		Set<Integer> passivePortsList = new HashSet<Integer>();

		boolean inRange = false;
		Integer lastPort = Integer.valueOf(1);
		StringTokenizer st = new StringTokenizer(portsString, ",;-", true);
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();

			if (",".equals(token) || ";".equals(token)) {
				if (inRange) {
					fillRange(passivePortsList, lastPort, MAX_PORT_INTEGER);
				}

				// reset state
				lastPort = Integer.valueOf(1);
				inRange = false;
			} else if ("-".equals(token)) {
				inRange = true;
			} else if (token.length() == 0) {
				// ignore whitespace
			} else {
				Integer port = Integer.valueOf(token);

				verifyPort(port);

				if (inRange) {
					// add all numbers from last int
					fillRange(passivePortsList, lastPort, port);

					inRange = false;
				}

				addPort(passivePortsList, port);

				lastPort = port;
			}
		}

		if (inRange) {
			fillRange(passivePortsList, lastPort, MAX_PORT_INTEGER);
		}

		return passivePortsList;
	}

	/**
	 * Fill a range of ports
	 */
	private static void fillRange(final Set<Integer> passivePortsList, final Integer beginPort, final Integer endPort) {
		for (int i = beginPort; i <= endPort; i++) {
			addPort(passivePortsList, Integer.valueOf(i));
		}
	}

	/**
	 * Add a single port if not already in list
	 */
	private static void addPort(final Set<Integer> passivePortsList, final Integer port) {
		passivePortsList.add(port);
	}

	/**
	 * Verify that the port is within the range of allowed ports
	 */
	private static void verifyPort(final int port) {
		if (port < 0) {
			throw new IllegalArgumentException("Port can not be negative: " + port);
		} else if (port > MAX_PORT) {
			throw new IllegalArgumentException("Port too large: " + port);
		}
	}

	public Future<?> destroy() {
		return executor.shutdownGracefully();
	}

	/**
	 * Checks that the port of not bound by another application
	 */
	private boolean checkPortUnbound(int port) {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
			socket.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			// port probably in use, check next
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// could not close, check next
					return false;
				}
			}
		}
	}

}
