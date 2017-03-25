package frontend;

import backend.ConfigManager;
import backend.FileBackup;
import backend.SavedFile;
import utils.RMI_Interface;
import utils.RMI_Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class Interface {

	private static Interface instance = null;


	private Interface() {
		startRMI();
	}

	private void startRMI(){
		//TODO:MY IP


		try {

			RMI_Interface engine = new RMI_Server();
			//System.setProperty("java.rmi.server.hostname", "192.168.1.89");

			RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(engine,0);

			Registry reg = LocateRegistry.getRegistry();

			reg.rebind(ConfigManager.getConfigManager().getRMI_Object_Name(),stub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	public static Interface getInstance() {
		if (instance == null) {
			instance = new Interface();
		}
		return instance;
	}

	public void startUp(){
		startRMI();
		ConfigManager.getConfigManager().startupListeners();
	}

	public boolean backupFile(String filePath, int replication) throws SavedFile.FileDoesNotExistsException,
			SavedFile.FileTooLargeException {
		// TODO: add exceptions File too large, File already in system, File  does not exist

		SavedFile file = new SavedFile(filePath, replication);

		file.showFileChunks();

		FileBackup.getInstance().saveFile(file);

		return true;
	}



}
