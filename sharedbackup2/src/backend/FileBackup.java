package backend;

import protocols.ChunkBackup;
import protocols.FileDelete;
import protocols.FileRecord;
import protocols.MasterPeer;

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

			if (!ChunkBackup.getInstance().putChunk(chunk)) {
				FileDelete.getInstance().deleteFile(file.getFileId());
				ConfigManager.getConfigManager().removeSavedFile(file.getFilePath());
			}

		}
		
		try {
			FileRecord record = new FileRecord(file.getFileId(), ConfigManager.getConfigManager().getUser().getUserName());
			MasterPeer.getInstance().getMasterStub().addFile(record);
			
		} catch (Exception e){
			System.out.println("Fail to Sync New File -> " + file.getFilePath());
			e.printStackTrace();
		}
		return true;
	}
	
	
}