package backend;

import java.io.File;

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

	// private boolean isOwnMachineFile;

	public Chunk(SavedFile tFile, long tChunkno) {
		file = tFile;
		chunkNo = tChunkno;
		fileID = tFile.getFileId();
		currentReplicationDegree = 0;
		wantedReplicationDegree = tFile.getWantedReplicationDegree();
		chunkName = buildChunkName();

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

	public void setChunkName(String chunkName) {
		this.chunkName = chunkName;
	}

	public String buildChunkName() {
		
		String chunkName = file.getFile().getName() + "." + String.format("%04d", chunkNo);
		return chunkName;
	}
}
