package utils;

import com.sun.org.apache.regexp.internal.RE;
import utils.RMI_Interface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Duarte on 25-Mar-17.
 */
public class Client {


    //TODO: Validate inputs, launch protocols
    static RMI_Interface stub;
    public static void main(String[] args) {
        //TODO: USAGE and args validation
        //Client Acess_Point Command operand1 operand2
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_Interface.RMI_PORT);
            stub = (RMI_Interface) registry.lookup(args[0]);
        } catch (Exception e) {
            System.err.println("utils.Client exception: " + e.toString());
            e.printStackTrace();
        }

        String command = args[1];
        switch (command){
            case "HELLO":
                try {
                    String response = stub.sayHello();
                    System.out.println("response = " + response);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case "BACKUP":
                boolean response = false;
                try {
                    response = stub.backupFile("test.pdf", 1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                System.out.println("response: " + response);
                break;

            case "RESTORE":
                response = false;
                try {
                    response = stub.restoreFile("test.pdf");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                System.out.println("response: " + response);
                break;

            case "DELETE":
                response = false;
                try {
                    response = stub.deleteFile("test.pdf");
                } catch (RemoteException e){
                    e.printStackTrace();
                }
                System.out.println("response: "+ response);
                break;

            case "STATE":
                try {
                    stub.state();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case "RECLAIM":
                //
                break;

            default:
                System.out.println("Command = " + command+ " not recognized");
        }
    }
}
