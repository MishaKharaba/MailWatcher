package ExchangeActiveSync;

import android.util.Base64;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// This class represents a generic Exchange ActiveSync command request.
public class ASCommandRequest {
    private String encodedCredential = null;
    private String server = null;
    private boolean ignoreCert;
    private byte[] wbxmlBytes = null;
    private String xmlString = null;
    private String protocolVersion = "14.1";
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

    public void setCredentials(String user, String pwd) throws Exception {
        String credential = user + ":" + pwd;
        encodedCredential = Base64.encodeToString(credential.getBytes("UTF-8"), Base64.DEFAULT);
    }

    public String getServer() throws Exception {
        return server;
    }

    public void setServer(String value) throws Exception {
        server = value;
    }

    public boolean getIgnoreCert() throws Exception {
        return ignoreCert;
    }

    public void setIgnoreCert(boolean value) throws Exception {
        ignoreCert = value;
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

    protected void setCommand(String value) throws Exception {
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

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws java.security.cert.CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    private static void ignoreCertificateError(HttpsURLConnection connection) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
        connection.setSSLSocketFactory(ctx.getSocketFactory());
    }

    // This function sends the request and returns
    // the response.
    public ASCommandResponse getResponse() throws Exception {
        generateXMLPayload();
        if (getEncodedCredentials() == null)
            throw new Exception("ASCommandRequest: Credentials not initialized.");
        if (getServer() == null)
            throw new Exception("ASCommandRequest: Server not initialized.");
        if (getProtocolVersion() == null)
            throw new Exception("ASCommandRequest: ProtocolVersion not initialized.");
        if (getWbxmlBytes() == null)
            throw new Exception("ASCommandRequest: WbxmlBytes not initialized.");

        // Generate the URI for the request
        String uriString = String.format("%s/Microsoft-Server-ActiveSync?%s", getServer(), getRequestLine());
        URL url = new URL(uriString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (getIgnoreCert() && (connection instanceof HttpsURLConnection)) {
            ignoreCertificateError((HttpsURLConnection) connection);
        }
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

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(getWbxmlBytes());
        outputStream.close();

        connection.connect();
        ASCommandResponse response = wrapHttpResponse(connection);
        connection.disconnect();

        return response;
    }

    // This function generates an ASCommandResponse from an
    // HTTP response.
    protected ASCommandResponse wrapHttpResponse(HttpURLConnection connection) throws Exception {
        return new ASCommandResponse(connection);
    }

    // This function builds a request line from the class properties.
    protected void buildRequestLine() throws Exception {
        if (getCommand() == null)
            throw new Exception("ASCommandRequest: Command not initialized.");
        if (getUser() == null)
            throw new Exception("ASCommandRequest: User not initialized.");
        if (getDeviceID() == null)
            throw new Exception("ASCommandRequest: DeviceID not initialized.");
        if (getDeviceType() == null)
            throw new Exception("ASCommandRequest: DeviceType not initialized.");

        // Generate a plain-text request line.
        setRequestLine(String.format("Cmd=%s&User=%s&DeviceId=%s&DeviceType=%s",
                URLEncoder.encode(getCommand(), "UTF-8"), URLEncoder.encode(getUser(), "UTF-8"),
                URLEncoder.encode(getDeviceID(), "UTF-8"), URLEncoder.encode(getDeviceType(), "UTF-8")));
        if (getCommandParameters() != null) {
            for (int i = 0; i < parameters.length; i++) {
                setRequestLine(String.format("%s&%s=%s", getRequestLine(), getCommandParameters()[i].Parameter,
                        URLEncoder.encode(getCommandParameters()[i].Value, "UTF-8")));
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
        ASWBXML decoder = new ASWBXML();
        decoder.loadBytes(wbxml);
        return decoder.getXml();
    }

    // This function uses the ASWBXML class to encode
    // XML into a WBXML stream.
    private byte[] encodeXMLString(String xmlString) throws Exception {
        ASWBXML encoder = new ASWBXML();
        encoder.loadXml(xmlString);
        return encoder.getBytes();
    }

}
