package backend;

import utils.Message;

import java.util.Random;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MCHandler implements Runnable {
    private Message mMessage;
    private Random random;
    private static final int TIMEOUT= 400;
    private String IP;
    public MCHandler(Message receivedMessage, String IP) {
        mMessage=receivedMessage;
        random = new Random();
        this.IP=IP;
    }

    public void run(){
        String[] headerParts  = mMessage.getHeader().split(" ");

        String messageType = headerParts[0].trim();
        int messageID = Integer.parseInt(headerParts[2].trim());

        //TODO:TAKE ME OUT LATER, JUST FOR TESTING
        System.out.println("MC Received Message: " + messageType + " from " + messageID);

        final String fileID;
        final int chunkNR;


        switch (messageType) {
            case "STORED":

                fileID = headerParts[3].trim();
                chunkNR = Integer.parseInt(headerParts[4].trim());
                //if the file is mine, ++ repCount of the chunk

                if (messageID != ConfigManager.getConfigManager().getMyID()) {
                    try {
                        ConfigManager.getConfigManager().incChunkReplication(fileID,
                                chunkNR);
                    } catch (ConfigManager.InvalidChunkException e) {
                        e.printStackTrace();
                    }

                }
                // if not my file, ++ the count of the chunks im pending

                else {
                synchronized (MCListener.getInstance().pendingChunks) {
                    for (Chunk chunk : MCListener
                            .getInstance().pendingChunks) {
                        if (fileID.equals(chunk.getFileID())
                                && chunk.getChunkNo() == chunkNR) {

                            chunk.incCurrentReplication();
                        }
                    }
                    }
                }
                break;
            case "GETCHUNK":
                //getchunk
                break;
            case "DELETE":
                //delete
                break;
            case "REMOVED":
                //removed
                break;
            default:
                //unknown
                System.out.println("Can't handle " + messageType + "type");
                break;
        }

    }
}
