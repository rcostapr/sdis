package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_Interface extends Remote{

    int RMI_PORT = 1900;
    String RMI_HOST = "localhost";

    String sayHello()throws RemoteException;
    boolean backupFile(String filePath, int replication) throws RemoteException;
    boolean restoreFile(String filePath)throws RemoteException;
    boolean state() throws RemoteException;
    boolean deleteFile(String filePath) throws RemoteException;
    boolean spaceReclaim(int newSpace) throws RemoteException;
}
