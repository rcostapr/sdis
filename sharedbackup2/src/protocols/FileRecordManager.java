package protocols;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import backend.ConfigManager;
import backend.MulticastServer;

public class FileRecordManager {
	public static final String ADD_FILE_CMD = "ADD_FILE";
	public static final String DELETE_FILE_CMD = "DELETE_FILE";

	private static FileRecordManager instance = null;

	private FileRecordManager() {
	}

	public static FileRecordManager getInstance() {
		if (instance == null) {
			instance = new FileRecordManager();
		}
		return instance;
	}
	public boolean addFileToSharedDB(FileRecord record) {

		InetAddress MCAddr = ConfigManager.getConfigManager().getMcAddr();
		int MCPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(MCAddr, MCPort);

		String message;
		message = ADD_FILE_CMD + " " + "2.0"+ " " + ConfigManager.getConfigManager().getMyID() + " " + record.getFileID() + " " + record.getUsername() + " " + MulticastServer.CRLF + MulticastServer.CRLF;

		try {
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println("Sent ADD_FILE command for file " + record.getFileID() + " and user " + record.getUsername());

		return true;
	}
	
	public boolean deleteFileFromSharedDB(FileRecord record) {

		InetAddress MCAddr = ConfigManager.getConfigManager().getMcAddr();
		int MCPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(MCAddr, MCPort);

		String message;
		message = DELETE_FILE_CMD + " " + "2.0"+ " " + ConfigManager.getConfigManager().getMyID() + " " + record.getFileID() + " " + record.getUsername() + " " + MulticastServer.CRLF + MulticastServer.CRLF;

		try {
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println("Sent DELETE_FILE_CMD command for file " + record.getFileID() + " and user " + record.getUsername());

		return true;
	}
}
