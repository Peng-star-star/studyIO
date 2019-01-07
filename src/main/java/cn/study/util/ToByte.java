package cn.study.util;

import java.util.Arrays;

public class ToByte {

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
		// short=2byte
		return new byte[] { (byte) ((data >> 8) & 0xFF), (byte) (data & 0xFF) };
	}

	public static byte[] stringToBytes(String data) {
		// ascii码
		return data.getBytes();
	}

	public static void main(String[] args) {
		char a = '1';
		System.err.println(Arrays.toString(charToBytes(a)));
	}
}
