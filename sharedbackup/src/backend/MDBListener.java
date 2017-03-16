package backend;

import java.net.InetAddress;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MDBListener implements Runnable{
    private static MDBListener mdbListener = null;


    private MDBListener() {
    }

    public static MDBListener getInstance(){
        if (mdbListener==null){
            mdbListener= new MDBListener();
        }
        return mdbListener;
    }

    @Override
    public void run() {
        System.out.println("MDB listener started");
        InetAddress addr = ConfigManager.getConfigManager().getMdbAddr();
        int port = ConfigManager.getConfigManager().getmMDBport();


        MulticastServer receiver = new MulticastServer(addr , port);
        receiver.join();

        try {
            //TODO: get a way to stop this
            while (true){
                final byte[] message;

                message = receiver.receiveMessage();

                //TODO: split message

                //TODO: Launch MDB HANDLER
                //ConfigManager.getConfigManager()
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
