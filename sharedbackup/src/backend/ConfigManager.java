package backend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigManager {
	private static final String VERSION = "1.0";
	private MCListener mcListener;
	private MDRListener mdrListener;
	private MDBListener mdbListener;
	private ExecutorService mExecutorService = null;

	private InetAddress mcAddr = null, mdbAddr = null, mdrAddr = null;
	private int mMCport = 0, mMDBport = 0, mMDRport = 0;

	// static
	private static ConfigManager iConfigManager = null;

	private ConfigManager() {
		mcListener = null;
		mdrListener = null;
		mdbListener = null;
		mExecutorService = Executors.newFixedThreadPool(60);

	}

	public static ConfigManager getConfigManager() {
		if (iConfigManager == null) {
			iConfigManager = new ConfigManager();
		}
		return iConfigManager;
	}

	public boolean setAdresses(String mcIP, String mcPort, String mdbIP, String mdbPort, String mdrIP, String mdrPort) {

		try {
			this.mcAddr = InetAddress.getByName(mcIP);
			this.mdbAddr = InetAddress.getByName(mdbIP);
			this.mdrAddr = InetAddress.getByName(mdrIP);

			this.mMCport = Integer.parseInt(mcPort);
			this.mMDBport = Integer.parseInt(mdbPort);
			this.mMDRport = Integer.parseInt(mdrPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void startupListeners() {
		if (mcListener == null) {
			mcListener = MCListener.getInstance();
			mExecutorService.execute(mcListener);
		}
		if (mdbListener == null) {
			mdbListener = MDBListener.getInstance();
			mExecutorService.execute(mdbListener);
		}
		if (mdrListener == null) {
			mdrListener = MDRListener.getInstance();
			mExecutorService.execute(mdrListener);
		}
	}

	public InetAddress getMcAddr() {
		return mcAddr;
	}

	public InetAddress getMdbAddr() {
		return mdbAddr;
	}

	public InetAddress getMdrAddr() {
		return mdrAddr;
	}

	public int getmMCport() {
		return mMCport;
	}

	public int getmMDBport() {
		return mMDBport;
	}

	public int getmMDRport() {
		return mMDRport;
	}

	public Executor getExecutorService() {
		return mExecutorService;
	}

	public void addSavedChunk(Chunk chunk) {
		// TODO Auto-generated method stub
		
	}

}
