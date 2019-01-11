package cn.study.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;

public class StudyLeakDetector {
	/*
	 * 参考，实现监测内存泄漏问题
	 * http://www.importnew.com/22205.html 
	 */
	
	/*
	 * 1.使用池化的ByteBuf，在内存回收时才会检测到内存没有回收
	 * 2.传递到最后一个地方时释放，默认会有一个TailHandler，HeadHandler，所以调用write会自己释放
	 * 3.异常提示只会出现一次，之后不在出现
	 */
	
	
	public static void main(String[] args) throws Exception{
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
		StudyLeakDetector server = new StudyLeakDetector();
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
									ch.pipeline().addLast(new InboundHandler1());
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
			while(true){
				Thread.currentThread().sleep(1000);
				ctx.alloc().buffer(1024);
				ctx.alloc().buffer(1024);
				ctx.alloc().buffer(1024);
				ctx.alloc().buffer(1024);
				ctx.alloc().buffer(1024);
				ReferenceCountUtil.release(ctx.alloc().buffer(1024));
				System.err.println("6");
				System.gc();
			}
		}
	}
}

