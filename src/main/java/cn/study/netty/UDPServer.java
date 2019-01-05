package cn.study.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

public class UDPServer {
	
	public static void main(String[] args) {
		int listenPort = 10002;
		UDPServer.initServer(listenPort);
	}
	
	public static void initServer(int listenPort) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
					.handler(new simpleServerHandler());
			Channel channel = bootstrap.bind(listenPort).sync().channel();
			System.out.println("udp服务启动...");
			channel.closeFuture().await();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}
	
	public static class simpleServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
			String content = packet.content().toString(CharsetUtil.UTF_8);
			String ip = packet.sender().getHostString();
			System.out.println(ip + " : " + content);
			
			//返回hello world+原数据
			String rtContent = "hello world ";
			ByteBuf btf = Unpooled.copiedBuffer((rtContent+content).getBytes());
			ctx.writeAndFlush(new DatagramPacket(btf,packet.sender()));
		}
	}
}
