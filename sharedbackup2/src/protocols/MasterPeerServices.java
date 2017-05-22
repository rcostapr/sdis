package protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import utils.SharedDatabase;

public interface MasterPeerServices extends Remote {

    public static final String REG_ID = "2.0";

    public long getMasterClock() throws RemoteException;
    public SharedDatabase getMasterPeerDB() throws RemoteException;
    public void addFile(FileRecord record) throws RemoteException;
    public void addUser(String username, String hashedPassword) throws RemoteException;
}
