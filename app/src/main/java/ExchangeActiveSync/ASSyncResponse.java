package ExchangeActiveSync;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ExchangeActiveSync.ASCommandResponse;
import ExchangeActiveSync.Namespaces;
import ExchangeActiveSync.ServerSyncCommand;
import ExchangeActiveSync.EasSyncCommand;
import ExchangeActiveSync.EasSyncCommand.Type;
import ExchangeActiveSync.Xmlns;

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

	private XPath createXPath() {
		XPath xPath = XPathFactory.newInstance().newXPath();
		// NamespaceContext nsContext = xmlDoc.lookupNamespaceURI(prefix);
		xPath.setNamespaceContext(new NamespaceContext() {
			@Override
			public Iterator<String> getPrefixes(String namespaceURI) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getPrefix(String namespaceURI) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getNamespaceURI(String prefix) {
				if (Xmlns.airSyncXmlns.equals(prefix)) {
					return Namespaces.airSyncNamespace;
				} else if (Xmlns.airSyncBaseXmlns.equals(prefix)) {
					return Namespaces.airSyncBaseNamespace;
				} else {
					return "";
				}
			}
		});
		return xPath;
	}

	// This function gets the sync key for
	// a folder.
	public String getSyncKeyForFolder(String folderId) throws Exception {
		String folderSyncKey = "0";
		String collectionXPath = ".//airsync:Collection[airsync:CollectionId = \"" + folderId + "\"]/airsync:SyncKey";
		XPath xPath = createXPath();
		Node syncKeyNode = (Node) xPath.evaluate(collectionXPath, responseXml, XPathConstants.NODE);
		if (syncKeyNode != null)
			folderSyncKey = syncKeyNode.getTextContent();
		return folderSyncKey;
	}

	// This function returns the new items (Adds)
	// for a folder.
	public List<ServerSyncCommand> getServerSyncsForFolder(String folderId) throws Exception {
		List<ServerSyncCommand> srvCommands = new ArrayList<>();
		XPath xPath = createXPath();
		String collectionXPath = ".//airsync:Collection[airsync:CollectionId = \"" + folderId
				+ "\"]/airsync:Commands/*";
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
			Node serverIdNode = (Node) xPath.evaluate("./airsync:ServerId", cmdNode, XPathConstants.NODE);
			Node applicationDataNode = (Node) xPath.evaluate("./airsync:ApplicationData", cmdNode, XPathConstants.NODE);
			if (serverIdNode != null && applicationDataNode != null) {
				String serverId = serverIdNode.getTextContent();
				ServerSyncCommand srvCommand = new ServerSyncCommand(cmdType, serverId, applicationDataNode, null);
				srvCommands.add(srvCommand);
			}
		}
		return srvCommands;
	}

	// This function extracts the response status from the
	// XML and sets the status property.
	private void setStatus() throws Exception {
		XPath xPath = createXPath();
		Node statusNode = (Node) xPath.evaluate(".//airsync:Sync//airsync:Status", responseXml, XPathConstants.NODE);
		if (statusNode != null)
			status = Integer.parseInt((statusNode.getTextContent()));
	}

}
