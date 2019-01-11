package cn.study.util;

public class AnyToByte {

	public static byte[] intToBytes(int data) {
		// int=4byte
		return new byte[] { (byte) ((data >> 24) & 0xFF), (byte) ((data >> 16) & 0xFF), (byte) ((data >> 8) & 0xFF),
				(byte) (data & 0xFF) };
	}

	public static byte[] shortToBytes(short data) {
		// short=2byte
		return new byte[] { (byte) ((data >> 8) & 0xFF), (byte) (data & 0xFF) };
	}

	public static byte[] charToBytes(char data) {
		// char=2byte
		return new byte[] { (byte) ((data >> 8) & 0xFF), (byte) (data & 0xFF) };
	}

	public static byte[] stringToBytes(String data) {
		// ascii码
		return data.getBytes();
	}

	/**
	 * 将16进制字符串转为bytes
	 * @param data
	 * @return
	 */
	public static byte[] hexToBytes(String data) {
		byte[] dest = new byte[data.length() / 2];
		for (int i = 0; i < data.length() - 1; i += 2) {
			dest[i / 2] = (byte) (Integer.parseInt(data.substring(i, i + 2), 16));
		}
		return dest;
	}
}
