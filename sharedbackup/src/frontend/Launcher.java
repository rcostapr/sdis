package frontend;

import backend.ConfigManager;

import java.io.File;
import java.net.*;

public class Launcher {

	public static void main(String[] args) {
		//args:
		//Protocol Version ,server id, server AP , MC:Port, MDB:Port, MDR:port

		// TODO Auto-generated method stub
		// String fileTosave = args[1];
		// int replication = Integer.parseInt(args[2]);
		File program;
		String programName="";

		try {
			System.out.println(java.net.InetAddress.getLocalHost());
		} catch (java.net.UnknownHostException e) {
			e.printStackTrace();
		}

		String mcIP = null;
		String mcPort= null;

		String mdbIP= null;
		String mdbPort= null;

		String mcrIP= null;
		String mcrPort= null;

		try {
			program = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

			 programName= program.getName();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (args.length != 6) {
			System.out.println("usage: " + programName +" Protocol_Version Server_ID Server_AP MC_Address:MC_Port MDB_Address:MDB_Port MDR_Address:MDR_Port");

		} else {
			//TODO: validate inputs

			if (args[3].indexOf(':') > -1) { // <-- does it contain ":"?
				String[] arr = args[3].split(":");
				mcIP = arr[0];
				try {
					mcPort = arr[1];
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			if (args[4].indexOf(':') > -1) { // <-- does it contain ":"?
				String[] arr = args[4].split(":");
				mdbIP = arr[0];
				try {
					mdbPort = arr[1];
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if (args[5].indexOf(':') > -1) { // <-- does it contain ":"?
				String[] arr = args[5].split(":");
				mcrIP = arr[0];
				try {
					mcrPort = arr[1];
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			ConfigManager myConfig = ConfigManager.getConfigManager();

			System.out.println("args[2] = " + args[2]);
			System.out.println("args[1] = " + args[1]);
			Interface.getInstance().setAccessPoint(args[2]);

			if (myConfig.setAdresses(mcIP, mcPort, mdbIP, mdbPort, mcrIP, mcrPort)) {

				Interface.getInstance().startUp();
				//Interface.getInstance().backupFile("\\test.txt", 2);

			}
		}
	}
}