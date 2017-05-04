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
        System.out.println("args = " + args.length);
        boolean response = false;
        switch (command) {
            case "HELLO":
                try {
                    String resp = stub.sayHello();
                    System.out.println("response = " + resp);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case "BACKUP":
                if (args.length == 4) {
                    try {
                        response = stub.backupFile(args[2], Integer.parseInt(args[3]));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    System.out.println("response: " + response);
                } else System.out.println("invalid arguments");
                break;

            case "RESTORE":
                if (args.length == 3) {

                    response = false;
                    try {
                        response = stub.restoreFile(args[2]);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    System.out.println("response: " + response);
                } else System.out.println("invalid arguments");
                break;

            case "DELETE":
                if (args.length == 3) {
                    response = false;
                    try {
                        response = stub.deleteFile(args[2]);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    System.out.println("response: " + response);
                } else System.out.println("invalid arguments");
                break;

            case "STATE":
                if (args.length == 2) {
                    try {
                        stub.state();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else System.out.println("invalid arguments");
                break;
            case "RECLAIM":

                if (args.length == 3) {
                    try {
                        response= stub.spaceReclaim(Integer.parseInt(args[2]));
                    }
                    catch (RemoteException e){
                        e.printStackTrace();
                    }
                } else System.out.println("invalid arguments");
                break;

            default:
                System.out.println("Command = " + command + " not recognized");
        }
    }
}
