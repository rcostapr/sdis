package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Duarte on 25-Mar-17.
 */
public interface RMI_Interface extends Remote{

    int RMI_PORT = 1900;
    String RMI_HOST = "localhost";

    String sayHello()throws RemoteException;
    boolean backupFile(String filePath, int replication) throws RemoteException;
    boolean restoreFile(String filePath)throws RemoteException;
}
