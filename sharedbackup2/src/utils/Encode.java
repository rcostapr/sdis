package utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class Encode {
	public static String encodeFile(File file) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String fileID = file.getName() + file.lastModified() + file.length();

		MessageDigest mD = MessageDigest.getInstance("SHA-256");
		mD.update(fileID.getBytes("UTF-8"));

		byte[] fileIDhashed = mD.digest();

		// printHexBinary converts a byte[] into string
		return new String(DatatypeConverter.printHexBinary(fileIDhashed));

	}
	
	 public static String byteArrayToHexString(byte[] b) {
	        String result = "";
	        for (int i = 0; i < b.length; i++) {
	            result +=
	                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
	        }
	        return result;
	    }
}
