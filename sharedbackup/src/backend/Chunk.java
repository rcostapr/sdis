package backend;

import java.io.*;

/**
 * Created by Duarte on 18-Mar-17.
 */
public class Chunk implements Serializable{

	public static int MAX_CHUNK_SIZE = 64000;

	private SavedFile file;
	private String fileID;
	private long chunkNo;
	private int currentReplicationDegree;


	private int wantedReplicationDegree;
	private String chunkName;
	private boolean isMyFile;

	public Chunk(SavedFile tFile, long tChunkno) {
		file = tFile;
		chunkNo = tChunkno;
		fileID = tFile.getFileId();
		currentReplicationDegree = 0;
		wantedReplicationDegree = tFile.getWantedReplicationDegree();

		isMyFile = true;

	}
	public Chunk(String fileId, int chunkNo, int desiredReplication) {
		this.file = null;
		this.chunkNo = chunkNo;
		this.fileID = fileId;
		this.wantedReplicationDegree = desiredReplication;
		isMyFile = false;
	}

	public long getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(long chunkNo) {
		this.chunkNo = chunkNo;
	}

	public int getCurrentReplicationDegree() {
		return currentReplicationDegree;
	}

	public int getWantedReplicationDegree() {
		return wantedReplicationDegree;
	}

	public void setCurrentReplicationDegree(int currentReplicationDegree) {
		this.currentReplicationDegree = currentReplicationDegree;
	}

	public String getChunkName() {
		return chunkName;
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public String getPath(){
		if (!isMyFile) {
			return chunkName;
		} else {
			return file.getFilePath();
		}
	}

	public byte[] getData() {

		if (isMyFile) {
			if (file.exists()) {

				int offset = (int) (SavedFile.CHUNK_SIZE * chunkNo);

				int chunkSize = (int) Math.min(SavedFile.CHUNK_SIZE,
						file.getFileSize() - offset);

				byte[] chunk = new byte[chunkSize];
				FileInputStream in = null;

				try {
					in = new FileInputStream(file.getFilePath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					in.skip(offset);

					in.read(chunk, 0, chunkSize);

					//Log.log("Lenght chunk" + chunkSize);
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return chunk;
			} else {
				return null;
			}
		} else {
			File newchunkfile = new File(getChunkName());
			FileInputStream in = null;
			try {
				in = new FileInputStream(newchunkfile);
				byte[] buffer = new byte[(int) newchunkfile.length()];
				int i = in.read(buffer);
				//Log.log("Chunk has " + i + " size");
				in.close();
				return buffer;
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		}
	}
	public boolean saveToFile(byte[] data) {
		FileOutputStream out = null;
		try {
			File f = new File(fileID+"/"+chunkNo);
			if(f.getParentFile().mkdir())
				System.out.println("FILE DIR DIDNT EXIST");
			out = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			System.out.println("merda1");
			e.printStackTrace();
			return false;
		}
		try {
			out.write(data);
		} catch (IOException e) {

			System.out.println("merda");
			e.printStackTrace();
			try {
				out.close();
			} catch (IOException e1) {

				System.out.println("merda2");
				e1.printStackTrace();
			}
			return false;
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	public synchronized int incCurrentReplication() {
		return ++currentReplicationDegree;

	}

	public int getCurrentReplicationDeg() {
		return currentReplicationDegree;
	}
}
