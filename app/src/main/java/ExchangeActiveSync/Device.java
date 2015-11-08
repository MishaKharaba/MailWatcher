package ExchangeActiveSync;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// This class represents a "device" and is used to
// generate a DeviceInformation Xml element, as specified
// in [MS-ASCMD] section 2.2.3.45.
public class Device {
	static final String strSettingsXmlns = "settings";
	static final String strSettingsNamespace = "Settings";
	private String deviceID = null;
	private String deviceType = null;
	private String model = null;
	private String IMEINumber = null;
	private String friendlyName = null;
	private String operatingSystem = null;
	private String operatingSystemLanguage = null;
	private String phoneNumber = null;
	private String mobileOperator = null;
	private String userAgent = null;

	public String getDeviceID() throws Exception {
		return deviceID;
	}

	public void setDeviceID(String value) throws Exception {
		deviceID = value;
	}

	public String getDeviceType() throws Exception {
		return deviceType;
	}

	public void setDeviceType(String value) throws Exception {
		deviceType = value;
	}

	public String getModel() throws Exception {
		return model;
	}

	public void setModel(String value) throws Exception {
		model = value;
	}

	public String getIMEI() throws Exception {
		return IMEINumber;
	}

	public void setIMEI(String value) throws Exception {
		IMEINumber = value;
	}

	public String getFriendlyName() throws Exception {
		return friendlyName;
	}

	public void setFriendlyName(String value) throws Exception {
		friendlyName = value;
	}

	public String getOperatingSystem() throws Exception {
		return operatingSystem;
	}

	public void setOperatingSystem(String value) throws Exception {
		operatingSystem = value;
	}

	public String getOperatingSystemLanguage() throws Exception {
		return operatingSystemLanguage;
	}

	public void setOperatingSystemLanguage(String value) throws Exception {
		operatingSystemLanguage = value;
	}

	public String getPhoneNumber() throws Exception {
		return phoneNumber;
	}

	public void setPhoneNumber(String value) throws Exception {
		phoneNumber = value;
	}

	public String getMobileOperator() throws Exception {
		return mobileOperator;
	}

	public void setMobileOperator(String value) throws Exception {
		mobileOperator = value;
	}

	public String getUserAgent() throws Exception {
		return userAgent;
	}

	public void setUserAgent(String value) throws Exception {
		userAgent = value;
	}

	// This function generates and returns an XmlNode for the
	// DeviceInformation element.
	public Node getDeviceInformationNode() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xmlDoc = builder.newDocument();

		Element deviceInfoElement = xmlDoc.createElementNS(strSettingsNamespace,
				strSettingsXmlns + ":DeviceInformation");
		xmlDoc.appendChild(deviceInfoElement);
		Element setElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":Set");
		deviceInfoElement.appendChild(setElement);
		if (getModel() != null) {
			Element modelElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":Model");
			modelElement.setNodeValue(getModel());
			setElement.appendChild(modelElement);
		}

		if (getIMEI() != null) {
			Element IMEIElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":IMEI");
			IMEIElement.setNodeValue(getIMEI());
			setElement.appendChild(IMEIElement);
		}

		if (getFriendlyName() != null) {
			Element friendlyNameElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":FriendlyName");
			friendlyNameElement.setNodeValue(getFriendlyName());
			setElement.appendChild(friendlyNameElement);
		}

		if (getOperatingSystem() != null) {
			Element operatingSystemElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":OS");
			operatingSystemElement.setNodeValue(getOperatingSystem());
			setElement.appendChild(operatingSystemElement);
		}

		if (getOperatingSystemLanguage() != null) {
			Element operatingSystemLanguageElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":OSLanguage");
			operatingSystemLanguageElement.setNodeValue(getOperatingSystemLanguage());
			setElement.appendChild(operatingSystemLanguageElement);
		}

		if (getPhoneNumber() != null) {
			Element phoneNumberElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":PhoneNumber");
			phoneNumberElement.setNodeValue(getPhoneNumber());
			setElement.appendChild(phoneNumberElement);
		}

		if (getMobileOperator() != null) {
			Element mobileOperatorElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":MobileOperator");
			mobileOperatorElement.setNodeValue(getMobileOperator());
			setElement.appendChild(mobileOperatorElement);
		}

		if (getUserAgent() != null) {
			Element userAgentElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":UserAgent");
			userAgentElement.setNodeValue(getUserAgent());
			setElement.appendChild(userAgentElement);
		}

		return xmlDoc.getDocumentElement();
	}

}
