package backend;

import utils.Database;
import utils.SharedDatabase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import protocols.MasterPeer;
import protocols.SharedClock;

public class ConfigManager {
	private MCListener mcListener;

	private int myID;
	private boolean server = false;
	private MDRListener mdrListener;
	private MDBListener mdbListener;
	private ExecutorService mExecutorService = null;

	private String RMI_Object_Name = null;

	private boolean databaseLoaded;
	
	private Database database = null;
	private SharedDatabase sharedDatabase = null;
	private Random random;
	private InetAddress mcAddr = null, mdbAddr = null, mdrAddr = null;
	private int mMCport = 0, mMDBport = 0, mMDRport = 0;
	private boolean isRunning;
	private long startTime;
	private User user;

	// static
	private static ConfigManager iConfigManager = null;

	private ConfigManager() {
		mcListener = null;
		mdrListener = null;
		mdbListener = null;
		mExecutorService = Executors.newFixedThreadPool(60);
		setDatabaseLoaded(loadDatabase());
		random = new Random();
		isRunning = true;

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

				System.out.println("Starting new DB");
				in.close();
				fileIn.close();
				database = new Database();
				return false;
			}
			System.out.println("== DB already exists ==");
			System.out.println("===== Saved Files =====");
			database.printSavedFiles();
			System.out.println("=======================");
			in.close();
			fileIn.close();

		} catch (FileNotFoundException e) {

			System.out.println("Starting new DB");
			database = new Database();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void setRMI_Object_Name(String nome) {
		RMI_Object_Name = nome;
	}

	public boolean setAdresses(String mcIP, int mcPort, String mdbIP, int mdbPort, String mdrIP, int mdrPort) {

		try {
			this.mcAddr = InetAddress.getByName(mcIP);
			this.mdbAddr = InetAddress.getByName(mdbIP);
			this.mdrAddr = InetAddress.getByName(mdrIP);
			System.out.println("mcAddr = " + mdrAddr);
			this.mMCport = mcPort;
			this.mMDBport = mdbPort;
			this.mMDRport = mdrPort;
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
		database.addChunk(chunk);
		saveDB();
	}

	public void terminate() {
		mExecutorService.shutdown();
		isRunning = false;
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

	public synchronized void incChunkReplication(String fileId, int chunkNo) throws InvalidChunkException {
		database.incChunkReplication(fileId, chunkNo);
		database.saveDatabase();
	}

	public synchronized void decChunkReplication(String fileId, int chunkNo) {
		database.decChunkReplication(fileId, chunkNo);
		database.saveDatabase();
	}

	public void removeChunk(Chunk chunk) {
		database.removeChunk(chunk);
		saveDB();
	}

	public void saveDB() {
		database.saveDatabase();
	}

	public SavedFile getNewSavedFile(String path, int replication) throws SavedFile.FileTooLargeException, FileAlreadySaved, SavedFile.FileDoesNotExistsException {
		return database.getNewSavedFile(path, replication);
	}

	public SavedFile getFileByPath(String path) {
		return database.getFileByPath(path);
	}

	public String getRMI_Object_Name() {
		return RMI_Object_Name;
	}

	public void printState() {

		database.print();
	}

	public void deleteFile(String fileID) {
		database.deleteChunksFile(fileID);
		saveDB();
	}

	public void removeFile(String filePath) {
		database.removeSavedFile(filePath);
		saveDB();
	}

	public long getUsedSpace() {
		return database.getUsedSpace();
	}

	public long getMaxSpace() {
		return database.getMaxBackupSize();
	}

	public void setMaxSpace(long maxSpace) {

		database.setAvailSpace(maxSpace);
		saveDB();
	}

	public Chunk getNextRemovableChunk() {
		List<Chunk> savedChunks = database.getSavedChunks();

		for (Chunk chunk : savedChunks) {
			if (chunk.getCurrentReplicationDegree() > chunk.getWantedReplicationDegree()) {
				return chunk;
			}
		}

		Chunk randomChunk = null;

		do {
			randomChunk = savedChunks.get(random.nextInt(savedChunks.size()));
		} while (randomChunk.getCurrentReplicationDegree() <= 0);

		return randomChunk;
	}

	public static class ConfigurationsNotInitializedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	public static class InvalidFolderException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	public static class InvalidBackupSizeException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	public static class InvalidChunkException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	public static class FileAlreadySaved extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	public void removeSavedFile(String filePath) {
		database.removeSavedFile(filePath);
		database.saveDatabase();
	}

	public boolean isAppRunning() {
		return isRunning;
	}

	public void decDeletedFileReplication(String fileID) {
		database.decDeletedFileCount(fileID);
		database.saveDatabase();

	}

	public String getInterfaceIP() throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if (netint.getDisplayName().equals(database.getInterfaceName())) {
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					try {
						Inet4Address addr = (Inet4Address) inetAddress;
						return addr.getHostAddress();
					} catch (ClassCastException e) {
					}
				}
			}
		}
		return null;
	}

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public void enterMainStage() throws ConfigurationsNotInitializedException {
		if (database.isLoaded()) {
			mExecutorService.execute(new FileDeletionChecker());
			Date d = new Date();
			startTime = d.getTime();
			if (!MasterPeer.getInstance().imMaster()) {
				SharedClock.getInstance().startSync();
			}
			sharedDatabase.createNameSpace(getChunksDestination());
		} else {
			throw new ConfigurationsNotInitializedException();
		}
		sharedDatabase.saveDatabase();
	}
	
	public long getUpTime() {
        Date d = new Date();
        return d.getTime() - startTime;
    }

	private String getChunksDestination() {
		return database.getFolder();
	}

	public SharedDatabase getSharedDatabase() {
		return sharedDatabase;
	}

	public ArrayList<String> getDeletedFiles() {
		return database.getDeletedFiles();
	}

	public boolean isDatabaseLoaded() {
		return databaseLoaded;
	}

	public void setDatabaseLoaded(boolean databaseLoaded) {
		this.databaseLoaded = databaseLoaded;
	}
	
	public boolean login(String username, String password) {
        return (user = SharedDatabase.login(username, password)) != null;
    }
}
