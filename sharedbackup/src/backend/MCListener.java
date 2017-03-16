package backend;

import utils.Message;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MCListener implements Runnable{


    private static MCListener mcListener = null;


    private MCListener() {
    }

    public static MCListener getInstance(){
        if (mcListener==null){
            mcListener= new MCListener();
        }
        return mcListener;
    }

    @Override
    public void run() {
        System.out.println("MMC listener started");
        InetAddress addr = ConfigManager.getConfigManager().getMcAddr();
        int port = ConfigManager.getConfigManager().getmMCport();


            MulticastServer receiver = new MulticastServer(addr , port);
            receiver.join();

            try {
                //TODO: get a way to stop this
                while (true){
                    final byte[] message;

                    message = receiver.receiveMessage();

                    Message receivedMessage = new Message(message);

                    //TODO: Launch MC HANDLER
                    ConfigManager.getConfigManager().getExecutor().execute(new MCHandler(receivedMessage));


                }
            }
                catch(Exception e){
                    e.printStackTrace();
                }
    }
}
