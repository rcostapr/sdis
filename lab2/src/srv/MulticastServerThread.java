package srv;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastServerThread extends Thread {
	// Server Port for Unicast
	public int port;
	// Server Port for Multicast
	public int multicastPort;
	//  IP address for Multicast
	public InetAddress multicastAddress;
	//  IP address for Unicast
	public InetAddress address;
	// Server Socket for communication
	public MulticastSocket multicastSocket;
	// Datagram packets to implement a connectionless packet delivery service
	public DatagramPacket serverInfo;

	public MulticastServerThread(String port, String multicastAddress,
			String multicastPort) throws UnknownHostException {
		
		this.port = Integer.parseInt(port);
		this.multicastPort = Integer.parseInt(multicastPort);
		this.multicastAddress = InetAddress.getByName(multicastAddress);
		this.address = InetAddress.getLocalHost();

		String msg = this.address.getHostAddress() + " " + this.port;

		serverInfo = new DatagramPacket(msg.getBytes(), msg.length(),
				this.multicastAddress, this.multicastPort);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			// Open socket for communication on multicastPort
			multicastSocket = new MulticastSocket(multicastPort);
			multicastSocket.setTimeToLive(1);

			while (true) {
				// Send Server INFO
				multicastSocket.send(serverInfo);
				
				System.out.println("Multicast: " + multicastAddress.getHostAddress() + " "
						+ multicastPort + ":" + address.getHostAddress() + " "
						+ this.port);
				Thread.sleep(1000);
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}