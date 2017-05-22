package protocols;

import java.rmi.RemoteException;
import java.util.Date;

import backend.ConfigManager;
import backend.User;
import utils.SharedDatabase;

public class MasterPeerActions implements MasterPeerServices {

    @Override
    public long getMasterClock() {
        return new Date().getTime();
    }

    @Override
    public SharedDatabase getMasterPeerDB() {
        return ConfigManager.getConfigManager().getSharedDatabase();
    }

    @Override
    public void addFile(FileRecord record) throws RemoteException {
        boolean isNew = ConfigManager.getConfigManager().getSharedDatabase().addFile(record);
        if (isNew) {
            FilesSharingManager.getInstance().addFileToSharedDB(record);
        }
    }

    @Override
    public void addUser(String username, String hashedPassword) throws RemoteException {
        User user = new User(username, hashedPassword);
        user.setHashedPassword(hashedPassword);
        System.out.println("Received new username " + username + " info. Spreading the word...");
        ConfigManager.getConfigManager().getSharedDatabase().addUser(user);
        UsersSharingManager.getInstance().addUserToSharedDB(user);
    }

}
