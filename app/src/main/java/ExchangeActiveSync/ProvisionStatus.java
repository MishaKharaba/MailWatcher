package ExchangeActiveSync;

public class ProvisionStatus {
    // This enumeration covers the Provision-
    // specific status values that can come from
    // the server.
    public final static int Success = 1;
    public final static int SyntaxError = 2;
    public final static int ServerError = 3;
    public final static int DeviceNotFullyProvisionable = 139;
    public final static int LegacyDeviceOnStrictPolicy = 141;
    public final static int ExternallyManagedDevicesNotAllowed = 145;
    public final static int DeviceInformationRequired = 165;
}
