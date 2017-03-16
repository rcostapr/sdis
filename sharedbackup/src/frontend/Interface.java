package frontend;

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

    public boolean backupFile(String filePath, int replication) {
        //TODO: add exceptions File too large, File already in system, File does not exist
        SharedFile file = ConfigsManager.getInstance()
                .getNewSharedFileInstance(filePath, replication);

        FileBackup.getInstance().saveFile(file);

        return true;
    }
}
