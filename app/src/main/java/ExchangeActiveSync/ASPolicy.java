package ExchangeActiveSync;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Iterator;

// This class represents an Exchange
// ActiveSync policy.
public class ASPolicy {
    public enum EncryptionAlgorithm {
        TripleDES, DES, RC2_128bit, RC2_64bit, RC2_40bit
    }

    public enum SigningAlgorithm {
        SHA1, MD5
    }

    public enum CalendarAgeFilter {
        ALL, __dummyEnum__0, __dummyEnum__1, __dummyEnum__2, TWO_WEEKS, ONE_MONTH, THREE_MONTHS, SIX_MONTHS
    }

    public enum MailAgeFilter {
        ALL, ONE_DAY, THREE_DAYS, ONE_WEEK, TWO_WEEKS, ONE_MONTH
    }

    public enum PolicyStatus {
        None, Success, NoPolicyDefined, PolicyTypeUnknown, PolicyDataCorrupt, PolicyKeyMismatch
    }

    private int status = 0;
    private long policyKey = 0;
    private byte allowBlueTooth = 0;
    private boolean allowBrowser = false;
    private boolean allowCamera = false;
    private boolean allowConsumerEmail = false;
    private boolean allowDesktopSync = false;
    private boolean allowHTMLEmail = false;
    private boolean allowInternetSharing = false;
    private boolean allowIrDA = false;
    private boolean allowPOPIMAPEmail = false;
    private boolean allowRemoteDesktop = false;
    private boolean allowSimpleDevicePassword = false;
    private int allowSMIMEEncryptionAlgorithmNegotiation = 0;
    private boolean allowSMIMESoftCerts = false;
    private boolean allowStorageCard = false;
    private boolean allowTextMessaging = false;
    private boolean allowUnsignedApplications = false;
    private boolean allowUnsignedInstallationPackages = false;
    private boolean allowWifi = false;
    private boolean alphanumericDevicePasswordRequired = false;
    private boolean attachmentsEnabled = false;
    private boolean devicePasswordEnabled = false;
    private long devicePasswordExpiration = 0;
    private long devicePasswordHistory = 0;
    private long maxAttachmentSize = 0;
    private long maxCalendarAgeFilter = 0;
    private long maxDevicePasswordFailedAttempts = 0;
    private long maxEmailAgeFilter = 0;
    private int maxEmailBodyTruncationSize = -1;
    private int maxEmailHTMLBodyTruncationSize = -1;
    private long maxInactivityTimeDeviceLock = 0;
    private byte minDevicePasswordComplexCharacters = 1;
    private byte minDevicePasswordLength = 1;
    private boolean passwordRecoveryEnabled = false;
    private boolean requireDeviceEncryption = false;
    private boolean requireEncryptedSMIMEMessages = false;
    private int requireEncryptionSMIMEAlgorithm = 0;
    private boolean requireManualSyncWhenRoaming = false;
    private int requireSignedSMIMEAlgorithm = 0;
    private boolean requireSignedSMIMEMessages = false;
    private boolean requireStorageCardEncryption = false;
    private String[] approvedApplicationList = null;
    private String[] unapprovedInROMApplicationList = null;
    private boolean remoteWipeRequested = false;
    private boolean hasPolicyInfo = false;

    public int getStatus() throws Exception {
        return status;
    }

    public long getPolicyKey() throws Exception {
        return policyKey;
    }

    public byte getAllowBlueTooth() throws Exception {
        return allowBlueTooth;
    }

    public boolean getAllowBrowser() throws Exception {
        return allowBrowser;
    }

    public boolean getAllowCamera() throws Exception {
        return allowCamera;
    }

    public boolean getAllowConsumerEmail() throws Exception {
        return allowConsumerEmail;
    }

    public boolean getAllowDesktopSync() throws Exception {
        return allowDesktopSync;
    }

    public boolean getAllowHTMLEmail() throws Exception {
        return allowHTMLEmail;
    }

    public boolean getAllowInternetSharing() throws Exception {
        return allowInternetSharing;
    }

    public boolean getAllowIrDA() throws Exception {
        return allowIrDA;
    }

    public boolean getAllowPOPIMAPEmail() throws Exception {
        return allowPOPIMAPEmail;
    }

    public boolean getAllowRemoteDesktop() throws Exception {
        return allowRemoteDesktop;
    }

    public boolean getAllowSimpleDevicePassword() throws Exception {
        return allowSimpleDevicePassword;
    }

    public int getAllowSMIMEEncryptionAlgorithmNegotiation() throws Exception {
        return allowSMIMEEncryptionAlgorithmNegotiation;
    }

    public boolean getAllowSMIMESoftCerts() throws Exception {
        return allowSMIMESoftCerts;
    }

