package cn.study.util;

import java.io.UnsupportedEncodingException;

public class EncodeAndDecode {
	
	/**
	 * UCS2解码
	 * 
	 * @param src
	 *            UCS2 源串
	 * @return 解码后的UTF-16BE字符串
	 * @throws UnsupportedEncodingException 
	 */
	public static String DecodeUCS2(String src) throws UnsupportedEncodingException {
		byte[] bytes = new byte[src.length() / 2];

		for (int i = 0; i < src.length(); i += 2) {
			bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
		}
		String reValue;
		try {
			reValue = new String(bytes, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException("解码异常");
		}
		return reValue;

	}

	/**
	 * UCS2编码
	 * 
	 * @param src
	 *            UTF-16BE编码的源串
	 * @return 编码后的UCS2串
	 * @throws UnsupportedEncodingException 
	 */
	public static String EncodeUCS2(String src) throws UnsupportedEncodingException {

		byte[] bytes;
		try {
			bytes = src.getBytes("UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException("编码异常");
		}
		StringBuffer reValue = new StringBuffer();
		StringBuffer tem = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			tem.delete(0, tem.length());
			tem.append(Integer.toHexString(bytes[i] & 0xFF));
			if (tem.length() == 1) {
				tem.insert(0, '0');
			}
			reValue.append(tem);
		}
		return reValue.toString().toUpperCase();
	}
}
