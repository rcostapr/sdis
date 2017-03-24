package utils;

import backend.Chunk;
import backend.ConfigManager;
import backend.SavedFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Duarte on 24-Mar-17.
 */
public class Database implements Serializable {


    public static final String FILE = "metadata.ser";

    private String folder;
    private long maxBackupSize; // Bytes

    private ArrayList<Chunk> savedChunks; // chunks from others
    private Map<String, SavedFile> savedFiles; // files from me
    private boolean initialized;

    //private Map<String, Integer> mDeletedFiles;

    public Database() {
        initialized = false;
        maxBackupSize = 0;
        folder = "";
        savedFiles = new HashMap<String, SavedFile>();
        savedChunks = new ArrayList<Chunk>();

        //mDeletedFiles = new HashMap<String, Integer>();
    }


    public long getMaxBackupSize() {
        return maxBackupSize;
    }

    public synchronized void setAvailSpace(long space)
            throws ConfigManager.InvalidBackupSizeException {
        if (space <= 0) {
            throw new ConfigManager.InvalidBackupSizeException();
        }

        maxBackupSize = space;

        checkInitialization();
    }

    private void checkInitialization() {
        if (!folder.equals("") && maxBackupSize != 0) {
            initialized = true;
            saveDatabase();
        }
    }
    public String getFolder() {
        return folder;
    }

    public synchronized void setFolder(String dest)
            throws ConfigManager.InvalidFolderException {

        File destination = new File(dest);
        if (!destination.exists()) {

            destination.mkdir();
            folder = destination.getAbsolutePath();
            String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                folder += "\\";
            } else {
                folder += "/";
            }

            checkInitialization();

        } else if (destination.isDirectory()) {
            folder = destination.getAbsolutePath();
            String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                folder += "\\";
            } else {
                folder += "/";
            }

            checkInitialization();
        } else {
            throw new ConfigManager.InvalidFolderException();
        }
    }

    public void saveDatabase() {
        try {

            FileOutputStream fileOut = new FileOutputStream("metadata.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();

        } catch (IOException i) {

        }
    }

    public Chunk getSavedChunk(String fileId, int chunkNo) {
        Chunk rChunk = null;

        synchronized (savedChunks) {
            for (Chunk chunk : savedChunks) {
                if (chunk.getFileID().equals(fileId)
                        && chunk.getChunkNo() == chunkNo) {
                    rChunk = chunk;
                    break;
                }
            }
        }

        /*
        if (rChunk == null) {
            for (SharedFile file : mSharedFiles.values()) {
                if (file.getFileId().equals(fileId)) {
                    // I have the chunk in my own file
                    retChunk = file.getChunkList().get(chunkNo);
                    break;
                }
            }
        }
*/
        return rChunk;
    }
    public synchronized void incChunkReplication(String fileId, int chunkNo)
            throws ConfigManager.InvalidChunkException {
        SavedFile file = savedFiles.get(fileId);
        if (file != null) {
            // This is the owner machine of chunk's parent file
            file.incChunkReplication(chunkNo);
        } else {
            // It is a chunk saved in a backup operation
            Chunk chunk = null;
            int nrSavedChunks = savedChunks.size();
            for (int i = 0; i < nrSavedChunks; i++) {
                chunk = savedChunks.get(i);
                if (chunk.getFileID().equals(fileId)
                        && chunk.getChunkNo() == chunkNo) {
                    break;
                }
            }

            if (chunk != null) {
                chunk.incCurrentReplication();

            } else {
                throw new ConfigManager.InvalidChunkException();
            }
        }
    }
}
