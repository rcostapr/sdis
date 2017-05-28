package frontend;

import backend.*;
import protocols.MasterPeer;
import protocols.SpaceReclaim;
import utils.RMI_Interface;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Interface implements RMI_Interface {

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

	public void startUp() throws IOException {
		startRMI(accessPoint);
		ConfigManager.getConfigManager().startupListeners();
		ConfigManager.getConfigManager().setInterface();
		ConfigManager.getConfigManager().saveDB();
		MasterPeer.getInstance().sendStartup();
	}

	private void startRMI(String acessPoint) {
		try {
			RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(this, 0);
			Registry reg = null;
			try {
				reg = LocateRegistry.createRegistry(RMI_Interface.RMI_PORT);
			} catch (RemoteException e) {
				System.out.println("RMI registry already running");
				reg = LocateRegistry.getRegistry(RMI_Interface.RMI_PORT);
			}
			reg.rebind(accessPoint, stub);
		} catch (RemoteException e) {
			System.out.println("RMI Problem");
			e.printStackTrace();
		}
	}

	public String sayHello() throws RemoteException {
		return "Hello!";
	}

	public boolean backupFile(String filePath, int replication) {
		
		if (validateFile(filePath)) {
			System.out.println("===============  validateFile  ==================");
			SavedFile file = null;
			try {
				file = ConfigManager.getConfigManager().getNewSavedFile(filePath, replication);
				ConfigManager.getConfigManager().saveDB();
			} catch (SavedFile.FileTooLargeException e) {
				e.printStackTrace();
			} catch (ConfigManager.FileAlreadySaved fileAlreadySaved) {
				System.out.println("file Already Saved");
			} catch (SavedFile.FileDoesNotExistsException e) {
				System.out.println("File Does Not Exists");
			}

			return FileBackup.getInstance().saveFile(file);

		} else {
			
			return false;
			
		}
	}

	private boolean validateFile(String path) {
		// exists
		File f = new File(path);
		if (f.exists()) {
			if (f.length() < SavedFile.MAX_FILE) {
				return true;
			}
		}
		return false;
	}

	public void setAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}

	public boolean restoreFile(String filename) {
		
		String filePath = new File(filename).getAbsolutePath();
		
		SavedFile fileToRestore = ConfigManager.getConfigManager().getFileByPath(filePath);
		if (fileToRestore != null) {
			return FileRestore.getInstance().restoreFile(fileToRestore);
		}
		return false;
	}

	public boolean state() {
		ConfigManager.getConfigManager().printState();
		return true;
	}

	public boolean deleteFile(String filePath) throws RemoteException {

		ConfigManager.getConfigManager().removeFile(new File(filePath).getAbsolutePath());
		return true;

	}

	public boolean spaceReclaim(int newSpace) {

		ConfigManager.getConfigManager().setMaxSpace(newSpace * 1000);

		while (ConfigManager.getConfigManager().getMaxSpace() <= ConfigManager.getConfigManager().getUsedSpace()) {
			// get next reclaim//
			Chunk deletedChunk = ConfigManager.getConfigManager().getNextRemovableChunk();
			SpaceReclaim.getInstance().reclaim(deletedChunk);
			ConfigManager.getConfigManager().removeChunk(deletedChunk);
		}
		return true;
	}

	@Override
	public boolean login(String user, String password) {
		return ConfigManager.getConfigManager().login(user, password);
	}

	@Override
	public boolean userExists(String user) {
		return ConfigManager.getConfigManager().userExists(user);
	}

	@Override
	public void registerUser(String user, String password) {
		ConfigManager.getConfigManager().registerUser(user, password);
	
	}

	@Override
	public void printUsers() throws RemoteException {
		ConfigManager.getConfigManager().printUsers();
		
	}

	@Override
	public String getUserName() throws RemoteException {
		return ConfigManager.getConfigManager().getUser().getUserName();
	}
}