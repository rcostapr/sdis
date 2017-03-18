package backend;

import utils.Message;

import java.util.Random;

public class MDRHandler implements Runnable {
    private static final int TIMEOUT = 401;
    private Random random;
    private Message message;

    private static MDRHandler mInstance = null;
    public MDRHandler(Message receivedMessage) {
        message = receivedMessage;
        random = new Random();
    }

    @Override
    public void run() {
        //TODO: TAKE ME OUT LATER JUST FOR TEST
        System.out.println("MDR received a message");

        String[] header_parts = message.getHeader().split(" ");
        String messageType = header_parts[0].trim();

        switch (messageType){
            case "GETCHUNK":
                //getchunk
                break;
            default:
                System.out.println("Received " + messageType+ " message in MDR");
                break;
        }
    }
}