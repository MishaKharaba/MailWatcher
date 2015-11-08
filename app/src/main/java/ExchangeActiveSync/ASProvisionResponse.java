//
// Translated by CS2J (http://www.cs2j.com): 06/11/2015 15:37:33
//

package ExchangeActiveSync;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ExchangeActiveSync.ASCommandResponse;
import ExchangeActiveSync.ASPolicy;

// This class represents the Provision command
// response specified in MS-ASPROV section 2.2.
public class ASProvisionResponse extends ASCommandResponse {
	private boolean isPolicyLoaded = false;
	private ASPolicy policy = null;
	private int status = 0;

	public ASProvisionResponse(HttpURLConnection connection) throws Exception {
		super(connection);
		policy = new ASPolicy();
		isPolicyLoaded = policy.loadXML(getXmlString());
		setStatus();
	}

	public boolean getIsPolicyLoaded() throws Exception {
		return isPolicyLoaded;
	}

	public ASPolicy getPolicy() throws Exception {
		return policy;
	}

	public int getStatus() throws Exception {
		return status;
	}

	// This function parses the response XML for
	// the Status element under the Provision element
	// and sets the status property according to the
	// value.
	private void setStatus() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(getXmlString()));
		Document xmlDoc = builder.parse(is);
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
				if ("provision".equals(prefix)) {
					return "Provision";
				} else {
					return "";
				}
			}
		});

		Node statusNode = (Node) xPath.evaluate(".//provision:Provision/provision:Status", xmlDoc, XPathConstants.NODE);
		if (statusNode != null) {
			String statusStr = statusNode.getTextContent();
			status = Integer.parseInt(statusStr);
		}
	}
}
