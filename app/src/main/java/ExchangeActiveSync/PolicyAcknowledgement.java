package ExchangeActiveSync;

public class PolicyAcknowledgement {
    // This enumeration covers the acceptable values
    // of the Status element in a Provision request
    // when acknowledging a policy, as specified
    // in MS-ASPROV section 3.1.5.1.2.1.
    public final static int Success = 1;
    public final static int PartialSuccess = 2;
    public final static int PolicyIgnored = 3;
    public final static int ExternalManagement = 4;

}
