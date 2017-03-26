package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Duarte on 25-Mar-17.
 */
public interface RMI_Interface extends Remote{

    String sayHello()throws RemoteException;
    boolean backupFile(String filePath, int replication) throws RemoteException;
}
