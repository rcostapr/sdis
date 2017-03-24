package frontend;

import backend.ConfigManager;
import backend.SavedFile;

import java.io.File;

public class Launcher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String fileTosave = args[1];
		// int replication = Integer.parseInt(args[2]);

		String mcIP = "239.255.255.12";
		String mcPort = "25";

		String mdbIP = "239.255.255.13";
		String mdbPort = "26";

		String mcrIP = "239.255.255.14";
		String mcrPort = "27";

		ConfigManager myConfig = ConfigManager.getConfigManager();
		
		if (myConfig.setAdresses(mcIP, mcPort, mdbIP, mdbPort, mcrIP, mcrPort)) {

			Interface.getInstance().startUp();
			try {
				Interface.getInstance().backupFile("c:\\sdis\\test.txt", 2);
			} catch (SavedFile.FileTooLargeException ex) {

			} catch (SavedFile.FileDoesNotExistsException ex1) {
				System.out.println("File does not exist!");
			}
		}

		myConfig.terminate();

	}
}
