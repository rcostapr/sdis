package frontend;

import backend.ConfigManager;
import backend.FileBackup;
import backend.SavedFile;
import utils.RMI_Interface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class Interface implements RMI_Interface{

	private static Interface instance = null;
	private String accessPoint;

	private Interface() {
	}

	public static Interface getInstance() {
		if (instance == null) {
			instance = new Interface();
		}
		return instance;
	}

	public void startUp(){
		startRMI(accessPoint);
		ConfigManager.getConfigManager().startupListeners();
		ConfigManager.getConfigManager().saveDB();
	}

	private void startRMI(String acessPoint) {
		try {
			RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(this,0);
			Registry reg = null;
			try {
				reg = LocateRegistry.createRegistry(1090);
			}
			catch (RemoteException e)
			{
				System.out.println("RMI registry already running");
				reg = LocateRegistry.getRegistry(1090);
			}
			reg.rebind(accessPoint, stub);
		} catch (RemoteException e) {
			System.out.println("FODEU");
			e.printStackTrace();
		}
	}

	public String sayHello()throws RemoteException{
		return "Hello!";
	}

	public boolean backupFile(String filePath, int replication){
		// TODO: add exceptions File too large, File already in system, File  does not exist

		SavedFile file = null;
		try {
			file = new SavedFile(filePath, replication);
		} catch (SavedFile.FileTooLargeException e) {
			e.printStackTrace();
		} catch (SavedFile.FileDoesNotExistsException e) {
			e.printStackTrace();
		}

		file.showFileChunks();

		//TODO: if fileSize + database.availableSpace > Max space, cancel
		//file.showFileChunks();

		FileBackup.getInstance().saveFile(file);

		return true;
	}

	public void setAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}
}
