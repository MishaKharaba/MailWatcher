package ExchangeActiveSync;

import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ExchangeActiveSync.ASCommandRequest;
import ExchangeActiveSync.ASCommandResponse;
import ExchangeActiveSync.ASSyncResponse;
import ExchangeActiveSync.FolderInfo;
import ExchangeActiveSync.FolderInfo.FolderSyncOptions;
import ExchangeActiveSync.FolderInfo.MimeSupport;
import ExchangeActiveSync.FolderInfo.MimeTruncationType;
import ExchangeActiveSync.FolderInfo.SyncFilterType;
import ExchangeActiveSync.Namespaces;
import ExchangeActiveSync.Xmlns;

// This class represents the Sync command request
public class ASSyncRequest extends ASCommandRequest {
	private int wait = 0;
	// 1 - 59 minutes
	private int heartBeatInterval = 0;
	// 60 - 3540 seconds
	private int windowSize = 0;
	// 1 - 512 changes
	private boolean isPartial = false;
	List<FolderInfo> folderList = null;

	public int getWait() throws Exception {
		return wait;
	}

	public void setWait(int value) throws Exception {
		wait = value;
	}

	public int getHeartBeatInterval() throws Exception {
		return heartBeatInterval;
	}

	public void setHeartBeatInterval(int value) throws Exception {
		heartBeatInterval = value;
	}

	public int getWindowSize() throws Exception {
		return windowSize;
	}

	public void setWindowSize(int value) throws Exception {
		windowSize = value;
	}

	public boolean getIsPartial() throws Exception {
		return isPartial;
	}

	public void setIsPartial(boolean value) throws Exception {
		isPartial = value;
	}

	public List<FolderInfo> getFolders() throws Exception {
		return folderList;
	}

	public ASSyncRequest() throws Exception {
		this.setCommand("Sync");
		this.folderList = new ArrayList<FolderInfo>();
	}

	// This function generates an ASSyncResponse from an
	// HTTP response.
	@Override
	protected ASCommandResponse wrapHttpResponse(HttpURLConnection connection) throws Exception {
		return new ASSyncResponse(connection);
	}

