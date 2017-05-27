package protocols;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import backend.ConfigManager;
import backend.MulticastServer;
import backend.User;

public class UsersSharingManager {
	public static final String ADD_USER_CMD = "ADD_USER";

    private static UsersSharingManager instance = null;

    private UsersSharingManager() {
    }

    public static UsersSharingManager getInstance() {
        if (instance == null) {
            instance = new UsersSharingManager();
        }
        return instance;
    }

    public boolean addUserToSharedDB(User user) {

        InetAddress multCtrlAddr = ConfigManager.getConfigManager().getMcAddr();
        int multCtrlPort =  ConfigManager.getConfigManager().getmMCport();

        MulticastServer sender = new MulticastServer(multCtrlAddr,multCtrlPort);

        String message;

        message = ADD_USER_CMD + " " + "2.0"+ " " + ConfigManager.getConfigManager().getMyID() + " " + user.getUserName() + " " + user.getPassword() + " " + MulticastServer.CRLF + MulticastServer.CRLF;

        try {
            sender.sendMessage(message
                    .getBytes(MulticastServer.ASCII_CODE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("Sent ADD_USER command for user " + user.getUserName());

        return true;
    }
}
