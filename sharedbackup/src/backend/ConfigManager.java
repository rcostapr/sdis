package backend;

import sun.rmi.runtime.Log;
import utils.Database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigManager {
	private static final String VERSION = "1.0";
	private MCListener mcListener;



	private int myID;
	private MDRListener mdrListener;
	private MDBListener mdbListener;
	private ExecutorService mExecutorService = null;



	private String RMI_Object_Name = null;

	private boolean isDatabaseLoaded = false;
	private Database database = null;

	private InetAddress mcAddr = null, mdbAddr = null, mdrAddr = null;
	private int mMCport = 0, mMDBport = 0, mMDRport = 0;

	// static
	private static ConfigManager iConfigManager = null;

	private ConfigManager() {
		mcListener = null;
		mdrListener = null;
		mdbListener = null;
		mExecutorService = Executors.newFixedThreadPool(60);
		isDatabaseLoaded = loadDatabase();

	}

	public static ConfigManager getConfigManager() {
		if (iConfigManager == null) {
			iConfigManager = new ConfigManager();
		}
		return iConfigManager;
	}

	private boolean loadDatabase() {
		try {
			FileInputStream fileIn = new FileInputStream(Database.FILE);
			ObjectInputStream in = new ObjectInputStream(fileIn);

			try {
				database = (Database) in.readObject();
			} catch (ClassNotFoundException e) {


				database = new Database();
			}

			in.close();
			fileIn.close();

		} catch (FileNotFoundException e) {
			database = new Database();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void setDBDestination(String path)throws InvalidFolderException{
		database.setFolder(path);
	}

	public void setRMI_Object_Name(String nome){
		RMI_Object_Name = nome;
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

	public void startupListeners() {
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
	public void terminate (){
		mExecutorService.shutdown();
	}

	public int getMyID() {
		return myID;
	}

	public void setMyID(int myID) {
		this.myID = myID;
	}

	public Chunk getSavedChunk(String fileId, int chunkNo) {
		return database.getSavedChunk(fileId, chunkNo);
	}

	public void incChunkReplication(String fileId, int chunkNo)
			throws InvalidChunkException {
		database.incChunkReplication(fileId, chunkNo);
		database.saveDatabase();
	}

	public String getRMI_Object_Name() {
		return RMI_Object_Name;
	}

	public static class ConfigurationsNotInitializedException extends Exception {
	}

	public static class InvalidFolderException extends Exception {

	}

	public static class InvalidBackupSizeException extends Exception {

	}

	public static class InvalidChunkException extends Exception {


	}

	public static class FileAlreadySaved extends Exception {

	}
}
