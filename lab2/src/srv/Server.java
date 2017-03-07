package srv;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import Vehicle.Vehicle;

public class Server {
	
	// Datagram packets to implement a connectionless packet delivery service
	public static DatagramSocket socket;

	// List of Vehicle register
	public static List<Vehicle> vehicles = new ArrayList<Vehicle>();

	public static void main(String[] args) throws Exception {
		// java Server <port> <multicastAddress> <multicastPort>
		if (args.length != 3)
			throw (new Exception());
		
		new MulticastServerThread(args[0],args[1],args[2]).start();
		
		int port = Integer.parseInt(args[0]);
		
		socket = new DatagramSocket(port);

		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		while (true) {
			socket.receive(packet);

			String msg = new String(packet.getData(), 0, packet.getLength());

			String data[] = msg.split(" ");

			String replyMsg;

			if (data[0].equals("REGISTER")) {
				if (getVehicleByPlate(data[1]) != null)
					replyMsg = "-1";
				else {
					Vehicle newVehicle = new Vehicle(data[1], data[2]);
					vehicles.add(newVehicle);
					replyMsg = Integer.toString(vehicles.size());
				}

			}

			else {
				try {
					replyMsg = getVehicleByPlate(data[1]).getName();
				} catch (NullPointerException e) {
					replyMsg = "NOT_FOUND";
				}
			}

			DatagramPacket reply = new DatagramPacket(replyMsg.getBytes(),
					replyMsg.length(), packet.getAddress(), packet.getPort());

			socket.send(reply);

			System.out.println(msg);
		}

	}
	
	/**
	 * Get Vehicle from a register plate
	 */
	public static Vehicle getVehicleByPlate(String plate) {
		for (int i = 0; i < vehicles.size(); i++)
			if (vehicles.get(i).getPlate().equals(plate))
				return vehicles.get(i);

		return null;
	}
}