	// This function generates the XML request body
	// for the Sync request.
	@Override
	protected void generateXMLPayload() throws Exception {
		// If WBXML was explicitly set, use that
		if (getWbxmlBytes() != null)
			return;

		// Otherwise, use the properties to build the XML and then WBXML encode
		// it
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document syncXML = builder.newDocument();

		// XmlDeclaration xmlDeclaration = syncXML.CreateXmlDeclaration("1.0",
		// "utf-8", null);
		// syncXML.InsertBefore(xmlDeclaration, null);

		Node syncNode = syncXML.createElementNS(Namespaces.airSyncNamespace, Xmlns.airSyncXmlns + ":Sync");
		syncXML.appendChild(syncNode);
		// Only add a collections node if there are folders in the request.
		// If omitting, there should be a Partial element.
		if (folderList.size() == 0 && isPartial == false)
			throw new Exception("Sync requests must specify collections or include the Partial element.");

		if (folderList.size() > 0) {
			Node collectionsNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":Collections");
			syncNode.appendChild(collectionsNode);
			for (FolderInfo folder : folderList) {
				Node collectionNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
						Xmlns.airSyncXmlns + ":Collection");
				collectionsNode.appendChild(collectionNode);
				Node syncKeyNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
						Xmlns.airSyncXmlns + ":SyncKey");
				syncKeyNode.setTextContent(folder.syncKey);
				collectionNode.appendChild(syncKeyNode);
				Node collectionIdNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
						Xmlns.airSyncXmlns + ":CollectionId");
				collectionIdNode.setTextContent(folder.id);
				collectionNode.appendChild(collectionIdNode);
				// To override "ghosting", you must include a Supported element
				// here.
				// This only applies to calendar items and contacts
				// NOT IMPLEMENTED
				// If folder is set to permanently delete items, then add a
				// DeletesAsMoves
				// element here and set it to false.
				// Otherwise, omit. Per MS-ASCMD, the absence of this element is
				// the same as true.
				if (folder.areDeletesPermanent == true) {
					Node deletesAsMovesNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
							Xmlns.airSyncXmlns + ":DeletesAsMoves");
					deletesAsMovesNode.setTextContent("0");
					collectionNode.appendChild(deletesAsMovesNode);
				}

				// In almost all cases the GetChanges element can be omitted.
				// It only makes sense to use it if SyncKey != 0 and you don't
				// want
				// changes from the server for some reason.
				if (folder.areChangesIgnored == true) {
					Node getChangesNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
							Xmlns.airSyncXmlns + ":GetChanges");
					getChangesNode.setTextContent("0");
					collectionNode.appendChild(getChangesNode);
				}

				// If there's a folder-level window size, include it
				if (folder.windowSize > 0) {
					Node folderWindowSizeNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
							Xmlns.airSyncXmlns + ":WindowSize");
					folderWindowSizeNode.setTextContent(((Integer) folder.windowSize).toString());
					collectionNode.appendChild(folderWindowSizeNode);
				}

				// If the folder is set to conversation mode, specify that
				if (folder.useConversationMode == true) {
					Node conversationModeNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
							Xmlns.airSyncXmlns + ":ConversationMode");
					conversationModeNode.setTextContent("1");
					collectionNode.appendChild(conversationModeNode);
				}

				// Include sync options for the folder
				// Note that you can include two Options elements, but the 2nd
				// one is for SMS
				// SMS is not implemented at this time, so we'll only include
				// one.
				if (folder.options != null) {
					generateOptionsXml(folder.options, collectionNode);

				}
			}
		}

		// Include client-side changes
		// TODO: Implement client side changes on the Folder object
		// if (folder.Commands != null)
		// {
		// folder.GenerateCommandsXml(collectionNode);
		// }
		// If a wait period was specified, include it here
		if (wait > 0) {
			Node waitNode = syncXML.createElementNS(Namespaces.airSyncNamespace, Xmlns.airSyncXmlns + ":Wait");
			waitNode.setTextContent(((Integer) wait).toString());
			syncNode.appendChild(waitNode);
		}

		// If a heartbeat interval period was specified, include it here
		if (heartBeatInterval > 0) {
			Node heartBeatIntervalNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":HeartbeatInterval");
			heartBeatIntervalNode.setTextContent(((Integer) heartBeatInterval).toString());
			syncNode.appendChild(heartBeatIntervalNode);
		}

		// If a windows size was specified, include it here
		if (windowSize > 0) {
			Node windowSizeNode = syncXML.createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":WindowSize");
			windowSizeNode.setTextContent(((Integer) windowSize).toString());
			syncNode.appendChild(windowSizeNode);
		}

		// If this request contains a partial list of collections, include the
		// Partial element
		if (isPartial == true) {
			Node partialNode = syncXML.createElementNS(Namespaces.airSyncNamespace, Xmlns.airSyncXmlns + ":Partial");
			syncNode.appendChild(partialNode);
		}

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(syncXML), new StreamResult(writer));
		String output = writer.getBuffer().toString();
		setXmlString(output);
	}

	// This function generates an <Options> node for a Sync
	// command based on the settings for this folder.
	public void generateOptionsXml(FolderSyncOptions options, Node rootNode) {
		Node optionsNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncNamespace,
				Xmlns.airSyncXmlns + ":Options");
		rootNode.appendChild(optionsNode);

		if (options.FilterType != SyncFilterType.NoFilter) {
			Node filterTypeNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":FilterType");
			int filterTypeAsInteger = options.FilterType.ordinal();
			filterTypeNode.setTextContent(((Integer) filterTypeAsInteger).toString());
			optionsNode.appendChild(filterTypeNode);
		}

		if (options.Class != null) {
			Node classNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":Class");
			classNode.setTextContent(options.Class);
			optionsNode.appendChild(classNode);
		}

		if (options.BodyPreference != null && options.BodyPreference.length > 0 && options.BodyPreference[0] != null) {
			Node bodyPreferenceNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
					Xmlns.airSyncBaseXmlns + ":BodyPreference");
			optionsNode.appendChild(bodyPreferenceNode);

			Node typeNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
					Xmlns.airSyncBaseXmlns + ":Type");
			int typeAsInteger = options.BodyPreference[0].Type.ordinal();
			typeNode.setTextContent(((Integer) typeAsInteger).toString());
			bodyPreferenceNode.appendChild(typeNode);

			if (options.BodyPreference[0].TruncationSize > 0) {
				Node truncationSizeNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
						Xmlns.airSyncBaseXmlns + ":TruncationSize");
				truncationSizeNode.setTextContent(((Long) options.BodyPreference[0].TruncationSize).toString());
				bodyPreferenceNode.appendChild(truncationSizeNode);
			}

			if (options.BodyPreference[0].AllOrNone == true) {
				Node allOrNoneNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
						Xmlns.airSyncBaseXmlns + ":AllOrNone");
				allOrNoneNode.setTextContent("1");
				bodyPreferenceNode.appendChild(allOrNoneNode);
			}

			if (options.BodyPreference[0].Preview > -1) {
				Node previewNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
						Xmlns.airSyncBaseXmlns + ":Preview");
				previewNode.setTextContent(((Integer) options.BodyPreference[0].Preview).toString());
				bodyPreferenceNode.appendChild(previewNode);
			}
		}

		if (options.BodyPartPreference != null) {
			Node bodyPartPreferenceNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
					Xmlns.airSyncBaseXmlns + ":BodyPartPreference");
			optionsNode.appendChild(bodyPartPreferenceNode);

			Node typeNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
					Xmlns.airSyncBaseXmlns + ":Type");
			int typeAsInteger = options.BodyPartPreference.Type.ordinal();
			typeNode.setTextContent(((Integer) typeAsInteger).toString());
			bodyPartPreferenceNode.appendChild(typeNode);

			if (options.BodyPartPreference.TruncationSize > 0) {
				Node truncationSizeNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
						Xmlns.airSyncBaseXmlns + ":TruncationSize");
				truncationSizeNode.setTextContent(((Long) options.BodyPreference[0].TruncationSize).toString());
				bodyPartPreferenceNode.appendChild(truncationSizeNode);
			}

			if (options.BodyPartPreference.AllOrNone == true) {
				Node allOrNoneNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
						Xmlns.airSyncBaseXmlns + ":AllOrNone");
				allOrNoneNode.setTextContent("1");
				bodyPartPreferenceNode.appendChild(allOrNoneNode);
			}

			if (options.BodyPartPreference.Preview > -1) {
				Node previewNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncBaseNamespace,
						Xmlns.airSyncBaseXmlns + ":Preview");
				previewNode.setTextContent(((Integer) options.BodyPreference[0].Preview).toString());
				bodyPartPreferenceNode.appendChild(previewNode);
			}
		}

		if (options.MimeSupportLevel != MimeSupport.NeverSendMime) {
			Node mimeSupportNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":MIMESupport");
			int mimeSupportLevelAsInteger = options.MimeSupportLevel.ordinal();
			mimeSupportNode.setTextContent(((Integer) mimeSupportLevelAsInteger).toString());
			optionsNode.appendChild(mimeSupportNode);
		}

		if (options.MimeTruncation != MimeTruncationType.NoTruncate) {
			Node mimeTruncationNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":MIMETruncation");
			int mimeTruncationAsInteger = options.MimeTruncation.ordinal();
			mimeTruncationNode.setTextContent(((Integer) mimeTruncationAsInteger).toString());
			optionsNode.appendChild(mimeTruncationNode);
		}

		if (options.MaxItems > -1) {
			Node maxItemsNode = rootNode.getOwnerDocument().createElementNS(Namespaces.airSyncNamespace,
					Xmlns.airSyncXmlns + ":MaxItems");
			maxItemsNode.setTextContent(((Integer) options.MaxItems).toString());
			optionsNode.appendChild(maxItemsNode);
		}

		if (options.IsRightsManagementSupported == true) {
			Node rightsManagementSupportNode = rootNode.getOwnerDocument().createElementNS(
					Namespaces.rightsManagementNamespace, Xmlns.rightsManagementXmlns + ":RightsManagementSupport");
			rightsManagementSupportNode.setTextContent("1");
			optionsNode.appendChild(rightsManagementSupportNode);
		}
	}

}
