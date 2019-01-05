package cn.study.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {

	public static int listenPort = 10006;
	public static int threadNum = 10;

	public static void main(String[] args) throws IOException {
		// 创建通道和选择器
		Selector selector = Selector.open();
		DatagramChannel channel = DatagramChannel.open();
		// 设置为非阻塞模式
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(listenPort));
		// 将通道注册至selector，监听只读消息（此时服务端只能读数据，无法写数据）
		channel.register(selector, SelectionKey.OP_READ);
		while (true) {
			if (selector.select() > 0) {
				continue;
			}
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				try {
					iterator.remove();
					if (key.isReadable()) {
						// 接收数据
						doReceive(key);
					}
				} catch (Exception e) {
					System.err.println("SelectionKey receive exception" + e.getMessage());
					try {
						if (key != null) {
							key.cancel();
							key.channel().close();
						}
					} catch (IOException cex) {
						System.err.println("Close channel exception" + cex.getMessage());
					}
				}
			}
		}
	}

	public static void doReceive(final SelectionKey key) {
		//注意由于大量线程同时运行，很有会造成系统瘫痪
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		cachedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					DatagramChannel datagramChannel = (DatagramChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					buffer.clear();
					InetSocketAddress sa = (InetSocketAddress) datagramChannel.receive(buffer);
					buffer.flip();// position指到头部
					byte[] buf = new byte[buffer.limit()];
					buffer.get(buf);
					String content = new String(buf);
					// 处理报文
					System.out.println(sa.getHostString() + " : " + content);
					// 原报文返回
					buffer.flip();
					datagramChannel.send(buffer, sa);
					buffer.clear();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
