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
	private String deviceID;
	private String deviceType;
	private String model;
	private String IMEINumber;
	private String friendlyName;
	private String operatingSystem;
	private String operatingSystemLanguage;
	private String phoneNumber;
	private String mobileOperator;
	private String userAgent;

	public String getDeviceID() throws Exception {
		return deviceID;
	}

	public void setDeviceID(String value) {
		deviceID = value;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String value) {
		deviceType = value;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String value) {
		model = value;
	}

	public String getIMEI() {
		return IMEINumber;
	}

	public void setIMEI(String value) {
		IMEINumber = value;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String value) {
		friendlyName = value;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String value) {
		operatingSystem = value;
	}

	public String getOperatingSystemLanguage() {
		return operatingSystemLanguage;
	}

	public void setOperatingSystemLanguage(String value) {
		operatingSystemLanguage = value;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String value) {
		phoneNumber = value;
	}

	public String getMobileOperator() {
		return mobileOperator;
	}

	public void setMobileOperator(String value) {
		mobileOperator = value;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String value) {
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
			modelElement.setTextContent(getModel());
			setElement.appendChild(modelElement);
		}

		if (getIMEI() != null) {
			Element IMEIElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":IMEI");
			IMEIElement.setTextContent(getIMEI());
			setElement.appendChild(IMEIElement);
		}

		if (getFriendlyName() != null) {
			Element friendlyNameElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":FriendlyName");
			friendlyNameElement.setTextContent(getFriendlyName());
			setElement.appendChild(friendlyNameElement);
		}

		if (getOperatingSystem() != null) {
			Element operatingSystemElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":OS");
			operatingSystemElement.setTextContent(getOperatingSystem());
			setElement.appendChild(operatingSystemElement);
		}

		if (getOperatingSystemLanguage() != null) {
			Element operatingSystemLanguageElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":OSLanguage");
			operatingSystemLanguageElement.setTextContent(getOperatingSystemLanguage());
			setElement.appendChild(operatingSystemLanguageElement);
		}

		if (getPhoneNumber() != null) {
			Element phoneNumberElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":PhoneNumber");
			phoneNumberElement.setTextContent(getPhoneNumber());
			setElement.appendChild(phoneNumberElement);
		}

		if (getMobileOperator() != null) {
			Element mobileOperatorElement = xmlDoc.createElementNS(strSettingsNamespace,
					strSettingsXmlns + ":MobileOperator");
			mobileOperatorElement.setTextContent(getMobileOperator());
			setElement.appendChild(mobileOperatorElement);
		}

		if (getUserAgent() != null) {
			Element userAgentElement = xmlDoc.createElementNS(strSettingsNamespace, strSettingsXmlns + ":UserAgent");
			userAgentElement.setTextContent(getUserAgent());
			setElement.appendChild(userAgentElement);
		}

		return xmlDoc.getDocumentElement();
	}

}
