package protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import utils.SharedDatabase;

public interface MasterPeerServices extends Remote {

    public static final String REG_ID = "RMI2.0";

    public long getMasterClock() throws RemoteException;
    public SharedDatabase getMasterPeerDB() throws RemoteException;
    public void addFile(FileRecord record) throws RemoteException;
	public boolean userExists(String user) throws RemoteException;
	public void registerUser(String string, String password) throws RemoteException;
	public void addUser(String userName, String password) throws RemoteException;
	public void deleteFile(FileRecord record) throws RemoteException;
}
