package backend;

import java.net.InetAddress;

public class ConfigManager {
	private static final String VERSION = "1.0";
	private MCListener  mcListener;
	private MDBListener  mdrListener;
	private MDRListener mdbListener;

	private InetAddress mcAddr = null, mdbAddr = null, mdrAddr= null;
	private int mMCport = 0, mMDBport = 0, mMDRport = 0;


	// static
	private static ConfigManager iConfigManager = null;

	private ConfigManager(){
		mcListener=null;
		mdrListener=null;
		mdbListener= null;


	}

	public  static ConfigManager getConfigManager(){
		if(iConfigManager==null){
			iConfigManager = new ConfigManager();
		}
		return  iConfigManager;
	}


	public boolean setAdresses(String mcIP,String mcPort,String mdbIP,String mdbPort,String mcrIP,String mcrPort) {

		try {
			this.mcAddr= InetAddress.getByName(mcAddr);
			this.mdbAddr = InetAddress.getByName(mdbAddr);
			this.mdrAddr = InetAddress.getByName(mdrAddr);

			this.mMCport= int. mcPort;
			this.mMDBport = mdbPort;
			this.mMDRport = mdrPort;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
