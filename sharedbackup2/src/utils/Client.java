package utils;

import utils.RMI_Interface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

	static RMI_Interface stub;

	public static void main(String[] args) {

		String cmd = args[0];

		switch (cmd) {
		case "login":
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

	public static void login(String[] args) {
		// TODO
		//Verificar se o user existe na base de dados
		
		// TODO
		//Se existe pedir password Caso contrario exibir mensagem de "User Not Found"
		
		// TODO
		//Se password é boa registar o utilizador e correr comandos caso contrário exibir Mensagem "Not a Valid Password"
		runCMD();

	}

	public static void register(String[] args) {
		
		// TODO
		// Fazer o registo do user
		// pedir password
		// confirmar password
		// se as passwords coincidem fazer o registo do utilizador caso contrario exibir a mensagem "Passwords not Matching"
		

	}

	public static void runCMD() {

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Insert CMD or \"quit\" to exit:");
			String cmd = scanner.nextLine();
			if (cmd.equals("quit")) {
				break;
			}
			String[] args = cmd.split(" ");
			sendCMD(args);
		}
		System.out.println("Exiting Shared Bakup ...");
		scanner.close();
	}

	public static void sendCMD(String[] args) {
		System.out.print("Sending:");
		for (String str : args)
			System.out.print(" " + str);
		System.out.println();
		boolean test = true;

		if (!test) {
			// Client Acess_Point Command operand1 operand2
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
				} else
					System.out.println("invalid arguments");
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
				} else
					System.out.println("invalid arguments");
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
				} else
					System.out.println("invalid arguments");
				break;

			case "STATE":
				if (args.length == 2) {
					try {
						stub.state();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} else
					System.out.println("invalid arguments");
				break;
			case "RECLAIM":

				if (args.length == 3) {
					try {
						response = stub.spaceReclaim(Integer.parseInt(args[2]));
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
}
