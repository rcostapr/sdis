package frontend;

import backend.ConfigManager;

public class sb {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileTosave = args[1];
		
		String mcIP ="";
		String mcPort ="";
		
		String mdbIP="";
		String mdbPort="";
		
		String mcrIP="";
		String mcrPort="";
		
		ConfigManager myConfig;
		
		if (myConfig.openCommunication(mcIP,mcPort,mdbIP,mdbPort,mcrIP,mcrPort))		
		putChunk(fileTosave);

	}

}
