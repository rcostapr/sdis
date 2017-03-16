package backend;

import utils.Message;

import java.util.Random;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MDBHandler implements Runnable {
    private static final int TIMEOUT = 401;
    private Random random;
    private Message message;

    private static MDBHandler mInstance = null;
    public MDBHandler(Message receivedMessage) {
        message = receivedMessage;
        random = new Random();
    }

    @Override
    public void run() {
        //TODO: TAKE ME OUT LATER JUST FOR TEST
        System.out.println("MDB received a message");

        String[] header_parts = message.getHeader().split(" ");
        String messageType = header_parts[0].trim();

        switch (messageType){
            case "PUTCHUNK":
                //putchunk
                break;
            default:
                System.out.println("Received " + messageType+ " message in MDB");
                break;
        }
    }
}