    public boolean getAllowStorageCard() throws Exception {
        return allowStorageCard;
    }

    public boolean getAllowTextMessaging() throws Exception {
        return allowTextMessaging;
    }

    public boolean getAllowUnsignedApplications() throws Exception {
        return allowUnsignedApplications;
    }

    public boolean getAllowUnsignedInstallationPackages() throws Exception {
        return allowUnsignedInstallationPackages;
    }

    public boolean getAllowWifi() throws Exception {
        return allowWifi;
    }

    public boolean getAlphanumericDevicePasswordRequired() throws Exception {
        return alphanumericDevicePasswordRequired;
    }

    public boolean getAttachmentsEnabled() throws Exception {
        return attachmentsEnabled;
    }

    public boolean getDevicePasswordEnabled() throws Exception {
        return devicePasswordEnabled;
    }

    public long getDevicePasswordExpiration() throws Exception {
        return devicePasswordExpiration;
    }

    public long getDevicePasswordHistory() throws Exception {
        return devicePasswordHistory;
    }

    public long getMaxAttachmentSize() throws Exception {
        return maxAttachmentSize;
    }

    public long getMaxCalendarAgeFilter() throws Exception {
        return maxCalendarAgeFilter;
    }

    public long getMaxDevicePasswordFailedAttempts() throws Exception {
        return maxDevicePasswordFailedAttempts;
    }

    public long getMaxEmailAgeFilter() throws Exception {
        return maxEmailAgeFilter;
    }

    public int getMaxEmailBodyTruncationSize() throws Exception {
        return maxEmailBodyTruncationSize;
    }

    public int getMaxEmailHTMLBodyTruncationSize() throws Exception {
        return maxEmailHTMLBodyTruncationSize;
    }

    public long getMaxInactivityTimeDeviceLock() throws Exception {
        return maxInactivityTimeDeviceLock;
    }

    public byte getMinDevicePasswordComplexCharacters() throws Exception {
        return minDevicePasswordComplexCharacters;
    }

    public byte getMinDevicePasswordLength() throws Exception {
        return minDevicePasswordLength;
    }

    public boolean getPasswordRecoveryEnabled() throws Exception {
        return passwordRecoveryEnabled;
    }

    public boolean getRequireDeviceEncryption() throws Exception {
        return requireDeviceEncryption;
    }

    public boolean getRequireEncryptedSMIMEMessages() throws Exception {
        return requireEncryptedSMIMEMessages;
    }

    public int getRequireEncryptionSMIMEAlgorithm() throws Exception {
        return requireEncryptionSMIMEAlgorithm;
    }

    public boolean getRequireManualSyncWhenRoaming() throws Exception {
        return requireManualSyncWhenRoaming;
    }

    public int getRequireSignedSMIMEAlgorithm() throws Exception {
        return requireSignedSMIMEAlgorithm;
    }

    public boolean getRequireSignedSMIMEMessages() throws Exception {
        return requireSignedSMIMEMessages;
    }

    public boolean getRequireStorageCardEncryption() throws Exception {
        return requireStorageCardEncryption;
    }

    public String[] getApprovedApplicationList() throws Exception {
        return approvedApplicationList;
    }

    public String[] getUnapprovedInROMApplicationList() throws Exception {
        return unapprovedInROMApplicationList;
    }

    public boolean getRemoteWipeRequested() throws Exception {
        return remoteWipeRequested;
    }

    public boolean getHasPolicyInfo() throws Exception {
        return hasPolicyInfo;
    }

    // This function parses a Provision command
    // response (as specified in MS-ASPROV section 2.2)
    // and extracts the policy information.
    public boolean loadXML(String policyXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(policyXml));
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

        // If this is a remote wipe, there's no
        // further parsing to do.
        Node remoteWipeNode = (Node) xPath.evaluate(".//provision:RemoteWipe", xmlDoc, XPathConstants.NODE);
        if (remoteWipeNode != null) {
            remoteWipeRequested = true;
            return true;
        }

        // Find the policy.
        Node policyNode = (Node) xPath.evaluate(".//provision:Policy", xmlDoc, XPathConstants.NODE);
        if (policyNode == null) {
            return false;
        }

