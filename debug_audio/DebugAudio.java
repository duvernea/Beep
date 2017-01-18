import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DebugAudio {

	public static void main(String[] args) {

		// wav file is little endian (least significant bytes first) 
		// 2's complement 16-bit linear PCM format

		// byte is a signed type in Java -128 to 127
		// 0xff = -0x01
		// byte[] audio = new byte[2];
		// byte[0] = 0xa;
		byte[] rawbytes={ (byte) 0x00, (byte) 0x81, (byte) 0xff, (byte) 0xff};
		System.out.println("rawbytes[0] in decimal = " + rawbytes[0]);
		System.out.println("rawbytes[1] in decimal = " + rawbytes[1]);
		System.out.println("rawbytes[2] in decimal = " + rawbytes[2]);
		System.out.println("rawbytes[3] in decimal = " + rawbytes[3]);

		System.out.println("bytes in binary string, little endian");
		System.out.println(getBinaryString(rawbytes[1]) + getBinaryString(rawbytes[0]) + " " + getBinaryString(rawbytes[3]) + getBinaryString(rawbytes[2]));


		short s1 = byteToShortLE(rawbytes[0], rawbytes[1]);
		System.out.println("byte 0 - byte 1 as a short = " + s1);

		short s2 = byteToShortLE(rawbytes[2], rawbytes[3]);
		System.out.println("byte 2 - byte 3 as a short = " + s2);


		byte[] subByteArray = new byte[2];
		subByteArray[0] = rawbytes[0];
		subByteArray[1] = rawbytes[1];
		short s3 = bytesToShort(subByteArray);
		System.out.println("byte 0 - byte 1 as a short using bytebuffer = " + s3);


	}
	private static short byteToShortLE(byte b1, byte b2) {
        return (short) (b1 & 0xFF | ((b2 & 0xFF) << 8));
    }

    private static String getBinaryString(byte b) {
    	String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		return s1;
	}
	public static short bytesToShort(byte[] bytes) {
    	return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
}