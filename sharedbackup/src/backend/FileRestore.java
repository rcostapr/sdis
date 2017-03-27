package backend;

import protocols.ChunkRestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Duarte on 27-Mar-17.
 */
public class FileRestore {

    private static FileRestore fInstance = null;

    private FileRestore() {
    }

    public static FileRestore getInstance() {
        if (fInstance == null) {
            fInstance = new FileRestore();
        }
        return fInstance;
    }

    public boolean restoreFile (SavedFile file){
        ArrayList<ChunkData> receivedChunks = new ArrayList<ChunkData>();

        for (Chunk chunk:file.getChunkList()) {
            receivedChunks.add(ChunkRestore.getInstance().requestChunk(chunk.getFileID(),chunk.getChunkNo()));
        }
        if (receivedChunks.size() == file.getChunkList().size()){
            return rebuildFile(file,receivedChunks);
        }
        else return false;
    }

    private boolean rebuildFile(SavedFile file, ArrayList<ChunkData> chunks) {

        for (ChunkData chunk:chunks
                ) {
            if (!writeToFile(chunk.getData(), file.getFilePath())){
                return false;
            }
        }
        return true;
    }

    private boolean writeToFile(byte[] data, String filePath) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(filePath), true);

            out.write(data);
            out.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
