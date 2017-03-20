package protocols;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import sun.rmi.runtime.Log;
import backend.Chunk;
import backend.ConfigManager;
import backend.MulticastServer;

public class ChunkBackup {

	public static final String PUT_COMMAND = "PUTCHUNK";
	public static final String STORED_COMMAND = "STORED";
	private static final int PUT_TIME_INTERVAL = 500;
	private static final int MAX_RETRIES = 5;
	private static ChunkBackup sInstance = null;

	public static ChunkBackup getInstance() {

		if (sInstance == null) {
			sInstance = new ChunkBackup();
		}
		return sInstance;
	}

	private ChunkBackup() {
	}

	public boolean putChunk(Chunk chunk) {

		String header = "";

		header += PUT_COMMAND + " " + alId + " " + chunk.getFileID() + " " + chunk.getChunkNo() + " " + chunk.getCurrentReplicationDegree()
				+ MulticastServer.CRLF + chunk.getCurrentReplicationDegree() + MulticastServer.CRLF + MulticastServer.CRLF;

		byte[] data = chunk.getData();

		byte[] message = new byte[header.length() + data.length];

		try {
			System.arraycopy(header.getBytes(MulticastServer.ASCII_CODE), 0, message, 0, header.length());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		System.arraycopy(data, 0, message, header.length(), data.length);

		InetAddress multDBAddr = ConfigManager.getConfigManager().getMdbAddr();
		int multDBPort = ConfigManager.getConfigManager().getmMDBport();

		MulticastServer sender = new MulticastServer(multDBAddr, multDBPort);

		int counter = 0;

		Log.log("Sending chunk " + chunk.getChunkNo() + " of file " + chunk.getFileID() + "with " + chunk.getData().length + " bytes");

		do {
			try {
				sender.sendMessage(message);
			} catch (HasToJoinException e1) {
				e1.printStackTrace();
			}
			try {
				Log.log("WAITING : " + PUT_TIME_INTERVAL * (int) Math.pow(2, counter));
				Thread.sleep(PUT_TIME_INTERVAL * (int) Math.pow(2, counter));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;
			Log.log("REP DEG: " + chunk.getChunkNo() + " " + chunk.getCurrentReplicationDegree());
		} while (chunk.getCurrentReplicationDegree() > chunk.getCurrentReplicationDegree() && counter < MAX_RETRIES);

		if (counter == MAX_RETRIES) {

			Log.log("Did not reach necessary replication");

			return false;
		} else {

			Log.log("Sent successfully");
			return true;
		}

	}

	public boolean storeChunk(Chunk chunk, byte[] data) {

		InetAddress multCtrlAddr = ConfigManager.getConfigManager().getMcAddr();
		int multCtrlPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(multCtrlAddr, multCtrlPort);

		// save chunk in file
		chunk.saveToFile(data);

		chunk.incCurrentReplication();

		// add chunk to database
		ConfigManager.getConfigManager().addSavedChunk(chunk);

		String message = null;

		message = STORED_COMMAND + " " + chunk.getFileId() + " " + String.valueOf(chunk.getChunkNo()) + MulticastServer.CRLF
				+ MulticastServer.CRLF;

		try {
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
		} catch (HasToJoinException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Log.log("Sent STORED command for chunk of file " + chunk.getFileID() + " no " + chunk.getChunkNo() + " with " + data.length
				+ " bytes");

		return true;
	}
}