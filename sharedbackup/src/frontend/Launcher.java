package frontend;

import backend.ConfigManager;

public class Launcher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileTosave = args[1];
		int replication = Integer.parseInt(args[2]);
		
		String mcIP ="";
		String mcPort ="";
		
		String mdbIP="";
		String mdbPort="";
		
		String mcrIP="";
		String mcrPort="";
		
		ConfigManager myConfig = ConfigManager.getConfigManager();
		
		if (myConfig.setAdresses(mcIP,mcPort,mdbIP,mdbPort,mcrIP,mcrPort)) {

		    //Interface.getInstance().backupFile()
		}
	}


}
