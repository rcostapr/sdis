package utils;

import backend.Chunk;
import backend.ConfigManager;
import backend.SavedFile;
import protocols.FileDelete;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Database implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String FILE = "metadata.ser";
	private String communicationInterface = null;

	private String folder;
	private long maxBackupSize; // Bytes

	private List<Chunk> savedChunks; // chunks from others
	private Map<String, SavedFile> savedFiles; // my saved files
	private Map<String, Chunk> mDeletedFiles; // Deleted files

	private boolean loaded;

	public Database() {
		setLoaded(false);
		maxBackupSize = 40000 * 1000;// 40MB default space
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		setFolder(s);
		// System.out.println("setFolder: " + s);
		savedFiles = new HashMap<String, SavedFile>();
		savedChunks = Collections.synchronizedList(new ArrayList<Chunk>());
		mDeletedFiles = new HashMap<String, Chunk>();
	}

	public List<Chunk> getSavedChunks() {
		return savedChunks;
	}

	public long getMaxBackupSize() {
		return maxBackupSize;
	}

	public synchronized void setAvailSpace(long space) {
		if (space > 0) {
			maxBackupSize = space;
		}

	}

	public void saveDatabase() {
		try {
			FileOutputStream fileOut = new FileOutputStream("metadata.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();

		} catch (IOException i) {

		}
	}

	public Chunk getSavedChunk(String fileId, int chunkNo) {
		Chunk rChunk = null;

		synchronized (savedChunks) {
			for (Chunk chunk : savedChunks) {
				if (chunk.getFileID().equals(fileId) && chunk.getChunkNo() == chunkNo) {
					rChunk = chunk;
					break;
				}
			}
		}

		if (rChunk == null) {
			for (SavedFile file : savedFiles.values()) {
				if (file.getFileId().equals(fileId)) {
					// I have the chunk in my own file
					rChunk = file.getChunkList().get(chunkNo);
					break;
				}
			}
		}

		return rChunk;
	}

	public synchronized void incChunkReplication(String fileId, int chunkNo) throws ConfigManager.InvalidChunkException {
		SavedFile file = savedFiles.get(fileId);
		Chunk chunk = null;
		if (file != null) {
			// This is the owner machine of chunk's parent file
			file.incChunkReplication(chunkNo);
		} else {
			// It is a chunk saved in a backup operation
			for (Chunk list : savedChunks) {
				if (list.getFileID().equals(fileId) && list.getChunkNo() == chunkNo) {
					chunk = list;
					break;
				}
			}
		}
		if (chunk != null) {
			chunk.incCurrentReplication();
		} else {
			throw new ConfigManager.InvalidChunkException();
		}
	}

	public SavedFile getNewSavedFile(String path, int replication) throws SavedFile.FileTooLargeException, SavedFile.FileDoesNotExistsException, ConfigManager.FileAlreadySaved {
		SavedFile file = new SavedFile(path, replication);
		if (savedFiles.containsKey(file.getFileId())) {
			System.out.println("============================");
			System.out.println("=== File Exists: " + path);
			System.out.println("=== File Replication: " + replication);
			System.out.println("=== File Saved Replication: " + file.getWantedReplicationDegree());
			System.out.println("=== Delete File and Put New one");

			// if file already saved and replication degree is diferent ->
			// delete file and store a new one
			ConfigManager.getConfigManager().removeFile(new File(path).getAbsolutePath());
			// throw new ConfigManager.FileAlreadySaved();

		}
		savedFiles.put(file.getFileId(), file);

		return file;
	}

	public void addChunk(Chunk chunk) {
		synchronized (savedChunks) {
			savedChunks.add(chunk);
		}
	}

	public SavedFile getFileByPath(String path) {
		for (SavedFile file : savedFiles.values()) {
			if (file.getFilePath().equals(path)) {
				return file;
			}
		}
		System.out.println("getFileByPath File Not Found: " + path);
		printSavedFiles();
		return null;
	}

	public void printSavedFiles() {
		for (SavedFile file : savedFiles.values()) {
			System.out.println("FileId: " + file.getFileId() + " FilePath: " + file.getFilePath().toString());
		}
	}

	public boolean isChunkBackedUP(String fileID, int chunkNR) {
		for (Chunk chunk : savedChunks) {
			if (chunk.getFileID().equals(fileID) && chunk.getChunkNo() == chunkNR) {
				return true;
			}
		}
		return false;
	}

	public void deleteChunksFile(String fileID) {

		synchronized (savedChunks) {
			Iterator<Chunk> iterator = savedChunks.iterator();

			while (iterator.hasNext()) {
				Chunk chunk = iterator.next();
				if (chunk.getFileID().equals(fileID)) {
					chunk.removeData();
					iterator.remove();
					FileDelete.getInstance().wasDeleteChunkFile(chunk.getFileID(), Long.toString(chunk.getChunkNo()));
				}
			}
		}
		File folder = new File(fileID);
		folder.delete();
	}

	public String removeSavedFile(String filePath) {
		File f = new File(filePath);
		String fileId = null;
		synchronized (savedFiles) {
			Iterator<SavedFile> it = savedFiles.values().iterator();

			while (it.hasNext()) {
				SavedFile file = it.next();
				if (file.getFilePath().equals(f.getAbsolutePath())) {
					// Check if all chunks was delete from peers backups
					for (Chunk chunk : file.getChunkList()) {
						String fileChunkID = file.getFileId() + Long.toString(chunk.getChunkNo());
						mDeletedFiles.put(fileChunkID, chunk);
					}
					fileId = file.getFileId();
					FileDelete.getInstance().deleteFile(file.getFileId());
					it.remove();
				}
			}
		}
		return fileId;
	}

	public long getUsedSpace() {
		long usedSpace = 0;
		for (Chunk chunk : savedChunks) {
			usedSpace += chunk.getSize();
		}
		return usedSpace;
	}

	public Map<String, SavedFile> getSavedFiles() {
		return savedFiles;
	}

	public void setSavedFiles(Map<String, SavedFile> savedFiles) {
		this.savedFiles = savedFiles;
	}

	public synchronized void decChunkReplication(String fileId, int chunkNo) {

		for (SavedFile file : savedFiles.values()) {
			if (file.getFileId().equals(fileId)) {
				file.getChunkList().get(chunkNo).decCurrentReplicationDegree();
			}
		}

		for (Chunk chunk : savedChunks) {
			if (chunk.getFileID().equals(fileId) && chunk.getChunkNo() == chunkNo) {
				chunk.setCurrentReplicationDegree(chunk.getCurrentReplicationDegree() - 1);
				return;
			}
		}
	}

	public void removeChunk(Chunk deleteChunk) {
		synchronized (savedChunks) {
			Iterator<Chunk> iterator = savedChunks.iterator();

			while (iterator.hasNext()) {
				Chunk chunk = iterator.next();
				if (chunk.getFileID().equals(deleteChunk.getFileID()) && chunk.getChunkNo() == deleteChunk.getChunkNo()) {
					chunk.removeData();
					iterator.remove();
					break;
				}
			}
		}
		File folder = new File(deleteChunk.getFileID());
		folder.delete();
	}

	public synchronized void decDeletedChunkFileCount(String fileId, long chunkNo) {
		Chunk filechunk = mDeletedFiles.get(fileId);
		if (filechunk != null) {
			Integer currReplication = filechunk.getCurrentReplicationDegree();
			if (currReplication != null) {
				int newReplication = filechunk.getCurrentReplicationDegree() - 1;
				if (newReplication <= 0) {
					mDeletedFiles.remove(fileId);
				} else {
					filechunk.setCurrentReplicationDegree(newReplication);
					synchronized (mDeletedFiles) {
						mDeletedFiles.put(fileId, filechunk);
					}
				}
			}
		}
	}

	public InetAddress getInterface() throws SocketException {
		return NetworkInterface.getByName(communicationInterface).getInetAddresses().nextElement();
	}

	public String getInterfaceName() {
		return communicationInterface;
	}

	public void setInterface(String intrfc) {
		communicationInterface = intrfc;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public ArrayList<Chunk> getDeletedChunkFiles() {
		ArrayList<Chunk> retFileChunk = new ArrayList<Chunk>();

		for (Chunk chunk : mDeletedFiles.values()) {
			retFileChunk.add(chunk);
		}
		return retFileChunk;
	}

	public void print() {
		System.out.println();
		System.out.println("==============================================================");
		System.out.println("Used Space: " + getUsedSpace() / 1000 + " / " + maxBackupSize / 1000 + " KB");
		System.out.println("FILES FROM THIS PEER:");
		for (SavedFile file : savedFiles.values()) {
			System.out.println("----------------------------------------------------------");
			System.out.println("file = " + file.getFilePath());
			System.out.println("FileId = " + file.getFileId());
			System.out.println("REP DEG = " + file.getWantedReplicationDegree());
			System.out.println("..........................................................");
			file.showFileChunks();
			System.out.println("----------------------------------------------------------");
			System.out.println();
		}
		System.out.println();
		System.out.println("==============================================================");
		System.out.println();
		System.out.println("Chunks Stored on This Peer:");
		System.out.println();
		for (Chunk chunk : savedChunks) {
			System.out.println("chunk ID = " + chunk.getFileID());
			System.out.println("Chunk NO = " + chunk.getChunkNo());
			System.out.println("chunk size = " + chunk.getSize());
			System.out.println("CurrentReplicationDeg = " + chunk.getCurrentReplicationDegree());
			System.out.println();
		}
		System.out.println("==============================================================");
		System.out.println();
		System.out.println("Missing Delete Chunks Of the Deleted Files in this Peer:");
		System.out.println();
		for (Chunk chunk : mDeletedFiles.values()) {
			System.out.println("chunk ID = " + chunk.getFileID());
			System.out.println("Chunk NO = " + chunk.getChunkNo());
			System.out.println("chunk size = " + chunk.getSize());
			System.out.println("CurrentReplicationDeg = " + chunk.getCurrentReplicationDegree());
			System.out.println();
		}
		System.out.println("==============================================================");
	}

}