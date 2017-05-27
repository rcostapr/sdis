package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import backend.Chunk;
import backend.ConfigManager;
import backend.SavedFile;
import backend.User;
import protocols.FileRecord;
import protocols.MasterPeer;
import protocols.SharedClock;

public class SharedDatabase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String FILE = "sharedDB.ser";
	private ArrayList<User> users;
	private ArrayList<FileRecord> files;
	private Date date;
	private long lastModification;

	public SharedDatabase() {
		date = new Date();
		users = new ArrayList<User>();
		files = new ArrayList<FileRecord>();
		// indicates default database
		lastModification = 0;
	}

	private void updateTimestamp() {
		try {
			lastModification = SharedClock.getInstance().getTime();
		} catch (SharedClock.NotSyncedException e) {
			lastModification = date.getTime();
		}
	}

	public boolean addUser(User user) {
		for (int i = 0; i < users.size(); i++)
			if (users.get(i).getUserName().equals(user.getUserName()))
				return false;

		users.add(user);
		System.out.println("User " + user.getUserName() + " added");

		updateTimestamp();
		saveDatabase();

		return true;
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public long getLastModification() {
		return lastModification;
	}

	public void saveDatabase() {
		try {
			FileOutputStream fileOut = new FileOutputStream(FILE);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();

			System.out.println("Shared Database Saved");
		} catch (IOException i) {
			System.out.println("Could not save shared database");
			i.printStackTrace();
		}
	}

	public User login(String userName, String password) {
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			if (u.login(userName, password))
				return u;
		}

		return null;
	}

	private void createFolder(String path) {
		File theDir = new File(path);
		if (!theDir.exists()) {
			System.out.println("Creating directory: " + theDir.getName());
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if (result) {
				System.out.println("DIR Created");
			}
		}

	}

	public void join(SharedDatabase masterPeerDB) {
		long masterTimestamp = masterPeerDB.getLastModification();
		ArrayList<User> masterPeerUsers = masterPeerDB.getUsers();
		ArrayList<FileRecord> masterFiles = masterPeerDB.getFiles();

		if (lastModification == 0) {
			// this is the default database
			lastModification = masterTimestamp;
			users = masterPeerUsers;
			files = masterFiles;
			return;
		}

		// merge users
		for (User masterPeerUser : masterPeerUsers) {
			boolean found = false;
			for (User nUser : users) {
				if (masterPeerUser.equals(nUser)) {
					found = true;
					if (!masterPeerUser.getPassword().equals(nUser.getPassword()) && masterTimestamp > lastModification) {
						nUser.setHashedPassword(masterPeerUser.getPassword());
						System.out.println("Modified password of " + masterPeerUser.getUserName());
					}
					break;
				}
			}
			if (!found) {
				users.add(masterPeerUser);
				System.out.println("Added user " + masterPeerUser.getUserName());
			}
		}

		// send new users to master
		for (User nUser : users) {
			boolean found = false;
			for (User masterPeerUser : masterPeerUsers) {
				if (masterPeerUser.equals(nUser)) {
					found = true;
					break;
				}
			}
			if (!found) {
				try {
					MasterPeer.getInstance().getMasterStub().addUser(nUser.getUserName(), nUser.getPassword());
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Sent info of user " + nUser.getUserName());
			}
		}
		saveDatabase();
	}

	private ArrayList<FileRecord> getFiles() {
		return files;
	}

	public boolean addFile(FileRecord newRecord) {

		for (FileRecord fr : files) {
			if (fr.getFileID().equals(newRecord.getFileID())) {
				return false;
			}
		}
		// file not found
		files.add(newRecord);
		return true;
	}

	public void removeFile(FileRecord deletedRecord) {
		for (FileRecord fr : files) {
			if (fr.getFileID().equals(deletedRecord.getFileID())) {
				files.remove(fr);
			}
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public boolean userExists(String user) {
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			if (u.getUserName().equals(user))
				return true;
		}
		return false;
	}

	public void printUsers() {
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			System.out.println(u.getUserName());
		}
	}

	public void printUserFiles() {
		for (int i = 0; i < files.size(); i++) {
			FileRecord fr = files.get(i);
			System.out.println("Username: " + fr.getUsername() + " FileID: " + fr.getFileID());
		}
	}

	public void print() {
		System.out.println("=========  USERS  =============");
		printUsers();
		System.out.println("======= Files By User =========");
		printUserFiles();
		System.out.println("===============================");

	}

	public void print(String userName) {
		System.out.println();
		System.out.println("==============================================================");
		System.out.println("Used Space: " + getUsedSpace(userName) / 1000 + " KB");
		System.out.println("FILES FROM THIS PEER:");
		for (SavedFile file : ConfigManager.getConfigManager().getDatabase().getSavedFiles().values()) {
			if (isUserFile(file.getFileId())) {
				System.out.println("----------------------------------------------------------");
				System.out.println("file = " + file.getFilePath());
				System.out.println("FileId = " + file.getFileId());
				System.out.println("REP DEG = " + file.getWantedReplicationDegree());
				System.out.println("..........................................................");
				file.showFileChunks();
				System.out.println("----------------------------------------------------------");
				System.out.println();
			}
		}
		System.out.println();
		System.out.println("==============================================================");
		System.out.println();
		System.out.println("Chunks Stored on This Peer:");
		System.out.println();
		for (Chunk chunk : ConfigManager.getConfigManager().getDatabase().getSavedChunks()) {
			if (isUserFile(chunk.getFileID())) {
				System.out.println("chunk ID = " + chunk.getFileID());
				System.out.println("Chunk NO = " + chunk.getChunkNo());
				System.out.println("chunk size = " + chunk.getSize());
				System.out.println("CurrentReplicationDeg = " + chunk.getCurrentReplicationDegree());
				System.out.println();
			}
		}
		System.out.println("==============================================================");
	}

	private boolean isUserFile(String fileId) {
		for (FileRecord record : files) {
			if (record.getFileID().equals(fileId)) {
				return true;
			}
		}
		return false;
	}

	private long getUsedSpace(String username) {
		long usedSpace = 0;
		for (FileRecord record : files) {
			if (record.getUsername().equals(username))
				for (Chunk chunk : ConfigManager.getConfigManager().getDatabase().getSavedChunks()) {
					if (record.getFileID().equals(chunk.getFileID()))
						usedSpace += chunk.getSize();
				}
		}
		return usedSpace;
	}

}
