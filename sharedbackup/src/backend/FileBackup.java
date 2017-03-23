package backend;

import protocols.ChunkBackup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class FileBackup {
	private static FileBackup fInstance = null;

	private FileBackup() {
	}

	public static FileBackup getInstance() {
		if (fInstance == null) {
			fInstance = new FileBackup();
		}
		return fInstance;
	}

	// call putChunk for each chunk in SavedFile
	public boolean saveFile(SavedFile file) {
		ArrayList<Chunk> list = file.getChunkList();

		for (int i = 0; i < list.size(); i++) {

			final Chunk chunk = list.get(i);

			 ChunkBackup.getInstance().putChunk(chunk);
		}

		return true;
	}
}