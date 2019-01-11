package cn.study.netty;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TCPClient {

	public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = Integer.parseInt("8080");
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
	
	static class TimeClientHandler extends ChannelInboundHandlerAdapter {
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	                ByteBuf m = (ByteBuf) msg; // (1)
	        try {
	            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
	            Date currentTime = new Date(currentTimeMillis);
	            System.out.println("Default Date Format:" + currentTime.toString());

	            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            String dateString = formatter.format(currentTime);
	            // 转换一下成中国人的时间格式
	            System.out.println("Date Format:" + dateString);

	            ctx.close();
	        } finally {
	            m.release();
	        }
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }
	}
}
