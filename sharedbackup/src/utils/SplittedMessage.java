package utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import backend.MulticastServer;

public class SplittedMessage {

	private String header;
	private byte[] body;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public static SplittedMessage split(byte[] messg) {

		byte[] headerEnd = null;

		try {
			headerEnd = new String(MulticastServer.CRLF + MulticastServer.CRLF).getBytes(MulticastServer.ASCII_CODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int headerEndIndex = findIndexOfSubByteArray(messg, headerEnd);

		SplittedMessage splittedMessage = new SplittedMessage();

		splittedMessage.setHeader(new String(Arrays.copyOfRange(messg, 0, headerEndIndex)));

		try {
			splittedMessage.setBody(Arrays.copyOfRange(messg, headerEndIndex + 4, messg.length));
		} catch (ArrayIndexOutOfBoundsException e) {
			// there is no body
			splittedMessage.setBody(null);
		}

		return splittedMessage;
	}

	private static int findIndexOfSubByteArray(byte[] original, byte[] separator) {
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < separator.length; j++) {
				if (separator[j] != original[i + j]) {
					break;
				} else {
					if (j == separator.length - 1) {
						return i;
					}
				}
			}
		}
		// NO INDEX
		return -1;
	}
}
