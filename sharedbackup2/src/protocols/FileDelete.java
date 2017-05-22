package protocols;

import backend.ConfigManager;
import backend.MulticastServer;
import sun.rmi.runtime.Log;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

public class FileDelete {
    private static FileDelete sInstance = null;

    public static FileDelete getInstance() {

        if (sInstance == null) {
            sInstance = new FileDelete();
        }
        return sInstance;
    }

    private FileDelete(){

    }

    public boolean deleteFile(String fileID){
        String message = "";

        message += "DELETE" +" "+"1.0"+ " "+ ConfigManager.getConfigManager().getMyID() +" " + fileID+ MulticastServer.CRLF
                + MulticastServer.CRLF;

        InetAddress mcAddr = ConfigManager.getConfigManager().getMcAddr();
        int mcPort = ConfigManager.getConfigManager().getmMCport();

        MulticastServer sender = new MulticastServer(mcAddr,
                mcPort);

        try {
            sender.sendMessage(message
                    .getBytes(MulticastServer.ASCII_CODE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }
}
