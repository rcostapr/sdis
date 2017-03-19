package backend;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Duarte on 18-Mar-17.
 */
public class SavedFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final long CHUNK_SIZE = 64000;
	public static final long MAX_CHUNK = 1000000;
	public static final long MAX_FILE = CHUNK_SIZE * (MAX_CHUNK - 1);

	private String filePath;
	private String fileId;
	private ArrayList<Chunk> chunkList;
	private long chunkCounter;
	private File sfile;

	private int wantedReplicationDegree;

	public SavedFile(String filePath, int desiredReplicationDegree) throws FileTooLargeException, FileDoesNotExistsException {
		this.filePath = filePath;

		validateFile();

		wantedReplicationDegree = desiredReplicationDegree;
		sfile = new File(filePath);
		fileId = generateID(sfile);
		chunkCounter = 0;
		chunkList = new ArrayList<Chunk>();

		generateChunks();
	}

	private void generateChunks() {
		long fileSize = sfile.length();

		for (int i = 0; i < fileSize; i += CHUNK_SIZE) {
			chunkList.add(new Chunk(this, chunkCounter++));
		}

		// If the file size is a multiple of the chunk size, the last chunk has
		// size 0

		if (fileSize % CHUNK_SIZE == 0) {
			chunkList.add(new Chunk(this, chunkCounter++));
		}

	}

	private String generateID(File file) {
		// hash is generated from file name + file data + file size

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String file_id = file.getName();
		file_id += file.lastModified();

		file_id += file.length();

		md.update(file_id.getBytes());

		byte[] result = md.digest();

		StringBuilder result_string = new StringBuilder();
		for (byte bb : result) {
			result_string.append(String.format("%02X", bb));
		}
		return result_string.toString();
	}

	private void validateFile() throws FileTooLargeException, FileDoesNotExistsException {

		// exists

		if (!new File(filePath).exists()) {
			throw new FileDoesNotExistsException();
		}

		// not too big

		if (getFileSize() > MAX_FILE) {
			throw new FileTooLargeException();
		}
	}

	public String getFileId() {
		return fileId;
	}

	public File getFile() {
		return sfile;
	}

	public long getFileSize() {
		return sfile.length();
	}

	public int getWantedReplicationDegree() {
		return wantedReplicationDegree;
	}

	public class FileDoesNotExistsException extends Exception {

	}

	public class FileTooLargeException extends Exception {

	}

	public void showFileChunks() {
		if (chunkCounter > 0 && chunkList.size() > 0) {
			for (int i = 0; i < chunkList.size(); i++)
				System.out.println("Chunk Name: " + chunkList.get(i).getChunkName() + " Chunk No: " + chunkList.get(i).getChunkNo()
						+ " Replication Degree: " + chunkList.get(i).getCurrentReplicationDegree());
		}
	}
	public ArrayList<Chunk> getChunkList() {
		return chunkList;
	}

	public void setChunkList(ArrayList<Chunk> chunkList) {
		this.chunkList = chunkList;
	}
}
