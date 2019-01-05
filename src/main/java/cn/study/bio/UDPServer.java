package cn.study.bio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {
	
	public static int listenPort = 10005;
	public static int threadNum = 10;
	
	public static void main(String[] args){
		try {
			final DatagramSocket ds = new DatagramSocket(listenPort);
			for(int i=0;i<threadNum;i++){
				new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								byte[] buf = new byte[1024];
								DatagramPacket dp = new DatagramPacket(buf, buf.length);
								//bio程序在receive阻塞
								ds.receive(dp);
								String ip = dp.getAddress().getHostAddress();
								String data = new String(dp.getData(),dp.getOffset(),dp.getLength());
								//处理报文
								//处理时间尽可能短，手机卡没有固定IP,IP地址更换时间与信号有关，具有不确定因素，所以要尽快返回结果
								//如果报文处理时间过长，建议分步骤考虑，先处理返回结果相关数据，其他可以先缓存后处理
								System.out.println(ip + " : " + data);
								//返回结果
								ds.send(dp);
							} catch (Exception e) {
								//记录此条报文处理的错误日志
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
