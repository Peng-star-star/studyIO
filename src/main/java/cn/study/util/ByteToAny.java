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

	/**
	 * 将bytes转为非负数int
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static int bytesToPositiveInt(byte[] data) throws Exception {
		if (data == null || data.length > 3) {
			throw new Exception("data不能为null并且不能超过3位");
		}
		byte[] destBytes = new byte[4];
		System.arraycopy(data, 0, destBytes, data.length, destBytes.length - data.length);
		return bytesToInt(destBytes);
	}
	
	/**
	 * 将bytes转为16进制字符串
	 * @param data 数据源
	 * @param index 起点
	 * @param len 长度
	 * @return
	 */
	public static String bytesToHex(byte[] data, int index, int len) {
		StringBuffer retValue = new StringBuffer();
		for (int i = 0; i < data.length && len > 0; i++, len--) {
			retValue.append(String.format("%02X", data[i] & 0xFF));
		}
		return retValue.toString();
	}
}
