package backend;

import protocols.ChunkBackup;
import protocols.ChunkRestore;
import protocols.FileRecord;
import protocols.MasterPeer;
import utils.Message;
import utils.SplittedMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
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
		int peerID = Integer.parseInt(headerParts[2].trim());

		System.out.println("MC Received Message: " + messageType + " from " + peerID);

		final String fileID;
		final int chunkNR;

		if (headerParts[1].trim().equals("2.0")) {
			switch (messageType) {
			case "STORED":

				fileID = headerParts[3].trim();
				chunkNR = Integer.parseInt(headerParts[4].trim());
				// if the file is mine, ++ repCount of the chunk
				System.out.println("Received STORED from " + peerID + " for chunk " + chunkNR);

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

				if (peerID != ConfigManager.getConfigManager().getMyID()) {
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
				if (peerID != ConfigManager.getConfigManager().getMyID()) {
					fileID = headerParts[3].trim();
					ConfigManager.getConfigManager().deleteFile(fileID);
				}
				break;

			case "WASDELETE":
				if (peerID != ConfigManager.getConfigManager().getMyID()) {
					fileID = headerParts[3].trim();
					String chunkNo = headerParts[4].trim();
					ConfigManager.getConfigManager().decChunkDeletedFileReplication(fileID, chunkNo);
				}
				break;
			case "REMOVED":
				if (peerID != ConfigManager.getConfigManager().getMyID()) {
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
			case "WAKED_UP":
				if (peerID != ConfigManager.getConfigManager().getMyID()) {
					if (MasterPeer.getInstance().imMaster()) {
						try {
							System.out.println("Sending Peer:"+ConfigManager.getConfigManager().getMyID()+" i am MASTER ("+ConfigManager.getConfigManager().getInterfaceIP()+") in response to WAKED_UP");
							MasterPeer.getInstance().sendMasterCmd();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			case "MASTER":
				String masterIP = headerParts[3];

				System.out.println("Received MASTER CMD from " + masterIP);

				if (!MasterPeer.getInstance().checkMasterPeer(masterIP)) {

					if (MasterPeer.getInstance().imMaster()) {
						MasterPeer.getInstance().candidate();
					} else {
						try {
							MasterPeer.setInitMaster(masterIP);
							ConfigManager.getConfigManager().getSharedDatabase()
									.join(MasterPeer.getInstance().getMasterStub().getMasterPeerDB());
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("Received valid MASTER CMD from " + masterIP);
				}
				break;
			case "CANDIDATE":
				if (peerID != ConfigManager.getConfigManager().getMyID()) {

					long itsUptime = Long.parseLong(headerParts[3]);

					try {
						MasterPeer.getInstance().updateMaster(IP, itsUptime);
					} catch (Exception e) {
						new Thread() {
							@Override
							public void run() {
								MasterPeer.getInstance().candidate();
							}
						}.start();
						try {
							Thread.sleep(400);
							MasterPeer.getInstance().updateMaster(IP, itsUptime);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
				break;
			case "ADD_FILE":

				fileID = headerParts[3].trim();
				String username = headerParts[4].trim();
				FileRecord newFile = new FileRecord(fileID, username);
				// add record to shared database, to keep them sync
				ConfigManager.getConfigManager().getSharedDatabase().addFile(newFile);
				break;
			case "DELETE_FILE":

				fileID = headerParts[3].trim();
				String uname = headerParts[4].trim();
				FileRecord nFile = new FileRecord(fileID, uname);
				// delete record from shared database, to keep them sync
				ConfigManager.getConfigManager().getSharedDatabase().removeFile(nFile);
				break;
			case "ADD_USER":

				String newUsername = headerParts[3].trim();
				String password = headerParts[4].trim();
				User newUser = new User(newUsername, password);
				// update to proper password
				newUser.setHashedPassword(password);

				// add record to shared database, to keep them sync
				ConfigManager.getConfigManager().getSharedDatabase().addUser(newUser);
				break;

			default:
				// unknown
				System.out.println("Do NOT KNOW " + messageType + " type.");
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