        Node policyTypeNode = (Node) xPath.evaluate("provision:PolicyType", policyNode, XPathConstants.NODE);
        if (policyTypeNode != null && "MS-EAS-Provisioning-WBXML".equals(policyTypeNode.getTextContent())) {
            // Get the policy's status
            Node policyStatusNode = (Node) xPath.evaluate("provision:Status", policyNode, XPathConstants.NODE);
            if (policyStatusNode != null)
                status = Integer.parseInt(policyStatusNode.getTextContent());

            // Get the policy key
            Node policyKeyNode = (Node) xPath.evaluate("provision:PolicyKey", policyNode, XPathConstants.NODE);
            if (policyKeyNode != null)
                policyKey = Long.parseLong(policyKeyNode.getTextContent());

            // Get the contents of the policy
            Node provisioningDocNode = (Node) xPath.evaluate(".//provision:EASProvisionDoc", policyNode,
                    XPathConstants.NODE);
            if (provisioningDocNode != null) {
                hasPolicyInfo = true;
                NodeList childNodes = provisioningDocNode.getChildNodes();
                for (int i = 0, n = childNodes.getLength(); i < n; i++) {
                    Node policySettingNode = childNodes.item(i);
                    // Loop through the child nodes and
                    // set the corresponding property.
                    String name = policySettingNode.getLocalName();
                    if (name.equals(("AllowBluetooth"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowBlueTooth = Byte.parseByte(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowBrowser"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowBrowser = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowCamera"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowCamera = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowConsumerEmail"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowConsumerEmail = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowDesktopSync"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowDesktopSync = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowHTMLEmail"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowHTMLEmail = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowInternetSharing"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowInternetSharing = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowIrDA"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowIrDA = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowPOPIMAPEmail"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowPOPIMAPEmail = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowRemoteDesktop"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowRemoteDesktop = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowSimpleDevicePassword"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowSimpleDevicePassword = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowSMIMEEncryptionAlgorithmNegotiation"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowSMIMEEncryptionAlgorithmNegotiation = Integer
                                    .parseInt(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowSMIMESoftCerts"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowSMIMESoftCerts = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowStorageCard"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowStorageCard = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowTextMessaging"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowTextMessaging = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowUnsignedApplications"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowUnsignedApplications = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowUnsignedInstallationPackages"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowUnsignedInstallationPackages = Boolean
                                    .parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AllowWiFi"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            allowWifi = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("AlphanumericDevicePasswordRequired"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            alphanumericDevicePasswordRequired = Boolean
                                    .parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("ApprovedApplicationList"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            approvedApplicationList = parseAppList(policySettingNode);

                    } else if (name.equals(("AttachmentsEnabled"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            attachmentsEnabled = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("DevicePasswordEnabled"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            devicePasswordEnabled = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("DevicePasswordExpiration"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            devicePasswordExpiration = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("DevicePasswordHistory"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            devicePasswordHistory = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxAttachmentSize"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxAttachmentSize = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxCalendarAgeFilter"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxCalendarAgeFilter = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxDevicePasswordFailedAttempts"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxDevicePasswordFailedAttempts = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxEmailAgeFilter"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxEmailAgeFilter = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxEmailBodyTruncationSize"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxEmailBodyTruncationSize = Integer.parseInt(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxEmailHTMLBodyTruncationSize"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxEmailHTMLBodyTruncationSize = Integer.parseInt(policySettingNode.getTextContent());

                    } else if (name.equals(("MaxInactivityTimeDeviceLock"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            maxInactivityTimeDeviceLock = Long.parseLong(policySettingNode.getTextContent());

                    } else if (name.equals(("MinDevicePasswordComplexCharacters"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            minDevicePasswordComplexCharacters = Byte.parseByte(policySettingNode.getTextContent());

                    } else if (name.equals(("MinDevicePasswordLength"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            minDevicePasswordLength = Byte.parseByte(policySettingNode.getTextContent());

                    } else if (name.equals(("PasswordRecoveryEnabled"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            passwordRecoveryEnabled = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireDeviceEncryption"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireDeviceEncryption = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireEncryptedSMIMEMessages"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireEncryptedSMIMEMessages = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireEncryptionSMIMEAlgorithm"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireEncryptionSMIMEAlgorithm = Integer.parseInt(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireManualSyncWhenRoaming"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireManualSyncWhenRoaming = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireSignedSMIMEAlgorithm"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireSignedSMIMEAlgorithm = Integer.parseInt(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireSignedSMIMEMessages"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireSignedSMIMEMessages = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("RequireStorageCardEncryption"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            requireStorageCardEncryption = Boolean.parseBoolean(policySettingNode.getTextContent());

                    } else if (name.equals(("UnapprovedInROMApplicationList"))) {
                        if (!"".equals(policySettingNode.getTextContent()))
                            unapprovedInROMApplicationList = parseAppList(policySettingNode);

                    } else {
                    }
                }
            }
        }

        return true;
    }

    // This function parses the contents of the
    // ApprovedApplicationList and the UnapprovedInROMApplicationList
    // nodes.
    private String[] parseAppList(Node appListNode) throws Exception {
        NodeList childNodes = appListNode.getChildNodes();
        String[] appList = new String[childNodes.getLength()];
        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node appNode = (Node) childNodes.item(i);
            appList[i] = appNode.getNodeValue();
        }
        return appList;
    }

}
