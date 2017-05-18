package backend;

import utils.Message;
import utils.Packet;
import utils.SplittedMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import protocols.ChunkRestore;

public class MDRListener implements Runnable {
	
	private static final int BUFFER_SIZE = 70000;
	
	private static MDRListener mdrListener = null;

	public ArrayList<ChunkRecord> subscribedChunks;

	private MDRListener() {
		subscribedChunks = new ArrayList<ChunkRecord>();
	}

	public static MDRListener getInstance() {
		if (mdrListener == null) {
			mdrListener = new MDRListener();
		}
		return mdrListener;
	}

	@Override
	public void run() {
		System.out.println("MDR listener started");
		InetAddress addr = ConfigManager.getConfigManager().getMdrAddr();
		int port = ConfigManager.getConfigManager().getmMDRport();

		MulticastServer receiver = new MulticastServer(addr, port);
		receiver.join();

		ConfigManager.getConfigManager().getExecutorService().execute(new restoreListenerIPListener());

		try {
			// TODO: get a way to stop this
			while (ConfigManager.getConfigManager().isAppRunning()) {
				final Packet messagePacket = receiver.receiveMessage();

				Message receivedMessage = new Message(messagePacket.getMessage());

				ConfigManager.getConfigManager().getExecutorService().execute(new MDRHandler(receivedMessage));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void subscribeToChunkData(String fileId, long chunkNo) {
		subscribedChunks.add(new ChunkRecord(fileId, (int) chunkNo));
	}

	public synchronized void unsubscribeToChunkData(String fileId, long chunkNo) {

	}

	private class restoreListenerIPListener implements Runnable {

		@Override
		public void run() {
			DatagramSocket restoreSocket = null;
			try {
				restoreSocket = new DatagramSocket(ChunkRestore.ENHANCEMENT_SEND_PORT);
			} catch (SocketException e) {
				System.out.println("Could not open the desired port for restore");
				e.printStackTrace();
				System.exit(-1);
			}
			while (ConfigManager.getConfigManager().isAppRunning()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);

				try {
					restoreSocket.receive(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}

				byte[] message = new byte[packet.getLength()];
				System.arraycopy(buffer, 0, message, 0, packet.getLength());

				final SplittedMessage splittedMessage = SplittedMessage.split(message);

				String[] headers = splittedMessage.getHeader().split(MulticastServer.CRLF);

				String[] header_components = headers[0].split(" ");

				switch (header_components[0]) {
				case ChunkRestore.CHUNK_COMMAND:
					final String fileId = header_components[1];
					final int chunkNo = Integer.parseInt(header_components[2]);

					ConfigManager.getConfigManager().getExecutorService().execute(new Runnable() {

						@Override
						public void run() {
							for (ChunkRecord record : subscribedChunks) {
								if (record.fileId.equals(fileId) && record.chunkNo == chunkNo) {
									byte[] data;
									data = splittedMessage.getBody();

									ChunkData requestedChunk = new ChunkData(fileId, chunkNo, data);

									ChunkRestore.getInstance().addRequestedChunk(requestedChunk);

									ChunkRestore.getInstance().answerToChunkMessage(packet.getAddress(),
											ChunkRestore.ENHANCEMENT_RESPONSE_PORT, requestedChunk);

									subscribedChunks.remove(record);
									break;
								}
							}
						}
					});

					break;
				default:
					System.out.println("Received command not recognized");
				}
			}
			restoreSocket.close();
		}
	}

}
