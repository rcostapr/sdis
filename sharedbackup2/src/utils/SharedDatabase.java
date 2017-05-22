package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import backend.User;
import protocols.FileRecord;
import protocols.MasterPeer;
import protocols.SharedClock;

public class SharedDatabase implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String FILE = "sharedDB.ser";
    private static ArrayList<User> users;
    private ArrayList<FileRecord> files;
    private Date date;
    private long lastModification;


    public SharedDatabase() {
        date = new Date();
        users = new ArrayList<User>();
        files = new ArrayList<FileRecord>();
        // indicates default database
        lastModification = 0;
    }

    private void updateTimestamp() {
        try {
            lastModification = SharedClock.getInstance().getTime();
        } catch (SharedClock.NotSyncedException e) {
            lastModification = date.getTime();
        }
    }

    public boolean addUser(User user) {
        for (int i = 0; i < users.size(); i++)
            if (users.get(i).getUserName().equals(user.getUserName()))
                return false;

        users.add(user);
        System.out.println("User " + user.getUserName() + " added");

        updateTimestamp();
        saveDatabase();

        return true;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public long getLastModification() {
        return lastModification;
    }

    public void saveDatabase() {
        try {
            FileOutputStream fileOut = new FileOutputStream(FILE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();

            System.out.println("Shared Database Saved");
        } catch (IOException i) {
            System.out.println("Could not save shared database");
            i.printStackTrace();
        }
    }

    public static User login(String userName, String password) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.login(userName, password))
                return u;
        }

        return null;
    }

    public void createNameSpace(String path) {
        createFolders(path);
        System.out.println("Created namespace folders");
    }

    private void createFolders(String path) {
    	File theDir = new File(path);
		if (!theDir.exists()) {
			System.out.println("Creating directory: " + theDir.getName());
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if (result) {
				System.out.println("DIR Created");
			}
		}
		
	}

	public void merge(SharedDatabase masterPeerDB) {
        long masterTimestamp = masterPeerDB.getLastModification();
        ArrayList<User> masterPeerUsers = masterPeerDB.getUsers();
        ArrayList<FileRecord> masterFiles = masterPeerDB.getFiles();

        if (lastModification == 0) {
            // this is the default database
            lastModification = masterTimestamp;
            users = masterPeerUsers;
            files = masterFiles;
            return;
        }
        
        // merge users
        for (User masterPeerUser : masterPeerUsers) {
            boolean found = false;
            for (User nUser : users) {
                if (masterPeerUser.equals(nUser)) {
                    found = true;
                    if (!masterPeerUser.getHashedPassword().equals(nUser.getHashedPassword())
                            && masterTimestamp > lastModification) {
                    	nUser.setHashedPassword(masterPeerUser.getHashedPassword());
                        System.out.println("Modified password of " + masterPeerUser.getUserName());
                    }
                    break;
                }
            }
            if (!found) {
                users.add(masterPeerUser);
                System.out.println("Added user " + masterPeerUser.getUserName());
            }
        }

        // send new users to master
        for (User nUser : users) {
            boolean found = false;
            for (User masterPeerUser : masterPeerUsers) {
                if (masterPeerUser.equals(nUser)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                try {
                    MasterPeer.getInstance().getMasterStub().addUser(nUser.getUserName(), nUser.getHashedPassword());
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Sent info of user " + nUser.getUserName());
            }
        }
        saveDatabase();
    }

    private ArrayList<FileRecord> getFiles() {
		return files;
	}

	public boolean addFile(FileRecord record) {

        for (FileRecord fr : files) {
            if (fr.getHash().equals(record.getHash())) {
                return false;
            }
        }
        // file not found
        files.add(record);
        return true;
    }

    public void removeFile(FileRecord record) {
        for (FileRecord fr : files) {
            if (fr.getHash().equals(record.getHash())) {
            	files.remove(fr);
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
