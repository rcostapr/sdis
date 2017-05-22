package utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import protocols.FileRecord;

public class SharedDatabase implements Serializable {

    public static final String FILE = ".shareddata.ser";
    private ArrayList<User> users;
    private Date date;
    private long lastModification;


    public SharedDatabase() {
        date = new Date();
        users = new ArrayList<>();
        files = new HashMap<>();

        files.put(new ArrayList<FileRecord>());

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

    public User login(String userName, String password) {
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

    public void merge(SharedDatabase masterPeerDB) {
        long masterTimestamp = masterPeerDB.getLastModification();
        ArrayList<User> masterPeerUsers = masterPeerDB.getUsers();

        if (lastModification == 0) {
            // this is the default database
            lastModification = masterTimestamp;
            users = masterUsers;
            files = masterFiles;
            return;
        }
        
        // merge users
        for (User masterU : masterUsers) {
            boolean found = false;
            for (User mineU : users) {
                if (masterU.equals(mineU)) {
                    found = true;
                    if (!masterU.getHashedPassword().equals(mineU.getHashedPassword())
                            && masterTimestamp > lastModification) {
                        mineU.setHashedPassword(masterU.getHashedPassword());
                        System.out.println("Modified password of " + masterU.getUserName());
                    }
                    break;
                }
            }
            if (!found) {
                users.add(masterU);
                System.out.println("Added user " + masterU.getUserName());
            }
        }

        // send new users to master
        for (User mineU : users) {
            boolean found = false;
            for (User masterU : masterUsers) {
                if (masterU.equals(mineU)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                try {
                    MasterPeer.getInstance().getMasterStub().addUser(mineU.getUserName(), mineU.getHashedPassword(),
                            mineU.getAccessLevel());
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Sent info of user " + mineU.getUserName());
            }
        }
        saveDatabase();
    }

    public boolean addFile(FileRecord record) {

        ArrayList<FileRecord> list = files.get(record.getAccessLevel());

        for (FileRecord fr : list) {
            if (fr.getHash().equals(record.getHash())) {
                return false;
            }
        }
        // file not found
        list.add(record);
        return true;
    }

    public void removeFile(FileRecord record) {
        ArrayList<FileRecord> list = files.get(record.getAccessLevel());
        for (FileRecord fr : list) {
            if (fr.getHash().equals(record.getHash())) {
                list.remove(fr);
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
