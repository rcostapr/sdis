package backend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MulticastServer {

	private static int TTL = 1;
	private static int MAX_PACKET_SIZE  = 64000;

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
	
	public boolean sendMessage(byte[] messg) throws Exception {
		
		DatagramPacket packet = new DatagramPacket(messg, messg.length,
				multicastAddress, multicastPort);
		
		try {
			multicastSocket.setLoopbackMode(true);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		try {
			multicastSocket.send(packet);
		} catch (IOException e) {
			System.err.println("Failed to send packet");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public byte[] receiveMessage() throws Exception {

		if (multicastSocket == null) {
			throw new Exception();
		}

		byte[] buffer = new byte[MAX_PACKET_SIZE];

		DatagramPacket packet = new DatagramPacket(buffer, MAX_PACKET_SIZE);

		try {
			multicastSocket.receive(packet);
		} catch (IOException e) {
			System.err.println("Failed to send packet");
			e.printStackTrace();
			return null;
		}
		
		byte[] message = new byte[packet.getLength()];
		
		System.arraycopy(buffer, 0, message, 0, packet.getLength());

		return message;
	}

}