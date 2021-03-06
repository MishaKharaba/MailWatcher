package ExchangeActiveSync;

import java.io.ByteArrayOutputStream;

// This class extends the .NET Queue<byte> class
// to add some WBXML-specific functionality.
public class ASWBXMLByteQueue {
    private byte[] bytes;
    private int index;

    public ASWBXMLByteQueue(byte[] bytes) {
        this.bytes = bytes;
    }

    // This function will pop a multi-byte integer
    // (as specified in WBXML) from the WBXML stream.
    public int dequeueMultibyteInt() throws Exception {
        int returnValue = 0;
        byte singleByte;
        do {
            returnValue <<= 7;
            singleByte = this.Dequeue();
            returnValue |= singleByte & 0x7F;
        } while (checkContinuationBit(singleByte));
        return returnValue;
    }

    public byte Dequeue() {
        return bytes[index++];
    }

    // This function checks a byte to see if the continuation
    // bit is set. This is used in deciphering multi-byte integers
    // in a WBXML stream.
    private boolean checkContinuationBit(byte byteValue) throws Exception {
        byte continuationBitmask = (byte) 0x80;
        return (continuationBitmask & byteValue) != 0;
    }

    // This function pops a string from the WBXML stream.
    // It will read from the stream until a null byte is found.
    public String dequeueString() throws Exception {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte currentByte = 0x00;
        do {
            // TODO: Improve this handling. We are technically UTF-8, meaning
            // that characters could be more than one byte long. This will fail
            // if we have
            // characters outside of the US-ASCII range
            currentByte = this.Dequeue();
            if (currentByte != 0x00) {
                byteArray.write(currentByte);
            }

        } while (currentByte != 0x00);
        String str = new String(byteArray.toByteArray(), "UTF-8");
        return str;
    }

    // This function pops a string of the specified length from
    // the WBXML stream.
    public String dequeueString(int length) throws Exception {
        byte[] buffer = dequeueBinary(length);
        String str = new String(buffer, "UTF-8");
        return str;
    }

    // This function dequeues a byte array of the specified length
    // from the WBXML stream.
    public byte[] dequeueBinary(int length) throws Exception {
        byte[] returnBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            returnBytes[i] = Dequeue();
        }
        return returnBytes;
    }

    public int getCount() {
        return bytes != null ? bytes.length - index : 0;
    }

}
