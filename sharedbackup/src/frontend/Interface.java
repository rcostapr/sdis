package frontend;

import backend.*;
import protocols.ChunkRestore;
import protocols.FileDelete;
import utils.RMI_Interface;

import java.io.File;
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
				reg = LocateRegistry.createRegistry(RMI_Interface.RMI_PORT);
			}
			catch (RemoteException e)
			{
				System.out.println("RMI registry already running");
				reg = LocateRegistry.getRegistry(RMI_Interface.RMI_PORT);
			}
			reg.rebind(accessPoint, stub);
		} catch (RemoteException e) {
			System.out.println("RMI problem");
			e.printStackTrace();
		}
	}

	public String sayHello()throws RemoteException{
		return "Hello!";
	}

	public boolean backupFile(String filePath, int replication){

		if (validateFile(filePath)) {
			SavedFile file = null;
			try {
				file = ConfigManager.getConfigManager().getNewSavedFile(filePath,replication);
			} catch (SavedFile.FileTooLargeException e) {
				e.printStackTrace();
			} catch (ConfigManager.FileAlreadySaved fileAlreadySaved) {
				fileAlreadySaved.printStackTrace();
			} catch (SavedFile.FileDoesNotExistsException e) {
				e.printStackTrace();
			}
			//file.showFileChunks();

			//TODO: if fileSize + database.availableSpace > Max space, cancel
			//file.showFileChunks();

			return FileBackup.getInstance().saveFile(file);

		}else{
			return false;}
	}

	private boolean validateFile(String path){
		// exists
		File f = new File(path);
		if ( f.exists()) {
			if (f.length() < SavedFile.MAX_FILE) {
				return true;
			}
		}
		return false;
	}

	public void setAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}

	public boolean restoreFile(String filePath){

		SavedFile fileToRestore = ConfigManager.getConfigManager().getFileByPath(filePath);
		if (fileToRestore != null	){
			return FileRestore.getInstance().restoreFile(fileToRestore);
		}
		return false;
	}

	public boolean state(){
		ConfigManager.getConfigManager().printState();
		return true;
	}

	public boolean deleteFile(String filePath) throws RemoteException{
		ConfigManager.getConfigManager().removeFile(filePath);
		return true;

	}


}