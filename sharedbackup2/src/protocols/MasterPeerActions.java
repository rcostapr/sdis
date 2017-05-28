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
            FileRecordManager.getInstance().addFileToSharedDB(record);
        }
    }

    @Override
    public void registerUser(String username, String password) throws RemoteException {
        User user = new User(username, password);
        System.out.println("Register new username " + username + " to DB.");
        ConfigManager.getConfigManager().getSharedDatabase().addUser(user);
        UsersSharingManager.getInstance().addUserToSharedDB(user);
    }

	@Override
	public boolean userExists(String user) {
		if(ConfigManager.getConfigManager().getSharedDatabase().userExists(user)){
			return true;
		}
		return false;
	}

	@Override
	public void addUser(String userName, String password) throws RemoteException {
		 	User user = new User(userName, password);
	        user.setHashedPassword(password);
	        System.out.println("Add username " + userName + " to DB.");
	        ConfigManager.getConfigManager().getSharedDatabase().addUser(user);
	        UsersSharingManager.getInstance().addUserToSharedDB(user);
		
	}

	@Override
	public void deleteFile(FileRecord record) {
		FileRecordManager.getInstance().deleteFileFromSharedDB(record);
	}

}
