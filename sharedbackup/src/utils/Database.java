package utils;

import backend.Chunk;
import backend.ChunkData;
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


    //private Map<String, Integer> mDeletedFiles;

    public Database() {
        maxBackupSize = 40*1000;
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

        if (rChunk == null) {
            for (SavedFile file : savedFiles.values()) {
                if (file.getFileId().equals(fileId)) {
                    // I have the chunk in my own file
                    rChunk = file.getChunkList().get(chunkNo);
                    break;
                }
            }
        }

        return rChunk;
    }
    public synchronized void incChunkReplication(String fileId, int chunkNo)
            throws ConfigManager.InvalidChunkException {
        SavedFile file = savedFiles.get(fileId);
        System.out.println("size = " + savedFiles.size());
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

    public SavedFile getNewSavedFile(String path, int replication) throws
            SavedFile.FileTooLargeException,
            SavedFile.FileDoesNotExistsException,
            ConfigManager.FileAlreadySaved{
        SavedFile file = new SavedFile(path,replication);
        if (savedFiles.containsKey(file.getFileId())){
            throw new ConfigManager.FileAlreadySaved();
        }
        savedFiles.put(file.getFileId(),file);

        return file;
    }

    public void addChunk(Chunk chunk) {
        synchronized (savedChunks){
            savedChunks.add(chunk);
        }
    }

    public SavedFile getFileByPath(String path){
        for (SavedFile file : savedFiles.values()) {
            if (file.getFilePath().equals(path)) {
                return file;
            }
        }
        return null;
    }

    public void print() {

        //print MAX SPACE / USED SPACE
        System.out.println("MY FILES:");
        for (String key: savedFiles.keySet()
             ) {
            SavedFile file = savedFiles.get(key);
            System.out.println("file = " + file.getFilePath());
            System.out.println("FileId() = " + file.getFileId());
            System.out.println("REP DEG = " + file.getWantedReplicationDegree());
            file.showFileChunks();
            System.out.println();
        }
        System.out.println();
        System.out.println("Chunks Stored:");
        System.out.println();
        for (Chunk chunk:savedChunks
             ) {
            System.out.println("chunk ID = " + chunk.getFileID());
            System.out.println("chunk size = " + chunk.getSize());
            System.out.println("CurrentReplicationDeg = " + chunk.getCurrentReplicationDeg());
            System.out.println();
        }
    }
}
