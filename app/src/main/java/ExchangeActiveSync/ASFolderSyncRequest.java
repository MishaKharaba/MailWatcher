//
// Translated by CS2J (http://www.cs2j.com): 06/11/2015 15:37:33
//

package ExchangeActiveSync;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.HttpURLConnection;

// This class represents the FolderSync command
// request specified in MS-ASCMD section 2.2.2.4.1.
public class ASFolderSyncRequest extends ASCommandRequest {
    private String syncKey = "0";

    public String getSyncKey() throws Exception {
        return syncKey;
    }

    public void setSyncKey(String value) throws Exception {
        syncKey = value;
    }

    public ASFolderSyncRequest() throws Exception {
        setCommand("FolderSync");
    }

    // This function generates an ASFolderSyncResponse from an
    // HTTP response.
    protected ASCommandResponse wrapHttpResponse(HttpURLConnection connection) throws Exception {
        return new ASFolderSyncResponse(connection);
    }

    // This function generates the XML request body
    // for the FolderSync request.
    protected void generateXMLPayload() throws Exception {
        // If WBXML was explicitly set, use that
        if (getWbxmlBytes() != null)
            return;

        // Otherwise, use the properties to build the XML and then WBXML encode
        // it
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document folderSyncXML = builder.newDocument();

        // XmlDeclaration xmlDeclaration =
        // folderSyncXML.CreateXmlDeclaration("1.0", "utf-8", null);
        // folderSyncXML.insertBefore(xmlDeclaration, null);
        Node folderSyncNode = folderSyncXML.createElementNS(Namespaces.folderHierarchyNamespace,
                Xmlns.folderHierarchyXmlns + ":FolderSync");
        folderSyncXML.appendChild(folderSyncNode);
        if ("".equals(syncKey))
            syncKey = "0";

        Node syncKeyNode = folderSyncXML.createElementNS(Namespaces.folderHierarchyNamespace,
                Xmlns.folderHierarchyXmlns + ":SyncKey");
        syncKeyNode.setTextContent(syncKey);
        folderSyncNode.appendChild(syncKeyNode);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(folderSyncNode), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        setXmlString(output);
    }

}
