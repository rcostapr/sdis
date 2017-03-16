package backend;

import java.net.InetAddress;

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
        System.out.println("MDB listener started");
        InetAddress addr = ConfigManager.getConfigManager().getMDBAddr();
        int port = ConfigManager.getConfigManager().getMDBPort();
    }
}
