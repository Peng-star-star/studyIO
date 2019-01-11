package cn.study.netty;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;

public class StudyChannelHandlerContext {
	
	/*
	 * 参考下面网站的例子，做测试
	 * https://www.w3cschool.cn/essential_netty_in_action/
	 */
	
	//每一个连接创建一个Channel一个ChannelPipeline
	//多个ChannelHandler和与之对应的ChannelHandlerContext 
	
	
	//通过 Channel写缓存
	//通过 ChannelPipeline写缓冲区
	//通过 ChannelHandlerContext写缓存
	//添加一个 ChannelHandler 到 pipeline 来支持动态协议改变
	/*在ChannelHandler内部进行修改pipeline*/
	//统计某个ChannelHandler的调用次数
	/*设计成单例模式的同时注意在类上加入@ChannelHandler.Sharable注解。否则，试图将它添加到多个ChannelPipeline时将会触发异常*/
	
	
	
	/*
	 * 1.ChannelInboundHandler之间传递使用ctx.fireXXX;ChannelOutboundHandler之间传递ctx.write
	 * 2.writeAndFlush相当于write();flush()，未仔细看源码，需要再确定
	 * 3.ChannelOutboundHandler 在注册的时候需要放在最后一个ChannelInboundHandler之前，否则将无法传递到ChannelOutboundHandler
	 * 4.channelReadComplete在本例中是ChannelHandler全部处理完成后执行，那它到底是在什么时候触发的？
	 * 5.下例的执行顺序InboundHandler1.channelRead-》InboundHandler2.channelRead——》
	 * OutboundHandler2.write-》OutboundHandler1.write-》InboundHandler1.channelReadComplete-》
	 * 以下是异步不知道执行顺序
	 * OOutboundHandler1.writeAndFlush after doing——》OutboundHandler2.writeAndFlush after doing
	 * InboundHandler2.write after doing-》InboundHandler1.fireChannelRead after doing
	 */
	
	public static void main(String[] args) throws Exception {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
		StudyChannelHandlerContext server = new StudyChannelHandlerContext();
		server.start(8000);
	}
	
	public void start(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); 
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) 
					.childHandler(new ChannelInitializer<SocketChannel>() { 
								@Override
								public void initChannel(SocketChannel ch) throws Exception {
									// 注册两个OutboundHandler，执行顺序为注册顺序的逆序，所以应该是OutboundHandler2 OutboundHandler1
									ch.pipeline().addLast(new OutboundHandler1());
									ch.pipeline().addLast(new OutboundHandler2());
									// 注册两个InboundHandler，执行顺序为注册顺序，所以应该是InboundHandler1 InboundHandler2
									ch.pipeline().addLast(new InboundHandler1());
									ch.pipeline().addLast(new InboundHandler2());
								}
							}).option(ChannelOption.SO_BACKLOG, 128) 
					.childOption(ChannelOption.SO_KEEPALIVE, true); 
 
			ChannelFuture f = b.bind(port).sync(); 
 
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public class InboundHandler1 extends ChannelInboundHandlerAdapter {
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("InboundHandler1.channelRead: ctx :" + ctx);
			ByteBuf result = Unpooled.copiedBuffer(" InboundHandler1",Charset.forName("UTF-8"));
			//合并
			ByteBuf all = Unpooled.wrappedBuffer((ByteBuf)msg,result);
			// 通知执行下一个InboundHandler
			ctx.fireChannelRead(all);
			System.err.println("InboundHandler1.fireChannelRead after doing");
		}
	 
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			System.out.println("InboundHandler1.channelReadComplete");
			ctx.fireChannelReadComplete();
			//ctx.flush();
		}
	}
	
	public class InboundHandler2 extends ChannelInboundHandlerAdapter {
		@Override
		// 读取Client发送的信息，并打印出来
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("InboundHandler2.channelRead: ctx :" + ctx);
			ByteBuf result = (ByteBuf) msg;
			
			String resultStr = result.toString(Charset.forName("UTF-8"));
			System.out.println("Client said:" + resultStr);
			//result.release();
	 
			ctx.write(msg);
			System.err.println("InboundHandler2.write after doing");
		}
	 
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			System.out.println("InboundHandler2.channelReadComplete");
			ctx.flush();
		}
	 
	}
	
	public class OutboundHandler1 extends ChannelOutboundHandlerAdapter {
		@Override
		// 向client发送消息
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			System.out.println("OutboundHandler1.write");
			String response = "I am ok!";
			ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
			encoded.writeBytes(response.getBytes());
			ctx.write(encoded);
			ctx.flush();
			System.err.println("OutboundHandler1.writeAndFlush after doing");
		}
	}
	

	public class OutboundHandler2 extends ChannelOutboundHandlerAdapter {
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			System.out.println("OutboundHandler2.write");
			// 执行下一个OutboundHandler
			ctx.writeAndFlush(msg);
			System.err.println("OutboundHandler2.writeAndFlush after doing");
		}
	}
}
