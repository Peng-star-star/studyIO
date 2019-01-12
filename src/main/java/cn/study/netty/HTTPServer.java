package cn.study.netty;

import cn.study.netty.StudyLeakDetector.InboundHandler1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.ResourceLeakDetector;

public class HTTPServer {
	/*
	 * 参考
	 * https://www.w3cschool.cn/essential_netty_in_action/essential_netty_in_action-srlx28c3.html
	 */
	
	/*
	 * 1.HttpServerCodec=HttpRequestDecoder+HttpResponseEncoder;HttpClientCodec=..
	 */
	
	
	public static void main(String[] args) throws Exception{
		HTTPServer server = new HTTPServer();
		server.start(8000);
	}
	
	public void start(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); 
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) 
					.childHandler(new HttpPipelineInitializer(false)).option(ChannelOption.SO_BACKLOG, 128) 
					.childOption(ChannelOption.SO_KEEPALIVE, true); 
 
			ChannelFuture f = b.bind(port).sync(); 
 
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public class HttpPipelineInitializer extends ChannelInitializer<Channel> {

	    private final boolean client;

	    public HttpPipelineInitializer(boolean client) {
	        this.client = client;
	    }

	    @Override
	    protected void initChannel(Channel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();
	        if (client) {
	            pipeline.addLast("decoder", new HttpResponseDecoder());  //1
	            pipeline.addLast("encoder", new HttpRequestEncoder());  //2
	        } else {
	            pipeline.addLast("decoder", new HttpRequestDecoder());  //3
	            pipeline.addLast("encoder", new HttpResponseEncoder());  //4
	            pipeline.addLast("aggegator", new HttpObjectAggregator(512 * 1024));
	            //其他业务处理
	            ch.pipeline().addLast("server",new HttpHandler());
	        }
	    }
	}
	
	class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
		 
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
			try {
			ByteBuf content = msg.content();
			byte[] bts = new byte[content.readableBytes()];
			content.readBytes(bts);
			String result = null;
			if(msg.getMethod() == HttpMethod.GET) {
				String url = msg.getUri().toString();
				result = "get method and paramters is "+ url.substring(url.indexOf("?")+1);
			}else if(msg.getMethod() == HttpMethod.POST) {
				result = "post method and paramters is "+ new String(bts);
			}
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			response.headers().set("content-Type","text/html;charset=UTF-8");
			StringBuilder sb = new StringBuilder();
			sb.append("<html>")
						.append("<head>")
							.append("<title>netty http server</title>")
						.append("</head>")
						.append("<body>")
							.append(result)
						.append("</body>")
					.append("</html>\r\n");
			ByteBuf responseBuf = Unpooled.copiedBuffer(sb,CharsetUtil.UTF_8);
			response.content().writeBytes(responseBuf);
			responseBuf.release();
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
