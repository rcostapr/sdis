import utils.RMI_Interface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Duarte on 25-Mar-17.
 */
public class Client {

    static RMI_Interface stub;
    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(1090);
            stub = (RMI_Interface) registry.lookup("RMI");

            boolean response = stub.backupFile("test.txt", 1);
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
