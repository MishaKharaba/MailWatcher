package ExchangeActiveSync;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// This class represents a FolderSync command
// response as specified in MS-ASCMD section 2.2.2.4.2.
public class ASFolderSyncResponse extends ASCommandResponse {
    private Document responseXml = null;
    private int status = 0;
    private String syncKey;

    public int getStatus() {
        return status;
    }

    public String getSyncKey() {
        return syncKey;
    }

    public ASFolderSyncResponse(HttpURLConnection connection) throws Exception {
        super(connection);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(getXmlString()));
        responseXml = builder.parse(is);
        // xmlNsMgr.AddNamespace(Xmlns.folderHierarchyXmlns,
        // Namespaces.folderHierarchyNamespace);
        setStatus();
    }

    // This function updates a folder tree based on the
    // changes received from the server in the response.
    public List<FolderInfo> getFolderList() throws Exception {
        List<FolderInfo> result = new ArrayList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        // Process adds (new folders) first
        NodeList addNodes = (NodeList) xPath.evaluate(".//FolderHierarchy:Add", responseXml, XPathConstants.NODESET);
        for (int i = 0, n = addNodes.getLength(); i < n; i++) {
            Node addNode = (Node) addNodes.item(i);
            FolderInfo fi = new FolderInfo();
            nodeToFolderInfo(addNode, fi);
            fi.action = FolderInfo.Action.Added;
            result.add(fi);
        }
        // Then process deletes
        NodeList deleteNodes = (NodeList) xPath.evaluate(".//FolderHierarchy:Delete", responseXml, XPathConstants.NODESET);
        for (int i = 0, n = deleteNodes.getLength(); i < n; i++) {
            Node deleteNode = (Node) deleteNodes.item(i);
            FolderInfo fi = new FolderInfo();
            nodeToFolderInfo(deleteNode, fi);
            fi.action = FolderInfo.Action.Deleted;
            result.add(fi);
        }
        // Finally process any updates to existing folders
        NodeList updateNodes = (NodeList) xPath.evaluate(".//FolderHierarchy:Update", responseXml, XPathConstants.NODESET);
        for (int i = 0, n = updateNodes.getLength(); i < n; i++) {
            Node updateNode = (Node) updateNodes.item(i);
            FolderInfo fi = new FolderInfo();
            nodeToFolderInfo(updateNode, fi);
            fi.action = FolderInfo.Action.Updated;
            result.add(fi);
        }
        return result;
    }

    private void nodeToFolderInfo(Node node, FolderInfo fi) {
        node = node.getFirstChild();
        while (node != null) {
            String name = node.getLocalName();
            if ("DisplayName".equals(name))
                fi.name = node.getTextContent();
            else if ("ServerId".equals(name))
                fi.id = node.getTextContent();
            else if ("ParentId".equals(name))
                fi.parentId = node.getTextContent();
            else if ("Type".equals(name))
                fi.type = Integer.parseInt(node.getTextContent());
            node = node.getNextSibling();
        }
    }

    // This function extracts the response status from the
    // XML and sets the status property.
    private void setStatus() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put(null, "FolderHierarchy");
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node statusNode = (Node) xPath.evaluate(".//FolderHierarchy:FolderSync/FolderHierarchy:Status", responseXml,
                XPathConstants.NODE);
        if (statusNode != null)
            status = Integer.parseInt(statusNode.getTextContent());
        // Get sync key
        Node syncKeyNode = (Node) xPath.evaluate(".//FolderHierarchy:FolderSync/FolderHierarchy:SyncKey", responseXml,
                XPathConstants.NODE);
        if (syncKeyNode != null)
            syncKey = syncKeyNode.getTextContent();
    }

}
