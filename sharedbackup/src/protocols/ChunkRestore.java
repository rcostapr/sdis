package protocols;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import backend.*;

public class ChunkRestore {

	public static final int MAX_RESTORE_TRIES = 5;
	private static ChunkRestore sInstance = null;

	public static final String GET_COMMAND = "GETCHUNK";
	public static final String CHUNK_COMMAND = "CHUNK";
	public static final String CHUNK_CONFIRMATION = "CHUNKCONFIRM";
	public static final int ENHANCEMENT_SEND_PORT = 50555;
	public static final int ENHANCEMENT_RESPONSE_PORT = 50556;
	private static final int REQUEST_TIME_INTERVAL = 500;

	private ArrayList<ChunkData> mRequestedChunks;

	public static ChunkRestore getInstance() {

		if (sInstance == null) {
			sInstance = new ChunkRestore();
		}
		return sInstance;
	}

	private ChunkRestore() {
		mRequestedChunks = new ArrayList<ChunkData>();
	}

	public ChunkData requestChunk(String fileId, long chunkNo) {

		ChunkData retChunk = null;

		String message = "";

		message += GET_COMMAND + " " + fileId + " " + chunkNo + MulticastServer.CRLF + MulticastServer.CRLF;

		InetAddress multCAddr = ConfigManager.getConfigManager().getMcAddr();
		int multCPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(multCAddr, multCPort);

		MDRListener.getInstance().subscribeToChunkData(fileId, chunkNo);

		int nrTries = 0;

		do {
			try {
				sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
			} catch (Exception e) {
				e.printStackTrace();
			} 
			try {
				Thread.sleep(REQUEST_TIME_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (mRequestedChunks) {
				for (ChunkData chunk : mRequestedChunks) {
					if (chunk.getFileID().equals(fileId) && chunk.getChunkNo() == chunkNo) {
						retChunk = chunk;
						mRequestedChunks.remove(chunk);
						break;
					}
				}
			}
			nrTries++;
		} while (retChunk == null && nrTries < MAX_RESTORE_TRIES);

		return retChunk;
	}

	public boolean sendChunk(Chunk chunk) {

		InetAddress multDRAddr = ConfigManager.getConfigManager().getMdrAddr();
		int multDRPort = ConfigManager.getConfigManager().getmMDRport();

		String header = "";

		header += CHUNK_COMMAND + " " + chunk.getFileID() + " " + chunk.getChunkNo() + MulticastServer.CRLF
				+ MulticastServer.CRLF;

		byte[] data = chunk.getData();

		byte[] message = new byte[header.length() + data.length];

		try {
			System.arraycopy(header.getBytes(MulticastServer.ASCII_CODE), 0, message, 0, header.length());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.arraycopy(data, 0, message, header.length(), data.length);

		MulticastServer sender = new MulticastServer(multDRAddr, multDRPort);

		try {
			sender.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Sent CHUNK command to MULTICAST in response to request of " + chunk.getFileID() + " no " + chunk.getChunkNo());

		return true;
	}

	public boolean sendChunk(Chunk chunk, InetAddress destinationAddress, int destinationPort) {

		String header = "";

		header += CHUNK_COMMAND + " " + chunk.getFileID() + " " + chunk.getChunkNo() + MulticastServer.CRLF
				+ MulticastServer.CRLF;

		byte[] data = chunk.getData();

		byte[] message = new byte[header.length() + data.length];

		try {
			System.arraycopy(header.getBytes(MulticastServer.ASCII_CODE), 0, message, 0, header.length());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.arraycopy(data, 0, message, header.length(), data.length);

		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		DatagramPacket packet = null;

		packet = new DatagramPacket(message, message.length, destinationAddress, destinationPort);

		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		socket.close();

		System.out.println("Sent CHUNK command to IP in response to request of " + chunk.getFileID() + " no " + chunk.getChunkNo());

		return true;
	}

	public void answerToChunkMessage(InetAddress addr, int port, Chunk chunk) {

		String message = "";

		message += CHUNK_CONFIRMATION + " " + chunk.getFileID() + " " + chunk.getChunkNo() + MulticastServer.CRLF
				+ MulticastServer.CRLF;

		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		DatagramPacket packet = null;
		try {
			packet = new DatagramPacket(message.getBytes(MulticastServer.ASCII_CODE),
					message.getBytes(MulticastServer.ASCII_CODE).length, addr, port);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		System.out.println("Sent message to IP: " + message);

		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		socket.close();

		System.out.println("Answered to CHUNK command to IP");
	}

	public synchronized void addRequestedChunk(ChunkData chunk) {
		mRequestedChunks.add(chunk);
	}
}