//
// Translated by CS2J (http://www.cs2j.com): 06/11/2015 15:37:33
//

package ExchangeActiveSync;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ExchangeActiveSync.ASCommandResponse;
import ExchangeActiveSync.ASError;
import ExchangeActiveSync.ASWBXML;
import ExchangeActiveSync.CommandParameter;

// This class represents a generic Exchange ActiveSync command request.
public class ASCommandRequest {
	private String encodedCredential = null;
	private String server = null;
	private boolean useSSL = true;
	private byte[] wbxmlBytes = null;
	private String xmlString = null;
	private String protocolVersion = null;
	private String requestLine = null;
	private String command = null;
	private String user = null;
	private String deviceID = null;
	private String deviceType = null;
	private long policyKey = 0;
	private CommandParameter[] parameters = null;

	public String getEncodedCredentials() throws Exception {
		return encodedCredential;
	}

	public void setEncodedCredentials(String value) throws Exception {
		encodedCredential = value;
	}

	public String getServer() throws Exception {
		return server;
	}

	public void setServer(String value) throws Exception {
		server = value;
	}

	public boolean getUseSSL() throws Exception {
		return useSSL;
	}

	public void setUseSSL(boolean value) throws Exception {
		useSSL = value;
	}

	public byte[] getWbxmlBytes() throws Exception {
		return wbxmlBytes;
	}

	public void setWbxmlBytes(byte[] value) throws Exception {
		wbxmlBytes = value;
		// Loading WBXML bytes causes immediate decoding
		xmlString = decodeWBXML(wbxmlBytes);
	}

	public String getXmlString() throws Exception {
		return xmlString;
	}

	public void setXmlString(String value) throws Exception {
		xmlString = value;
		// Loading XML causes immediate encoding
		wbxmlBytes = encodeXMLString(xmlString);
	}

	public String getProtocolVersion() throws Exception {
		return protocolVersion;
	}

	public void setProtocolVersion(String value) throws Exception {
		protocolVersion = value;
	}

	public String getRequestLine() throws Exception {
		// Generate on demand
		buildRequestLine();
		return requestLine;
	}

	public void setRequestLine(String value) throws Exception {
		requestLine = value;
	}

	public String getCommand() throws Exception {
		return command;
	}

	public void setCommand(String value) throws Exception {
		command = value;
	}

	public String getUser() throws Exception {
		return user;
	}

	public void setUser(String value) throws Exception {
		user = value;
	}

	public String getDeviceID() throws Exception {
		return deviceID;
	}

	public void setDeviceID(String value) throws Exception {
		deviceID = value;
	}

	public String getDeviceType() throws Exception {
		return deviceType;
	}

	public void setDeviceType(String value) throws Exception {
		deviceType = value;
	}

	public long getPolicyKey() throws Exception {
		return policyKey;
	}

	public void setPolicyKey(long value) throws Exception {
		policyKey = value;
	}

	public CommandParameter[] getCommandParameters() throws Exception {
		return parameters;
	}

	public void setCommandParameters(CommandParameter[] value) throws Exception {
		parameters = value;
	}

	// This function sends the request and returns
	// the response.
	public ASCommandResponse getResponse() throws Exception {
		generateXMLPayload();
		if (getEncodedCredentials() == null || getServer() == null || getProtocolVersion() == null
				|| getWbxmlBytes() == null)
			throw new Exception("ASCommandRequest not initialized.");

		// Generate the URI for the request
		String requestParams = getRequestLine();
		requestParams = URLEncoder.encode(requestParams, "UTF-8");
		String uriString = String.format("%s//%s/Microsoft-Server-ActiveSync?%s", useSSL ? "https:" : "http:", server,
				requestParams);
		URL url = new URL(uriString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Authorization", "Basic " + getEncodedCredentials());
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/vnd.ms-sync.wbxml");

		// Encoded request lines include the protocol version
		// and policy key in the request line.
		// Non-encoded request lines require that those
		// values be passed as headers.
		connection.setRequestProperty("MS-ASProtocolVersion", protocolVersion);
		connection.setRequestProperty("X-MS-PolicyKey", ((Long) getPolicyKey()).toString());

		try {
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(getWbxmlBytes());
			outputStream.close();

			connection.connect();
			ASCommandResponse response = wrapHttpResponse(connection);
			connection.disconnect();

			return response;
		} catch (Exception ex) {
			ASError.reportException(ex);
			return null;
		}
	}

	// This function generates an ASCommandResponse from an
	// HTTP response.
	protected ASCommandResponse wrapHttpResponse(HttpURLConnection connection) throws Exception {
		return new ASCommandResponse(connection);
	}

	// This function builds a request line from the class properties.
	protected void buildRequestLine() throws Exception {
		if (getCommand() == null || getUser() == null || getDeviceID() == null || getDeviceType() == null)
			throw new Exception("ASCommandRequest not initialized.");

		// Generate a plain-text request line.
		setRequestLine(String.format("Cmd=%s&User=%s&DeviceId=%s&DeviceType=%s", getCommand(), getUser(), getDeviceID(),
				getDeviceType()));
		if (getCommandParameters() != null) {
			for (int i = 0; i < parameters.length; i++) {
				setRequestLine(String.format("%s&%s=%s", getRequestLine(), getCommandParameters()[i].Parameter,
						getCommandParameters()[i].Value));
			}
		}
	}

	// This function generates an XML payload.
	protected void generateXMLPayload() throws Exception {
	}

	// For the base class, this is a no-op.
	// Classes that extend this class to implement
	// commands override this function to generate
	// the XML payload based on the command's request schema
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

	// This function uses the ASWBXML class to encode
	// XML into a WBXML stream.
	private byte[] encodeXMLString(String xmlString) throws Exception {
		try {
			ASWBXML encoder = new ASWBXML();
			encoder.loadXml(xmlString);
			return encoder.getBytes();
		} catch (Exception ex) {
			ASError.reportException(ex);
			return null;
		}

	}

}
