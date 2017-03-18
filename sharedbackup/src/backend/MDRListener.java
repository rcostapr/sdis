package backend;

import utils.Message;

import java.net.InetAddress;

public class MDRListener implements Runnable{
    private static MDRListener mdrListener = null;


    private MDRListener() {
    }

    public static MDRListener getInstance(){
        if (mdrListener==null){
        	mdrListener= new MDRListener();
        }
        return mdrListener;
    }

    @Override
    public void run() {
        System.out.println("MDR listener started");
        InetAddress addr = ConfigManager.getConfigManager().getMdrAddr();
        int port = ConfigManager.getConfigManager().getmMDRport();


        MulticastServer receiver = new MulticastServer(addr , port);
        receiver.join();

        try {
            //TODO: get a way to stop this
            while (true){
                final byte[] message;

                message = receiver.receiveMessage();

                Message receivedMessage = new Message(message);

                ConfigManager.getConfigManager().getExecutorService().execute(new MDRHandler(receivedMessage));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

