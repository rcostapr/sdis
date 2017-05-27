package protocols;

public class FileRecord {
	private String fileID;
    private String username;

    public FileRecord(String fileID, String user) {
        this.setFileID(fileID);
        this.setUsername(user);
    }

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
