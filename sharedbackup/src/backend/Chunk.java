package backend;

/**
 * Created by Duarte on 18-Mar-17.
 */
public class Chunk {

    public static int MAX_CHUNK_SIZE = 64000;

    private SavedFile file;
    private String fileID;
    private long chunkNo;
    private int currentReplicationDegree;
    private int wantedReplicationDegree;

    //private boolean isOwnMachineFile;

    public Chunk(SavedFile tFile, long tChunkno){
        file=tFile;
        chunkNo = tChunkno;
        fileID = tFile.getFileId();
        currentReplicationDegree=0;
        wantedReplicationDegree = tFile.getWantedReplicationDegree();

    }

    public String buildChunkName(){
        
    }
}
