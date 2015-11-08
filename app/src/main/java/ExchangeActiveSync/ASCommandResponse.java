package ExchangeActiveSync;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import ExchangeActiveSync.ASError;
import ExchangeActiveSync.ASWBXML;

// This class represents a generic Exchange ActiveSync command response.
public class ASCommandResponse {
	private byte[] wbxmlBytes = null;
	private String xmlString = null;
	private int httpStatus = 200;
	private String httpStatusMessage = "OK";

	public byte[] getWbxmlBytes() throws Exception {
		return wbxmlBytes;
	}

	public String getXmlString() throws Exception {
		return xmlString;
	}

	public int getHttpStatus() throws Exception {
		return httpStatus;
	}

	public String getHttpStatusMessage() throws Exception {
		return httpStatusMessage;
	}

	public ASCommandResponse(HttpURLConnection connection) throws Exception {
		httpStatus = connection.getResponseCode();
		httpStatusMessage = connection.getResponseMessage();
		InputStream responseStream = connection.getInputStream();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] byteBuffer = new byte[256];
		int count = 0;
		// Read the WBXML data from the response stream
		// 256 bytes at a time.
		count = responseStream.read(byteBuffer, 0, 256);
		while (count > 0) {
			// Add the 256 bytes to the List
			bytes.write(byteBuffer, 0, count);
			// Read the next 256 bytes from the response stream
			count = responseStream.read(byteBuffer, 0, 256);
		}
		wbxmlBytes = bytes.toByteArray();
		// Decode the WBXML
		xmlString = decodeWBXML(wbxmlBytes);
	}

	// This function uses the ASWBXML class to decode
	// a WBXML stream into XML.
	private String decodeWBXML(byte[] wbxml) throws Exception {
		try {
			ASWBXML decoder = new ASWBXML();
			decoder.loadBytes(wbxml);
			return decoder.getXml();
		} catch (Exception ex) {
			ASError.reportException(ex);
			return "";
		}

	}

}
