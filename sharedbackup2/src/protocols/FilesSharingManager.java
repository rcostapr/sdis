package protocols;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import backend.ConfigManager;
import backend.MulticastServer;

public class FilesSharingManager {
	public static final String ADD_FILE_CMD = "ADD_FILE";

    private static FilesSharingManager instance = null;

    private FilesSharingManager() {
    }

    public static FilesSharingManager getInstance() {
        if (instance == null) {
            instance = new FilesSharingManager();
        }
        return instance;
    }

    // ADD FILE <hash> <file-name> <access-level>
    public boolean addFileToSharedDB(FileRecord record) {

        InetAddress multCtrlAddr = ConfigManager.getConfigManager().getMcAddr();
        int multCtrlPort = ConfigManager.getConfigManager().getmMCport();

        MulticastServer sender = new MulticastServer(multCtrlAddr,multCtrlPort);

        String message;
        message = ADD_FILE_CMD + " " + record.getHash() + " " + record.getFileName() + " " + record.getChunksCount() + " " + MulticastServer.CRLF + MulticastServer.CRLF;

        try {
            sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("Sent ADD_FILE command for file " + record.getFileName() + " with hash " + record.getHash());

        return true;
    }
}
