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

    public MCHandler(Message receivedMessage) {
        mMessage=receivedMessage;
        random = new Random();
    }

    public void run(){
        String[] headerParts  = mMessage.getHeader().split(" ");

        String messageType = headerParts[0].trim();
        int messageID = Integer.parseInt(headerParts[2].trim());



        //TODO:TAKE ME OUT LATER, JUST FOR TESTING
        System.out.println("Received Message: " + messageType + " from " + messageID);

        final String fileID;
        final int chunkNR;


        switch (messageType) {
            case "STORED":

                fileID = headerParts[3].trim();
                chunkNR = Integer.parseInt(headerParts[4].trim());
                //if the file is mine, ++ repCount of the chunk
                try {
                    ConfigManager.getConfigManager().incChunkReplication(fileID,
                            chunkNR);
                } catch (ConfigManager.InvalidChunkException e) {
                    e.printStackTrace();
                }


                // if not my file, ++ the count of the chunks im pending

                synchronized (MCListener.getInstance().pendingChunks) {
                    for (Chunk chunk : MCListener
                            .getInstance().pendingChunks) {
                        if (fileID.equals(chunk.getFileID())
                                && chunk.getChunkNo() == chunkNR) {

                            chunk.incCurrentReplication();
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
