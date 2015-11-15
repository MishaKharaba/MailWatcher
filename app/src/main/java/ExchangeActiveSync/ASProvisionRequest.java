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

// This class represents a Provision command request
// as specified in MS-ASPROV section 2.2.
public class ASProvisionRequest extends ASCommandRequest {
    private static String policyType = "MS-EAS-Provisioning-WBXML";
    private boolean isAcknowledgement = false;
    private boolean isRemoteWipe = false;
    private int status = 0;
    private Device provisionDevice = null;

    public ASProvisionRequest() throws Exception {
        setCommand("Provision");
    }

    public boolean getIsAcknowledgement() throws Exception {
        return isAcknowledgement;
    }

    public void setIsAcknowledgement(boolean value) throws Exception {
        isAcknowledgement = value;
    }

    public boolean getIsRemoteWipe() throws Exception {
        return isRemoteWipe;
    }

    public void setIsRemoteWipe(boolean value) throws Exception {
        isRemoteWipe = value;
    }

    public Device getProvisionDevice() throws Exception {
        return provisionDevice;
    }

    public void setProvisionDevice(Device value) throws Exception {
        provisionDevice = value;
    }

    public int getStatus() throws Exception {
        return status;
    }

    public void setStatus(int value) throws Exception {
        status = value;
    }

    // This function generates an ASProvisionResponse from an
    // HTTP response.
    protected ASCommandResponse wrapHttpResponse(HttpURLConnection connection) throws Exception {
        return new ASProvisionResponse(connection);
    }

    // This function generates the XML request body
    // for the Provision request.
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
        Document provisionXML = builder.newDocument();

        // XmlDeclaration xmlDeclaration =
        // provisionXML.CreateXmlDeclaration("1.0", "utf-8", null);
        // provisionXML.InsertBefore(xmlDeclaration, null);
        Node provisionNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                Xmlns.provisionXmlns + ":Provision");
        provisionXML.appendChild(provisionNode);
        // If this is a remote wipe acknowledgment, use
        // the remote wipe acknowledgment format
        // specified in MS-ASPROV section 3.1.5.1.2.2.
        if (isRemoteWipe) {
            // Build response to RemoteWipe request
            Node remoteWipeNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                    Xmlns.provisionXmlns + ":RemoteWipe");
            provisionNode.appendChild(remoteWipeNode);
            // Always return success for remote wipe
            Node statusNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                    Xmlns.provisionXmlns + ":Status");
            statusNode.setTextContent("1");
            remoteWipeNode.appendChild(statusNode);
        } else {
            // The other two possibilities here are
            // an initial request or an acknowledgment
            // of a policy received in a previous Provision
            // response.
            if (!isAcknowledgement) {
                // A DeviceInformation node is only included in the initial
                // request.
                if (provisionDevice != null) {
                    Node deviceNode = provisionXML.importNode(provisionDevice.getDeviceInformationNode(), true);
                    provisionNode.appendChild(deviceNode);
                }
            }

            // These nodes are included in both scenarios.
            Node policiesNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                    Xmlns.provisionXmlns + ":Policies");
            provisionNode.appendChild(policiesNode);
            Node policyNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                    Xmlns.provisionXmlns + ":Policy");
            policiesNode.appendChild(policyNode);
            Node policyTypeNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                    Xmlns.provisionXmlns + ":PolicyType");
            policyTypeNode.setTextContent(policyType);
            policyNode.appendChild(policyTypeNode);
            if (isAcknowledgement) {
                // Need to also include policy key and status
                // when acknowledging
                Node policyKeyNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                        Xmlns.provisionXmlns + ":PolicyKey");
                policyKeyNode.setTextContent(((Long) getPolicyKey()).toString());
                policyNode.appendChild(policyKeyNode);
                Node statusNode = provisionXML.createElementNS(Namespaces.provisionNamespace,
                        Xmlns.provisionXmlns + ":Status");
                statusNode.setTextContent(((Integer) status).toString());
                policyNode.appendChild(statusNode);
            }

        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(provisionXML), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        setXmlString(output);
    }

}
