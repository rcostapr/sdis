package srv;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastServerThread extends Thread {
	public int port;
	public int multicast_port;
	public InetAddress group;
	public InetAddress address;
	public MulticastSocket multicast_socket;
	public DatagramPacket serverInfo;

	public MulticastServerThread(String port, String group,
			String multicast_port) throws UnknownHostException {
		this.port = Integer.parseInt(port);
		this.multicast_port = Integer.parseInt(multicast_port);
		this.group = InetAddress.getByName(group);
		this.address = InetAddress.getLocalHost();

		String msg = this.address.getHostAddress() + " " + this.port;

		serverInfo = new DatagramPacket(msg.getBytes(), msg.length(),
				this.group, this.multicast_port);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			multicast_socket = new MulticastSocket(multicast_port);
			multicast_socket.setTimeToLive(1);

			while (true) {
				multicast_socket.send(serverInfo);
				System.out.println("multicast: " + group.getHostAddress() + " "
						+ multicast_port + ":" + address.getHostAddress() + " "
						+ this.port);
				Thread.sleep(1000);
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}