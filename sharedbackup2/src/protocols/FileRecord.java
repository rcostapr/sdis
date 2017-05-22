package protocols;

public class FileRecord {
	private String fileName;
    private String hash;
    private int chunksCount;

    public FileRecord(String fileName, String hash, int chunksCount) {
        this.fileName = fileName;
        this.hash = hash;
        this.chunksCount = chunksCount;
    }

    public String getFileName() {
        return fileName;
    }

    public String getHash() {
        return hash;
    }

    public int getChunksCount() {
        return chunksCount;
    }
}
