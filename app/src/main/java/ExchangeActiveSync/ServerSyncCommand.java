package ExchangeActiveSync;

import org.w3c.dom.Node;

// This class represents an <Add>, <Change>,
// <Delete>, or <SoftDelete> node in a
// Sync command response.
public class ServerSyncCommand {
	public enum ServerSyncCommandType {
		// This enumeration represents the types
		// of commands available.
		Invalid, Add, Change, Delete, SoftDelete
	}

	private ServerSyncCommandType type = ServerSyncCommandType.Invalid;
	private String serverId;
	private String itemClass;
	private Node appDataXml;

	public ServerSyncCommand(ServerSyncCommandType commandType, String id, Node appData, String changedItemClass)
			throws Exception {
		setType(commandType);
		setServerId(id);
		setAppDataXml(appData);
		setItemClass(changedItemClass);
	}

	public ServerSyncCommandType getType() throws Exception {
		return type;
	}

	public String getServerId() throws Exception {
		return serverId;
	}

	public String getItemClass() throws Exception {
		return itemClass;
	}

	public Node getAppDataXml() throws Exception {
		return appDataXml;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setItemClass(String itemClass) {
		this.itemClass = itemClass;
	}

	public void setAppDataXml(Node appDataXml) {
		this.appDataXml = appDataXml;
	}

	public void setType(ServerSyncCommandType type) {
		this.type = type;
	}

}
