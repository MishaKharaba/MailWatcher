package ExchangeActiveSync;

public class Utilities {
    // This function generates a string representation
    // of hexadecimal bytes.
    public static String printHex(byte[] bytes) throws Exception {
        StringBuilder returnString = new StringBuilder();
        for (byte b : bytes) {
            returnString.append(String.format("%02X ", b));
        }
        return returnString.toString();
    }

    // This function converts a string representation
    // of hexadecimal bytes into a byte array
    public static byte[] convertHexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

}
