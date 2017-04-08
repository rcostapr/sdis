package backend;

import protocols.ChunkBackup;
import protocols.ChunkRestore;
import utils.Message;

import java.util.Random;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MCHandler implements Runnable {
    private Message mMessage;
    private Random random;
    private static final int TIMEOUT = 400;
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

        //TODO:TAKE ME OUT LATER, JUST FOR TESTING
        System.out.println("MC Received Message: " + messageType + " from " + messageID);

        final String fileID;
        final int chunkNR;

        if (headerParts[1].trim().equals("1.0")) {
            switch (messageType) {
                case "STORED":

                    fileID = headerParts[3].trim();
                    chunkNR = Integer.parseInt(headerParts[4].trim());
                    //if the file is mine, ++ repCount of the chunk
                    System.out.println("Received STORED from " + messageID + " for chunk "+ chunkNR);
                    if (messageID != ConfigManager.getConfigManager().getMyID()) {
                        try {
                            System.out.println("try");
                            ConfigManager.getConfigManager().incChunkReplication(fileID,
                                    chunkNR);
                        } catch (ConfigManager.InvalidChunkException e) {
                            System.out.println("catch");
                            synchronized (MCListener.getInstance().pendingChunks) {
                                for (Chunk chunk : MCListener
                                        .getInstance().pendingChunks) {
                                    if (fileID.equals(chunk.getFileID())
                                            && chunk.getChunkNo() == chunkNR) {
                                        System.out.println("Chunk " + chunk.getChunkNo() + " increasing from "+ chunk.getCurrentReplicationDegree());
                                        chunk.incCurrentReplication();
                                    }
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
                        //if I don't store the chunk I don't care about the rest
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
                            //If no one responded to the GET, then I will do it
                            if (!record.isServed) {

                                ChunkRestore.getInstance().sendChunk(chunkToGet);
                            }
                            MCListener.getInstance().watchedChunk.remove(record);
                        }
                    }
                    break;


                case "DELETE":
                    if (messageID != ConfigManager.getConfigManager().getMyID()) {
                        fileID = headerParts[3].trim();
                        ConfigManager.getConfigManager().deleteFile(fileID);
                    }
                    break;
                case "REMOVED":
                    if (messageID != ConfigManager.getConfigManager().getMyID()) {
                        fileID = headerParts[3].trim();
                        chunkNR = Integer.parseInt(headerParts[4].trim());

                        Chunk removedChunk = ConfigManager.getConfigManager().getSavedChunk(fileID, chunkNR);
                        ConfigManager.getConfigManager().decChunkReplication(fileID, chunkNR);
                        if (removedChunk != null) {
                            try {
                                Thread.sleep(400);
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
                    //unknown
                    System.out.println("Can't handle " + messageType + "type");
                    break;
            }
        } else System.out.println("MC: message with different version");
    }
}
