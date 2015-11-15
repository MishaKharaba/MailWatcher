package ExchangeActiveSync;

import org.w3c.dom.Node;

// This class represents an <Add>, <Change>,
// <Delete>, or <SoftDelete> node in a
// Sync command response.
public class ServerSyncCommand {
    private EasSyncCommand.Type type = EasSyncCommand.Type.Invalid;
    private String serverId;
    private String itemClass;
    private Node appDataXml;

    public ServerSyncCommand(EasSyncCommand.Type commandType, String id, Node appData, String changedItemClass)
            throws Exception {
        setType(commandType);
        setServerId(id);
        setAppDataXml(appData);
        setItemClass(changedItemClass);
    }

    public EasSyncCommand.Type getType() throws Exception {
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

    public void setType(EasSyncCommand.Type type) {
        this.type = type;
    }

    public EasMessage getMessage() throws Exception {
/*	
     <ApplicationData>
		<email:To>&quot;Mykhaylo Kharaba&quot; &lt;Mykhaylo.Kharaba@eleks.com&gt;</email:To>
		<email:From>&quot;Heartbreak&quot; &lt;Heartbreak@enjoylove.in.net&gt;</email:From>
		<email:Subject>Read This if you miss your ex</email:Subject>
		<email:DateReceived>2015-11-13T11:32:04.947Z</email:DateReceived>
		<email:DisplayTo>Mykhaylo Kharaba</email:DisplayTo>
		<email:ThreadTopic>Read This if you miss your ex</email:ThreadTopic>
		<email:Importance>1</email:Importance>
		<email:Read>0</email:Read>
		<airsyncbase:Body>
			<airsyncbase:Type>2</airsyncbase:Type>
			<airsyncbase:EstimatedDataSize>5842</airsyncbase:EstimatedDataSize>
			<airsyncbase:Truncated>1</airsyncbase:Truncated>
		</airsyncbase:Body>
		<email:MessageClass>IPM.Note</email:MessageClass>
		<email:InternetCPID>65001</email:InternetCPID>
		<email:Flag/>
		<email:ContentClass>urn:content-classes:message</email:ContentClass>
		<airsyncbase:NativeBodyType>2</airsyncbase:NativeBodyType>
		<email2:ConversationId><![CDATA[D9 52 2A 4F 1E CF A1 4D 8F 81 E4 78 62 CA 34 A1 ]]></email2:ConversationId>
		<email2:ConversationIndex><![CDATA[01 D1 1E 06 EC ]]></email2:ConversationIndex>
		<email:Categories/>
	</ApplicationData>
*/
        if (getAppDataXml() == null)
            return null;

        EasMessage email = new EasMessage();
        Node node = getAppDataXml().getFirstChild();
        while (node != null) {
            String prefix = node.getPrefix();
            String namespace = node.getOwnerDocument().lookupNamespaceURI(prefix);
            if (namespace == "Email") {
                String nodeName = node.getLocalName();
                switch (nodeName) {
                    case "To":
                        email.setTo(node.getTextContent());
                        break;
                    case "From":
                        email.setFrom(node.getTextContent());
                        break;
                    case "Subject":
                        email.setSubject(node.getTextContent());
                        break;
                }
            }
            node = node.getNextSibling();
        }
        return email;
    }

}
