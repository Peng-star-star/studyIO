package cn.study.bio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPClient {
	
	/*客户端一般是单线程，此设置为多个线程是用于测试服务端处理并发下是否丢报文*/
	
	public static String serverIp = "127.0.0.1";
	public static int serverPort = 10002;
	
	public static void main(String[] args) {
		try {
			final DatagramSocket ds = new DatagramSocket();
			final AtomicInteger sendNum = new AtomicInteger(10000);
			final String content = "0123456789012345678901234567";
			Thread[] ts = new Thread[100];
			for (int i = 0; i < ts.length; i++) {
				ts[i] = new Thread() {
					public void run() {
						while (sendNum.getAndDecrement()>0) {
							try {
								byte[] buf = content.getBytes();
								DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverIp), serverPort);
								ds.send(dp);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
			}
			for (Thread t : ts) {
				t.start();
			}
			for (Thread t : ts) {
				t.join();
			}
			ds.close();
			System.out.println("已发送完数据 "+sendNum.get());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
