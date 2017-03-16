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
        final String fileID;
        final int ChunkNR;

        //TODO:TAKE ME OUT LATER, JUST FOR TESTING
        System.out.println("Received Message: " + messageType);


        switch (messageType) {
            case "STORED":
                //stored
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
