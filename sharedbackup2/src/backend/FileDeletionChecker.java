package backend;

import java.util.ArrayList;

import protocols.FileDelete;

public class FileDeletionChecker implements Runnable {

	private static final int ONE_MINUTE = 60000;
	private static final int HALF_SECOND = 500;

	@Override
	public void run() {

		try {
			while (ConfigManager.getConfigManager().isAppRunning()) {
				
				ArrayList<String> deletedFiles = ConfigManager.getConfigManager().getDeletedFiles();

				for (String filePath : deletedFiles) {
					
					//TODO
					System.out.println("Checker Deleted Files: " + filePath);
					FileDelete.getInstance().deleteFile(filePath);
					Thread.sleep(HALF_SECOND);
				}
				Thread.sleep(ONE_MINUTE);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
	}
}
