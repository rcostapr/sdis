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

		String mcIP = null;
		int mcPort=81;

		String mdbIP= null;
		int mdbPort=82;

		String mdrIP= null;
		int mdrPort=83;


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
					mcPort = Integer.parseInt(arr[1]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			if (args[4].indexOf(':') > -1) { // <-- does it contain ":"?
				String[] arr = args[4].split(":");
				mdbIP = arr[0];
				try {
					mdbPort= Integer.parseInt(arr[1]);

				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if (args[5].indexOf(':') > -1) { // <-- does it contain ":"?
				String[] arr = args[5].split(":");
				mdrIP= arr[0];
				try {
					mdrPort= Integer.parseInt(arr[1]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			//Check if DB is initialized

			ConfigManager myConfig = ConfigManager.getConfigManager();
			myConfig.setMyID(Integer.parseInt(args[1]));
			Interface.getInstance().setAccessPoint(args[2]);

			if (myConfig.setAdresses(mcIP, mcPort, mdbIP, mdbPort, mdrIP, mdrPort)) {


				Interface.getInstance().startUp();

				//Interface.getInstance().backupFile("\\test.txt", 1);

			}
			while (true);
		}
	}
}