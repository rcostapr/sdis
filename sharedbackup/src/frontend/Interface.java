package frontend;

import backend.ConfigManager;
import backend.FileBackup;
import backend.SavedFile;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class Interface {

	private static Interface instance = null;

	private Interface() {
	}

	public static Interface getInstance() {
		if (instance == null) {
			instance = new Interface();
		}
		return instance;
	}

	public void init (){
		ConfigManager.getConfigManager().startupListeners();
		ConfigManager.getConfigManager().saveDB();
	}


	public boolean backupFile(String filePath, int replication) throws SavedFile.FileDoesNotExistsException,
			SavedFile.FileTooLargeException {
		// TODO: add exceptions File too large, File already in system, File  does not exist

		SavedFile file = null;
		try {
			file = ConfigManager.getConfigManager().getNewSavedFile(filePath,replication);
		} catch (ConfigManager.FileAlreadySaved fileAlreadySaved) {
			fileAlreadySaved.printStackTrace();
		}

		//TODO: if fileSize + database.availableSpace > Max space, cancel
		//file.showFileChunks();

		FileBackup.getInstance().saveFile(file);

		return true;
	}
}
