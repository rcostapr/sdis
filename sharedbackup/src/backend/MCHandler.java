package backend;

import protocols.ChunkBackup;
import protocols.ChunkRestore;
import utils.Message;
import utils.SplittedMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;

public class MCHandler implements Runnable {
	private Message mMessage;
	private Random random;
	private static final int TIMEOUT = 400;
	private static final int BUFFER_SIZE = 128;
	private String IP;

	public MCHandler(Message receivedMessage, String IP) {
		mMessage = receivedMessage;
		random = new Random();
		this.IP = IP;
	}

	public void run() {
		String[] headerParts = mMessage.getHeader().split(" ");

		String messageType = headerParts[0].trim();
		int messageID = Integer.parseInt(headerParts[2].trim());

		// TODO:TAKE ME OUT LATER, JUST FOR TESTING
		System.out.println("MC Received Message: " + messageType + " from " + messageID);

		final String fileID;
		final int chunkNR;

		if (headerParts[1].trim().equals("1.0")) {
			switch (messageType) {
			case "STORED":

				fileID = headerParts[3].trim();
				chunkNR = Integer.parseInt(headerParts[4].trim());
				// if the file is mine, ++ repCount of the chunk
				System.out.println("Received STORED from " + messageID + " for chunk " + chunkNR);

				try {
					System.out.println("try");
					ConfigManager.getConfigManager().incChunkReplication(fileID, chunkNR);
				} catch (ConfigManager.InvalidChunkException e) {
					System.out.println("catch");
					synchronized (MCListener.getInstance().pendingChunks) {
						for (Chunk chunk : MCListener.getInstance().pendingChunks) {
							if (fileID.equals(chunk.getFileID()) && chunk.getChunkNo() == chunkNR) {
								System.out.println("Chunk " + chunk.getChunkNo() + " increasing from "
										+ chunk.getCurrentReplicationDegree());
								chunk.incCurrentReplication();
								break;
							}

						}
					}
				}
				break;
			case "GETCHUNK":
				fileID = headerParts[3].trim();
				chunkNR = Integer.parseInt(headerParts[4].trim());

				Chunk chunkToGet = ConfigManager.getConfigManager().getSavedChunk(fileID, chunkNR);

				if (messageID != ConfigManager.getConfigManager().getMyID()) {
					// if I don't store the chunk I don't care about the rest
					if (chunkToGet != null) {
						ChunkRecord record = new ChunkRecord(fileID, chunkNR);
						synchronized (MCListener.getInstance().watchedChunk) {
							MCListener.getInstance().watchedChunk.add(record);
						}
						try {
							Thread.sleep(random.nextInt(TIMEOUT));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// If no one responded to the GET, then I will do it
						if (!record.isServed) {

							Thread restoreByIp = new Thread(new restoreSenderIPListener());

							synchronized (MCListener.getInstance().watchedChunk) {
								MCListener.getInstance().watchedChunk.add(record);

								restoreByIp.start();

							}

							ChunkRestore.getInstance().sendChunk(chunkToGet);

							try {
								Thread.sleep(random.nextInt(TIMEOUT));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							synchronized (MCListener.getInstance().watchedChunk) {
								if (!record.isServed && MCListener.getInstance().watchedChunk.contains(record)) {
									// if no one else sent it:
									restoreByIp.interrupt();
									MCListener.getInstance().watchedChunk.remove(record);
									ChunkRestore.getInstance().sendChunk(chunkToGet);
								}
							}

						}
						synchronized (MCListener.getInstance().watchedChunk) {
							MCListener.getInstance().watchedChunk.remove(record);
						}
					}
				}
				break;

			case "DELETE":
				if (messageID != ConfigManager.getConfigManager().getMyID()) {
					fileID = headerParts[3].trim();
					ConfigManager.getConfigManager().deleteFile(fileID);
				}
				break;
				
			case "WASDELETED":
				if (messageID != ConfigManager.getConfigManager().getMyID()) {
					fileID = headerParts[3].trim();
					ConfigManager.getConfigManager().decDeletedFileReplication(fileID);
				}
				break;
			case "REMOVED":
				if (messageID != ConfigManager.getConfigManager().getMyID()) {
					fileID = headerParts[3].trim();
					chunkNR = Integer.parseInt(headerParts[4].trim());

					Chunk removedChunk = ConfigManager.getConfigManager().getSavedChunk(fileID, chunkNR);
					if (removedChunk != null) {
						try {
							removedChunk.decCurrentReplicationDegree();
							Thread.sleep(random.nextInt(401));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (removedChunk.getCurrentReplicationDegree() < removedChunk.getWantedReplicationDegree()) {
							ChunkBackup.getInstance().putChunk(removedChunk);
						}
					}
				}
				break;
			default:
				// unknown
				System.out.println("Can't handle " + messageType + "type");
				break;
			}
		} else
			System.out.println("MC: message with different version");
	}

	private static class restoreSenderIPListener implements Runnable {

		private static DatagramSocket restoreEnhSocket = null;

		@Override
		public void run() {
			if (restoreEnhSocket == null) {
				try {
					restoreEnhSocket = new DatagramSocket(ChunkRestore.ENHANCEMENT_RESPONSE_PORT);
				} catch (SocketException e) {
					System.out.println("Could not open the desired port for restore");
					e.printStackTrace();
					System.exit(-1);
				}
			}

			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);

			try {
				restoreEnhSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			SplittedMessage message = SplittedMessage.split(packet.getData());

			String[] headers = message.getHeader().split(MulticastServer.CRLF);

			String[] header_components = headers[0].split(" ");

			switch (header_components[0]) {
			case ChunkRestore.CHUNK_CONFIRMATION:
				String fileId = header_components[1];
				int chunkNo = Integer.parseInt(header_components[2]);

				synchronized (MCListener.getInstance().watchedChunk) {
					for (ChunkRecord record : MCListener.getInstance().watchedChunk) {
						if (record.fileId.equals(fileId) && record.chunkNo == chunkNo) {
							MCListener.getInstance().watchedChunk.remove(record);
							break;
						}
					}
				}

				break;
			default:
				System.out.println("Received non recognized command");
			}
		}
	}
}
