package backend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastServer {

	private static int TTL = 1;

	// Server Port for Multicast
	public int multicastPort;
	// IP address for Multicast
	public InetAddress multicastAddress;

	// Server Socket for communication
	public MulticastSocket multicastSocket;

	// Datagram packets to implement a connectionless packet delivery service
	public DatagramPacket serverInfo;

	public MulticastServer(String multicastAddress, String multicastPort) throws UnknownHostException {

		this.multicastPort = Integer.parseInt(multicastPort);
		this.multicastAddress = InetAddress.getByName(multicastAddress);
		this.multicastSocket = null;
	}

	public void join() {
		try {
			multicastSocket = new MulticastSocket(multicastPort);
			multicastSocket.setTimeToLive(TTL);

		} catch (IOException e) {
			
			e.printStackTrace();
		}

		try {
			multicastSocket.joinGroup(multicastAddress);

		} catch (IOException e) {
			
			e.printStackTrace();

		}
	}

}