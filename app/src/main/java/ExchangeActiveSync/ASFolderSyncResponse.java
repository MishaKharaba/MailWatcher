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
		XPath xPath = createXPath();
		// Process adds (new folders) first
		NodeList addNodes = (NodeList) xPath.evaluate(".//folderhierarchy:Add", responseXml, XPathConstants.NODESET);
		for (int i = 0, n = addNodes.getLength(); i < n; i++) {
			Node addNode = (Node) addNodes.item(i);
			FolderInfo fi = new FolderInfo();
			nodeToFolderInfo(addNode, fi);
			fi.action = FolderInfo.Action.Added;
			result.add(fi);
		}
		// Then process deletes
		NodeList deleteNodes = (NodeList) xPath.evaluate(".//folderhierarchy:Add", responseXml, XPathConstants.NODESET);
		for (int i = 0, n = deleteNodes.getLength(); i < n; i++) {
			Node deleteNode = (Node) deleteNodes.item(i);
			FolderInfo fi = new FolderInfo();
			nodeToFolderInfo(deleteNode, fi);
			fi.action = FolderInfo.Action.Deleted;
			result.add(fi);
		}
		// Finally process any updates to existing folders
		NodeList updateNodes = (NodeList) xPath.evaluate(".//folderhierarchy:Update", responseXml,
				XPathConstants.NODESET);
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
		XPath xPath = createXPath();
		Node statusNode = (Node) xPath.evaluate(".//folderhierarchy:FolderSync/folderhierarchy:Status", responseXml,
				XPathConstants.NODE);
		if (statusNode != null)
			status = Integer.parseInt(statusNode.getTextContent());
		// Get sync key
		Node syncKeyNode = (Node) xPath.evaluate(".//folderhierarchy:FolderSync/folderhierarchy:SyncKey", responseXml,
				XPathConstants.NODE);
		if (syncKeyNode != null)
			syncKey = syncKeyNode.getTextContent();
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
				if ("folderhierarchy".equals(prefix)) {
					return "FolderHierarchy";
				} else {
					return "";
				}
			}
		});
		return xPath;
	}

}
