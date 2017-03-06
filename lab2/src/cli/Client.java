package cli;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {

	public static DatagramSocket socket;
	public static MulticastSocket multicastSocket;

	public static void main(String[] args) throws Exception {
		// java Client <multicast_addr> <multicastPort> <oper> <opnd>*
		// <plate number> <owner name>, for register
		// <plate number>, for lookup
		if (args.length < 4)
			throw (new Exception());

		int multicastPort = Integer.parseInt(args[1]);
		InetAddress group = InetAddress.getByName(args[0]);
		
		multicastSocket = new MulticastSocket(multicastPort);
		socket = new DatagramSocket(); //unicast
		
		multicastSocket.joinGroup(group);
		
		byte[] buf = new byte[256];
		DatagramPacket serverInfo = new DatagramPacket(buf, buf.length);
		
		//IP PORT
		multicastSocket.receive(serverInfo);
		
		String msg = new String(serverInfo.getData(), 0, serverInfo.getLength());

		String data[] = msg.split(" ");	
		
		InetAddress address = InetAddress.getByName(data[0]);
		int port = Integer.parseInt(data[1]);
		
		if (args[2].toLowerCase().equals("REGISTER")) {
			if (args.length != 5)
				throw (new Exception());
			msg = "Register " + args[3] + " " + args[4];
			DatagramPacket packet = new DatagramPacket(msg.getBytes(),
					msg.length(), address, port);
			socket.send(packet);
		} else if (args[2].toLowerCase().equals("LOOKUP")) {
			msg = "Lookup " + args[3];
			DatagramPacket packet = new DatagramPacket(msg.getBytes(),
					msg.length(), address, port);
			socket.send(packet);
		} else
			throw (new Exception());

		buf = new byte[256];
		DatagramPacket reply = new DatagramPacket(buf, buf.length);

		socket.receive(reply);
		String replyMsg = new String(reply.getData());
		System.out.println(replyMsg);
	}
}