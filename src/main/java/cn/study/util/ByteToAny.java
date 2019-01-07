package cn.study.util;

public class ByteToAny {

	public static int bytesToInt(byte[] data) {
		return data[3] & 0xFF | (data[2] & 0xFF) << 8 | (data[1] & 0xFF) << 16 | (data[0] & 0xFF) << 24;
	}

	public static short bytesToShort(byte[] data) {
		return (short) (data[1] & 0xFF | (data[0] & 0xFF) << 8);
	}

	public static char bytesToChar(byte[] data) {
		return (char) (data[1] & 0xFF | (data[0] & 0xFF) << 8);
	}

	public static String bytesToString(byte[] data) {
		return new String(data);
	}

	/* 以上为byte转基本类型，以下为特殊的情况 */

	public static int bytesToPositiveInt(byte[] data) throws Exception {
		if (data == null || data.length > 3) {
			throw new Exception("data不能为null并且不能超过3位");
		}
		byte[] destBytes = new byte[4];
		System.arraycopy(data, 0, destBytes, data.length, destBytes.length - data.length);
		return bytesToInt(destBytes);
	}
}
