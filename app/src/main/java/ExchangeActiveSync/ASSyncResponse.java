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
import java.util.List;

// This class represents the Sync command response
public class ASSyncResponse extends ASCommandResponse {
    public enum SyncStatus {
        // This enumeration covers the possible Status
        // values for FolderSync responses.
        __dummyEnum__0, Success, __dummyEnum__1, InvalidSyncKey, ProtocolError, ServerError, ClientServerConversionError, ServerOverwriteConflict, ObjectNotFound, SyncCannotComplete, __dummyEnum__2, __dummyEnum__3, FolderHierarchyOutOfDate, PartialSyncNotValid, InvalidDelayValue, InvalidSync, Retry
    }

    private Document responseXml = null;
    private int status;

    public int getStatus() throws Exception {
        return status;
    }

    public ASSyncResponse(HttpURLConnection connection) throws Exception {
        super(connection);
        // Sync responses can be empty
        if (!"".equals(getXmlString())) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(getXmlString()));
            responseXml = builder.parse(is);

            setStatus();
        }

    }

    // This function gets the sync key for
    // a folder.
    public String getSyncKeyForFolder(String folderId) throws Exception {
        String folderSyncKey = "0";
        String collectionXPath = String.format(
                ".//AirSync:Collection[AirSync:CollectionId = \"%s\"]/AirSync:SyncKey", folderId);
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node syncKeyNode = (Node) xPath.evaluate(collectionXPath, responseXml, XPathConstants.NODE);
        if (syncKeyNode != null)
            folderSyncKey = syncKeyNode.getTextContent();
        return folderSyncKey;
    }

    // This function returns the new items (Adds)
    // for a folder.
    public List<ServerSyncCommand> getServerSyncsForFolder(String folderId) throws Exception {
        List<ServerSyncCommand> srvCommands = new ArrayList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        String collectionXPath = String.format(
                ".//AirSync:Collection[AirSync:CollectionId = \"%s\"]/AirSync:Commands/*",
                folderId);
        NodeList cmdNodes = (NodeList) xPath.evaluate(collectionXPath, responseXml, XPathConstants.NODESET);
        for (int i = 0, n = cmdNodes.getLength(); i < n; i++) {
            Node cmdNode = cmdNodes.item(i);
            String cmdTypeStr = cmdNode.getLocalName();
            EasSyncCommand.Type cmdType = EasSyncCommand.Type.Invalid;
            switch (cmdTypeStr) {
                case "Add":
                    cmdType = EasSyncCommand.Type.Add;
                    break;
                case "Change":
                    cmdType = EasSyncCommand.Type.Change;
                    break;
                case "Delete":
                    cmdType = EasSyncCommand.Type.Delete;
                    break;
                case "SoftDelete":
                    cmdType = EasSyncCommand.Type.SoftDelete;
                    break;
            }
            getServerSync(srvCommands, cmdNode, cmdType);
        }
        return srvCommands;
    }

    private void getServerSync(List<ServerSyncCommand> srvCommands, Node cmdNode, EasSyncCommand.Type cmdType) throws Exception {
        Node node = cmdNode.getFirstChild();
        String serverId = null;
        Node applicationDataNode = null;
        while (node != null) {
            String prefix = node.getPrefix();
            if ("AirSync".equals(node.getOwnerDocument().lookupNamespaceURI(prefix))) {
                String name = node.getNodeName();
                if (name.equals("ServerId")) {
                    serverId = node.getTextContent();
                } else if (name.equals("ApplicationData")) {
                    applicationDataNode = node;
                }
            }
            node = node.getNextSibling();
        }
        if (serverId != null && applicationDataNode != null) {
            ServerSyncCommand srvCommand = new ServerSyncCommand(cmdType, serverId, applicationDataNode, null);
            srvCommands.add(srvCommand);
        }
    }

    // This function extracts the response status from the
    // XML and sets the status property.
    private void setStatus() throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node statusNode = (Node) xPath.evaluate(".//AirSync:Sync//AirSync:Status", responseXml, XPathConstants.NODE);
        if (statusNode != null)
            status = Integer.parseInt((statusNode.getTextContent()));
    }

}
