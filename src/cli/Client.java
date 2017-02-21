package cli;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Client {

	private static DatagramSocket socket;
	private static DatagramPacket recPacket;

	private static ArrayList<String> registerList;

	private static InetAddress serverAddr;
	
	private static byte[] ipAddr;

	public static void main(String[] args){
		
		if(!checkInput(args))return;
		createMessage(args);
		sendPackets(args);
	}

	
	private static void sendPackets(String args[]) {
		try {
			socket = new DatagramSocket();
			
			ipAddr = new byte[]{127, 0, 0, 1};
			serverAddr = InetAddress.getByAddress(ipAddr);
			
			if (args[1].equals("REGISTER")) {
				String str = "REGISTER ";
				for (int i = 0; i < registerList.size(); i++) {
					if (i < (registerList.size()-1))
						str = str.concat(registerList.get(i)).concat(" ");
					else
						str = str.concat(registerList.get(i));
				}
				System.out.println("Message: " + str);
				byte[] msg = str.getBytes();
				System.out.println("Connecting: " + serverAddr.getHostName() + " on port: " + Integer.valueOf(args[0]));
				DatagramPacket p = new DatagramPacket(msg, msg.length, serverAddr,
						Integer.valueOf(args[0]));
				socket.send(p);
				System.out.println("Sended: " + msg.length + " bytes");
				byte[] buffer = new byte[256];
				DatagramPacket recPacket = new DatagramPacket(buffer,
						buffer.length);
				socket.receive(recPacket);

			String receivedPacket = new String(recPacket.getData(), 0, recPacket.getLength());
			System.out.println(receivedPacket);
			
			} else if (args[1].equals("LOOKUP")){
				String str = "LOOKUP ".concat(args[2]);
				System.out.println("Client "+str);
				byte[] msg = str.getBytes();
				System.out.println("Connecting: " + serverAddr.getHostName() + " on port: " + Integer.valueOf(args[0]));
				DatagramPacket p = new DatagramPacket(msg, msg.length, serverAddr, Integer.valueOf(args[0]));
				socket.send(p);
				System.out.println("Sended: " + msg.length + " bytes");

				byte[] buffer = new byte[256];
				recPacket = new DatagramPacket(buffer, buffer.length);
				System.out.println("Waiting reponse...");
				socket.receive(recPacket);

			String receivedPacket = new String(recPacket.getData(), 0, recPacket.getLength());
			System.out.println(receivedPacket);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private static void createMessage(String data[]) {

		if( data[1].equals("REGISTER")) {
			registerList = new ArrayList<String>();

			for ( int i = 2; i < data.length; i++) {
				registerList.add(data[i]);
			}	
		}
		
	}

	private static boolean checkInput(String args[]) {
		int length = args.length;
		if(length<3){
			usage();
			return false;
		} else {
			if(args[1].equals("REGISTER") && length>=4) {
				return true;
			}
			if (args[1].equals("LOOKUP") && length==3) {
				return true;
			}
		}
		return false;
	}

	private static void usage() {
		System.out.println("Usage: Client <port> <REGISTER|LOOKUP> <Plate> <name>");
	}

}
