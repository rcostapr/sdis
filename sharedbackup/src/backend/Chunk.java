package backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Duarte on 18-Mar-17.
 */
public class Chunk {

	public static int MAX_CHUNK_SIZE = 64000;

	private SavedFile file;
	private String fileID;
	private long chunkNo;
	private int currentReplicationDegree;
	private int wantedReplicationDegree;
	private String chunkName;
	private boolean isOwnMachineFile;

	public Chunk(SavedFile tFile, long tChunkno) {
		file = tFile;
		chunkNo = tChunkno;
		fileID = tFile.getFileId();
		currentReplicationDegree = 0;
		wantedReplicationDegree = tFile.getWantedReplicationDegree();
		chunkName = buildChunkName();
		isOwnMachineFile = true;

	}
	public Chunk(String fileId, int chunkNo, int desiredReplication) {
		this.file = null;
		this.chunkNo = chunkNo;
		this.fileID = fileId;
		this.wantedReplicationDegree = desiredReplication;
		isOwnMachineFile = false;
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

	public void setChunkName(String chunkName) {
		this.chunkName = chunkName;
	}

	public String buildChunkName() {
		
		String chunkName = file.getFile().getName() + "." + String.format("%04d", chunkNo);
		return chunkName;
	}
	
	public byte[] getData() {

		if (isOwnMachineFile) {
			if (file.exists()) {

				int offset = (int) (SavedFile.CHUNK_SIZE * chunkNo);

				int chunkSize = (int) Math.min(SavedFile.CHUNK_SIZE,
						file.getFileSize() - offset);

				byte[] chunk = new byte[chunkSize];
				FileInputStream in = null;

				try {
					in = new FileInputStream(file.getFile().getPath());
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
	public void saveToFile(byte[] data) {
		// TODO Auto-generated method stub
		
	}
	public void incCurrentReplication() {
		// TODO Auto-generated method stub
		
	}
}
