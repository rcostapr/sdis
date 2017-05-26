package utils;

import utils.RMI_Interface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

	static RMI_Interface stub;

	public static void main(String[] args) throws RemoteException {

		String cmd = args[0];

		switch (cmd) {
		case "login":
			if (args.length != 2) {
				System.out.println("Wrong Parameters\nUsage: login <user>");
			}
			login(args);
			break;
		case "register":
			register(args);
			break;
		default:
			System.out.println("Usage: login <user>");
			System.out.println("       register <user>");
			break;
		}

	}

	public static void login(String[] args) throws RemoteException {

		// Client Acess_Point Command operand1 operand2
		try {
			Registry registry = LocateRegistry.getRegistry(RMI_Interface.RMI_PORT);
			stub = (RMI_Interface) registry.lookup("RMI");
		} catch (Exception e) {
			System.out.print("== Server not Running ==");
			System.exit(0);
			// System.err.println("utils.Client exception: " + e.toString());
			// e.printStackTrace();
		}
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Login User: " + args[1]);
		String password = getPassword(scanner);

		if (stub.login(args[1], password)) {
			runCMD(scanner);
		} else {
			System.out.println("Wrong User or Password.");
		}
		
		scanner.close();

	}

	private static String getPassword(Scanner scanner) {
		// String password PasswordField.readPassword("Enter password: ");
		System.out.print("Enter password:");
		String password = scanner.nextLine();
		return password;
	}

	public static void register(String[] args) throws RemoteException {

		try {
			Registry registry = LocateRegistry.getRegistry(RMI_Interface.RMI_PORT);
			stub = (RMI_Interface) registry.lookup("RMI");
		} catch (Exception e) {
			System.out.print("== Server not Running ==");
			System.exit(0);
		}

		stub.printUsers();

		Scanner scanner = new Scanner(System.in);
		
		String password = getPassword(scanner);

		if (stub.userExists((args[1]))) {
			System.out.println("== User Already Exists ==");
		} else {
			stub.registerUser(args[1], password);
		}

		scanner.close();
		System.exit(0);

	}

	public static void runCMD(Scanner scanner) throws RemoteException {

		while (true) {

			clearConsole();
			System.out.println("USER: \"" + stub.getUserName() + "\" insert CMD or \"quit\" to Exit:");
			String cmd = scanner.nextLine();
			if (cmd.equals("quit")) {
				break;
			}
			String[] args = cmd.split(" ");
			sendCMD(args);
		}
		System.out.println("Exiting Shared Bakup ...");
	}

	public static void sendCMD(String[] args) {
		System.out.print("Sending:");
		for (String str : args)
			System.out.print(" " + str);
		System.out.println();
		boolean test = false;

		if (!test) {

			String command = args[0];
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
				if (args.length == 3) {
					try {
						response = stub.backupFile(args[1], Integer.parseInt(args[2]));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					System.out.println("response: " + response);
				} else
					System.out.println("invalid arguments");
				break;

			case "RESTORE":
				if (args.length == 2) {

					response = false;
					try {
						response = stub.restoreFile(args[1]);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					System.out.println("response: " + response);
				} else
					System.out.println("invalid arguments");
				break;

			case "DELETE":
				if (args.length == 2) {
					response = false;
					try {
						response = stub.deleteFile(args[1]);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					System.out.println("response: " + response);
				} else
					System.out.println("invalid arguments");
				break;

			case "STATE":
				if (args.length == 1) {
					try {
						stub.state();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} else
					System.out.println("invalid arguments");
				break;
			case "RECLAIM":

				if (args.length == 2) {
					try {
						response = stub.spaceReclaim(Integer.parseInt(args[1]));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} else
					System.out.println("invalid arguments");
				break;

			default:
				System.out.println("Command = " + command + " not recognized");
			}
		}
	}

	public final static void clearConsole() {
		try {
			final String os = System.getProperty("os.name");

			if (os.contains("Windows")) {
				Runtime.getRuntime().exec("cls");
			} else {
				Runtime.getRuntime().exec("clear");
			}
		} catch (final Exception e) {
			// Handle any exceptions.
		}
	}
}
