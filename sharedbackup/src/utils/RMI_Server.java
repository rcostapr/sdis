package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Duarte on 25-Mar-17.
 */
public class RMI_Server implements RMI_Interface {

    public RMI_Server(){

    }
    @Override
    public String sayHello()throws RemoteException{
        return "Hello!";
    }
}
