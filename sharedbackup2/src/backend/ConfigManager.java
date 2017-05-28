package backend;

import utils.Database;
import utils.SharedDatabase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private boolean sharedDatabaseLoaded;

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
		setSharedDatabaseLoaded(loadSharedDatabase());
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
			FileInputStream databaseFile = new FileInputStream(Database.FILE);
			ObjectInputStream input = new ObjectInputStream(databaseFile);

			try {

				database = (Database) input.readObject();

			} catch (ClassNotFoundException e) {

				Path currentRelativePath = Paths.get(Database.FILE);
				Files.delete(currentRelativePath);

				System.out.println("++++ Saved DB Incompatible ++++");
				System.out.println("++++ Starting new DB ++++");
				input.close();
				databaseFile.close();
				database = new Database();
				database.setLoaded(true);
				return true;
			} catch (InvalidClassException e) {
				Path currentRelativePath = Paths.get(Database.FILE);
				Files.delete(currentRelativePath);

				System.out.println("++++ Saved DB Incompatible ++++");
				System.out.println("++++ Starting new DB ++++");
				input.close();
				databaseFile.close();
				database = new Database();
				database.setLoaded(true);
				return true;
			}
			database.setLoaded(true);
			System.out.println("== DB already exists ==");
			System.out.println("===== Saved Files =====");
			database.printSavedFiles();
			System.out.println("=======================");
			input.close();
			databaseFile.close();

		} catch (FileNotFoundException e) {

			System.out.println("== Starting new DB ==");
			database = new Database();
			database.setLoaded(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
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
		sharedDatabase.saveDatabase();
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

		if(user.getUserName().equals("admin")){
			database.print();
			sharedDatabase.print();
		} else {
			sharedDatabase.print(user.getUserName());
		}
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public void deleteFile(String fileID) {
		database.deleteChunksFile(fileID);
		saveDB();
	}

	public void removeFile(String filePath) {
		System.out.println("removeFile - " + filePath);
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

	public void decChunkDeletedFileReplication(String fileID, String chunkNo) {
		String chunkFileId = fileID + chunkNo;
		long chunkNof = Long.parseLong(chunkNo);
		database.decDeletedChunkFileCount(chunkFileId, chunkNof);
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

	public void startClockSync() throws ConfigurationsNotInitializedException {
		if (database.isLoaded()) {
			mExecutorService.execute(new FileDeletionChecker());
			Date d = new Date();
			startTime = d.getTime();
			if (!MasterPeer.getInstance().imMaster()) {
				SharedClock.getInstance().startSync();
			}
		} else {
			throw new ConfigurationsNotInitializedException();
		}

		database.saveDatabase();
	}

	public long getUpTime() {
		Date d = new Date();
		return d.getTime() - startTime;
	}

	public SharedDatabase getSharedDatabase() {
		return sharedDatabase;
	}

	public ArrayList<Chunk> getDeletedChunkFiles() {
		return database.getDeletedChunkFiles();
	}

	public boolean isDatabaseLoaded() {
		return databaseLoaded;
	}

	public void setDatabaseLoaded(boolean databaseLoaded) {
		this.databaseLoaded = databaseLoaded;
	}

	public boolean login(String username, String password) {
		User loggedUser = getSharedDatabase().login(username, password);
		if (loggedUser != null) {
			user = loggedUser;
			return true;
		}
		return false;

	}

	public User getUser() {
		return user;
	}

	public User setUser(User user) {
		this.user = user;
		return user;
	}

	public boolean userExists(String user) {
		if (sharedDatabase.userExists(user)) {
			return true;
		}
		return false;
	}

	public InetAddress getInterface() {
		try {
			return database.getInterface();
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setInterface() throws IOException {
		NetworkInterface selectedInterface = null;
		// iterate over the network interfaces known to java
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		OUTER: for (NetworkInterface interface_ : Collections.list(interfaces)) {
			// we shouldn't care about loopback addresses
			if (interface_.isLoopback())
				continue;

			// if you don't expect the interface to be up you can skip this
			// though it would question the usability of the rest of the code
			if (!interface_.isUp())
				continue;

			// iterate over the addresses associated with the interface
			Enumeration<InetAddress> addresses = interface_.getInetAddresses();
			for (InetAddress address : Collections.list(addresses)) {
				// look only for ipv4 addresses
				if (address instanceof Inet6Address)
					continue;

				// use a timeout big enough for your needs
				if (!address.isReachable(3000))
					continue;

				// we close the socket immediately after use
				try (SocketChannel socket = SocketChannel.open()) {
					// again, use a big enough timeout
					socket.socket().setSoTimeout(3000);

					// bind the socket to your local interface
					socket.bind(new InetSocketAddress(address, 8080));

					// try to connect to *somewhere*
					try {
						socket.connect(new InetSocketAddress("google.com", 80));

					} catch (BindException be) {
						// be.printStackTrace();
					}

				} catch (IOException ex) {
					ex.printStackTrace();
					continue;
				}
				selectedInterface = interface_;
				System.out.format("network interface: %sW IP: %s\n", interface_, address);

				// stops at the first *working* solution
				break OUTER;
			}
		}
		database.setInterface(selectedInterface.getDisplayName());
	}

	private boolean loadSharedDatabase() {
		try {
			FileInputStream SDBfile = new FileInputStream(SharedDatabase.FILE);
			ObjectInputStream input = new ObjectInputStream(SDBfile);

			try {

				sharedDatabase = (SharedDatabase) input.readObject();
				//sharedDatabase = new SharedDatabase();
				System.out.println("Loaded shared database " + SharedDatabase.FILE);

			} catch (Exception e) {

				System.out.println("Error while reading from saved shared database. Starting New shared database.");

				sharedDatabase = new SharedDatabase();
				System.out.println("Class Error: New shared database created.");
			}

			sharedDatabase.printUsers();

			input.close();
			SDBfile.close();
			return true;

		} catch (FileNotFoundException e) {

			System.out.println("New shared database");

			sharedDatabase = new SharedDatabase();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isSharedDatabaseLoaded() {
		return sharedDatabaseLoaded;
	}

	public void setSharedDatabaseLoaded(boolean sharedDatabaseLoaded) {
		this.sharedDatabaseLoaded = sharedDatabaseLoaded;
	}

	public void registerUser(String user, String password) {
		User newUser = new User(user, password);
		sharedDatabase.addUser(newUser);
	}

	public void printUsers() {
		System.out.println("==================== USERS ==================");
		sharedDatabase.printUsers();
		System.out.println("==================== USERS ==================");

	}

}
