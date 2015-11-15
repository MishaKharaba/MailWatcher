package ExchangeActiveSync;

import ExchangeActiveSync.ASPolicy.PolicyStatus;

import java.util.ArrayList;
import java.util.List;

public class EasConnection {
    private String user;
    private String password;
    private String server;
    private boolean ignoreCertificate;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void setCredential(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public boolean isIgnoreCertificate() {
        return ignoreCertificate;
    }

    public void setIgnoreCertificate(boolean ignoreCertificate) {
        this.ignoreCertificate = ignoreCertificate;
    }

    private Device createDevice() {
        Device device = new Device();
        device.setDeviceID("Phone");
        device.setDeviceType("Mobile");
        device.setModel("Samsung S5");
        return device;
    }

    public long getPolicyKey() throws Exception {
        Device device = createDevice();

        ASProvisionRequest provReq = new ASProvisionRequest();
        provReq.setServer(getServer());
        provReq.setCredentials(getUser(), getPassword());
        provReq.setIgnoreCert(isIgnoreCertificate());
        provReq.setUser(getUser());
        provReq.setProvisionDevice(device);
        provReq.setDeviceID(device.getDeviceID());
        provReq.setDeviceType(device.getDeviceType());
        long policyKey = 0;
        provReq.setPolicyKey(policyKey);

        ASProvisionResponse provRes = (ASProvisionResponse) provReq.getResponse();
        if (provRes.getStatus() != ProvisionStatus.Success) {
            throw new Exception(
                    String.format("Error returned from initial provision request: status=%d", provRes.getStatus()));
        }

        if (provRes.getIsPolicyLoaded()) {
            if (provRes.getPolicy().getStatus() != PolicyStatus.Success.ordinal()) {
                throw new Exception(String.format("Policy Error returned from initial provision request: status=%d",
                        provRes.getPolicy().getStatus()));
            }

            if (provRes.getPolicy().getRemoteWipeRequested()) {
                ASProvisionRequest wipeAcknReq = new ASProvisionRequest();

                // Initialize the request with information
                // that applies to all requests.
                wipeAcknReq.setCredentials(getUser(), getPassword());
                wipeAcknReq.setIgnoreCert(isIgnoreCertificate());
                wipeAcknReq.setServer(getServer());
                wipeAcknReq.setUser(getUser());
                wipeAcknReq.setDeviceID(device.getDeviceID());
                wipeAcknReq.setDeviceType(device.getDeviceType());
                wipeAcknReq.setPolicyKey(policyKey);

                // Initialize the Provision command-specific
                // information.
                wipeAcknReq.setIsRemoteWipe(true);
                // Indicate successful wipe
                wipeAcknReq.setStatus(ProvisionStatus.Success);

                // Send the acknowledgment
                ASProvisionResponse wipeAckRes = (ASProvisionResponse) wipeAcknReq.getResponse();

                if (wipeAckRes.getStatus() != ProvisionStatus.Success) {
                    throw new Exception(
                            String.format("Error returned from remote wipe Acknowledgment request: status=%d",
                                    wipeAckRes.getStatus()));
                }
            } else {
                // The server has provided a policy
                // and a temporary policy key.
                // The client must acknowledge this policy
                // in order to get a permanent policy
                // key.

                ASProvisionRequest policyAckReq = new ASProvisionRequest();

                // Initialize the request with information
                // that applies to all requests.
                policyAckReq.setCredentials(getUser(), getPassword());
                policyAckReq.setIgnoreCert(isIgnoreCertificate());
                policyAckReq.setServer(getServer());
                policyAckReq.setUser(getUser());
                policyAckReq.setDeviceID(device.getDeviceID());
                policyAckReq.setDeviceType(device.getDeviceType());
                // Set the policy key to the temporary policy key from
                // the previous response.
                policyAckReq.setPolicyKey(provRes.getPolicy().getPolicyKey());

                // Initialize the Provision command-specific
                // information.
                policyAckReq.setIsAcknowledgement(true);
                // Indicate successful application of the policy.
                policyAckReq.setStatus(PolicyAcknowledgement.Success);

                // Send the request
                ASProvisionResponse policyAckRes = (ASProvisionResponse) policyAckReq.getResponse();

                if (policyAckRes.getStatus() == ProvisionStatus.Success && policyAckRes.getIsPolicyLoaded()) {
                    // Save the permanent policy key for use
                    // in subsequent command requests.
                    policyKey = policyAckRes.getPolicy().getPolicyKey();
                } else {
                    throw new Exception(String.format("Error returned from policy acknowledgment request: %s",
                            policyAckRes.getStatus()));
                }

            }
        }
        return policyKey;
    }

    public List<EasFolder> getFolders(long policyKey) throws Exception {
        Device device = createDevice();
        String syncKey = "0";
        ASFolderSyncRequest syncFolderReq = new ASFolderSyncRequest();
        syncFolderReq.setCredentials(getUser(), getPassword());
        syncFolderReq.setIgnoreCert(isIgnoreCertificate());
        syncFolderReq.setServer(getServer());
        syncFolderReq.setUser(getUser());
        syncFolderReq.setDeviceID(device.getDeviceID());
        syncFolderReq.setDeviceType(device.getDeviceType());
        syncFolderReq.setPolicyKey(policyKey);
        syncFolderReq.setSyncKey(syncKey);

        ASFolderSyncResponse syncFolderRes = (ASFolderSyncResponse) syncFolderReq.getResponse();

        List<EasFolder> folders = new ArrayList<>();
        List<FolderInfo> folderList = syncFolderRes.getFolderList();
        for (FolderInfo fi : folderList) {
            EasFolder folder = new EasFolder();
            folder.setId(fi.id);
            folder.setParentId(fi.parentId);
            folder.setName(fi.name);
            folder.setType(EasFolderType.valueOf(fi.type));
            folders.add(folder);
        }
        return folders;
    }

    public String getFolderSyncKey(long policyKey, EasFolder folder) throws Exception {
        Device device = createDevice();
        ASSyncRequest initSyncReq = new ASSyncRequest();
        initSyncReq.setCredentials(getUser(), getPassword());
        initSyncReq.setIgnoreCert(isIgnoreCertificate());
        initSyncReq.setServer(getServer());
        initSyncReq.setUser(getUser());
        initSyncReq.setDeviceID(device.getDeviceID());
        initSyncReq.setDeviceType(device.getDeviceType());
        initSyncReq.setPolicyKey(policyKey);
        FolderInfo fi = new FolderInfo();
        fi.id = folder.getId();
        fi.parentId = folder.getParentId();
        fi.name = folder.getName();
        fi.type = folder.getType().ordinal();
        initSyncReq.getFolders().add(fi);
        // inbox.areChangesIgnored = true;

        ASSyncResponse initSyncRes = (ASSyncResponse) initSyncReq.getResponse();
        if (initSyncRes.getStatus() != ASSyncResponse.SyncStatus.Success.ordinal()) {
            throw new Exception("Error returned from empty sync reqeust: " + initSyncRes.getStatus());
        }
        fi.syncKey = initSyncRes.getSyncKeyForFolder(fi.id);

        return fi.syncKey;
    }

    public List<EasSyncCommand> getFolderSyncCommands(long policyKey, EasFolder folder) throws Exception {
        Device device = createDevice();
        List<EasSyncCommand> syncCommands = new ArrayList<>();
        ASSyncRequest syncReq = new ASSyncRequest();
        syncReq.setCredentials(getUser(), getPassword());
        syncReq.setIgnoreCert(isIgnoreCertificate());
        syncReq.setServer(getServer());
        syncReq.setUser(getUser());
        syncReq.setDeviceID(device.getDeviceID());
        syncReq.setDeviceType(device.getDeviceType());
        syncReq.setPolicyKey(policyKey);
        FolderInfo fi = new FolderInfo();
        fi.id = folder.getId();
        fi.parentId = folder.getParentId();
        fi.name = folder.getName();
        fi.type = folder.getType().ordinal();
        fi.syncKey = folder.getSyncKey();
        syncReq.getFolders().add(fi);
        syncReq.setWindowSize(512);
        // inbox.useConversationMode = true;

        ASSyncResponse syncRes = (ASSyncResponse) syncReq.getResponse();
        List<ServerSyncCommand> srvSyncs = syncRes.getServerSyncsForFolder(folder.getId());
        if (syncRes.getStatus() == ASSyncResponse.SyncStatus.Success.ordinal()) {
            String syncKey = syncRes.getSyncKeyForFolder(folder.getId());
            folder.setSyncKey(syncKey);
        }

        for (ServerSyncCommand srvSync : srvSyncs) {
            EasSyncCommand syncCommand = new EasSyncCommand();
            syncCommand.setId(srvSync.getServerId());
            syncCommand.setType(srvSync.getType());
            syncCommand.setMessage(srvSync.getMessage());
            syncCommands.add(syncCommand);
        }

        return syncCommands;
    }
}
