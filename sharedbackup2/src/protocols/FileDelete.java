package protocols;

import backend.ConfigManager;
import backend.MulticastServer;
import sun.rmi.runtime.Log;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

public class FileDelete {
	private static FileDelete sInstance = null;

	public static FileDelete getInstance() {

		if (sInstance == null) {
			sInstance = new FileDelete();
		}
		return sInstance;
	}

	private FileDelete() {

	}

	public boolean deleteFile(String filePath) {
		String message = "";

		message += "DELETE" + " " + "2.0" + " " + ConfigManager.getConfigManager().getMyID() + " " + filePath + MulticastServer.CRLF + MulticastServer.CRLF;

		InetAddress MCAddr = ConfigManager.getConfigManager().getMcAddr();
		int MCPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(MCAddr, MCPort);

		try {
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean wasDeleteChunkFile(String fileID, String chunkNo) {
		String message = "";

		message += "WASDELETE" + " " + "2.0" + " " + ConfigManager.getConfigManager().getMyID() + " " + fileID + " " + chunkNo + MulticastServer.CRLF + MulticastServer.CRLF;

		InetAddress MCAddr = ConfigManager.getConfigManager().getMcAddr();
		int MCPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(MCAddr, MCPort);

		try {
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}
}
