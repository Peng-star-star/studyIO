package cn.study.netty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
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
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

public class HTTPSServer {
	/*
	 * 参考
	 * https://www.w3cschool.cn/essential_netty_in_action/essential_netty_in_action-srlx28c3.html
	 * 使用jdk生成证书
	 * https://www.cnblogs.com/ghq120/p/9063664.html
	 * 读取证书
	 * https://blog.csdn.net/huanongdetian/article/details/80175899
	 * 上一个网站读取的是javax.net.ssl.SSLContext而不是io.netty.handler.ssl.SslContext，所以参考下面例子
	 * https://www.cnblogs.com/zhjh256/p/6488668.html
	 */
	
	/*
	 * 
	 */
	
	
	public static void main(String[] args) throws Exception{
		HTTPSServer server = new HTTPSServer();
		server.start(8000);
	}
	
	public void start(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); 
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) 
					.childHandler(new HttpsCodecInitializer(HttpSslContextFactory.createSslContext(),false)).option(ChannelOption.SO_BACKLOG, 128) 
					.childOption(ChannelOption.SO_KEEPALIVE, true); 
 
			ChannelFuture f = b.bind(port).sync(); 
 
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public class HttpsCodecInitializer extends ChannelInitializer<Channel> {

	    private final SslContext context;
	    private final boolean client;

	    public HttpsCodecInitializer(SslContext context, boolean client) {
	        this.context = context;
	        this.client = client;
	    }

	    @Override
	    protected void initChannel(Channel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();
	        SSLEngine engine = context.newEngine(ch.alloc());
	        pipeline.addFirst("ssl", new SslHandler(engine));  //1

	        if (client) {
	            pipeline.addLast("codec", new HttpClientCodec());  //2
	        } else {
	            pipeline.addLast("codec", new HttpServerCodec());  //3
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
	
	//io.netty.handler.ssl.SslContext
	public static class HttpSslContextFactory {
	    /**针对于服务器端配置*/
	    private static SslContext sslContext = null;
	    static {
	        String algorithm = Security
	                .getProperty("ssl.KeyManagerFactory.algorithm");
	        if (algorithm == null) {
	            algorithm = "SunX509";
	        }
	        SslContext serverContext = null;
	        try {
	            KeyStore ks = KeyStore.getInstance("JKS");
	            ks.load(HttpsKeyStore.getKeyStoreStream(), HttpsKeyStore.getKeyStorePassword());
	            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
	            kmf.init(ks, HttpsKeyStore.getCertificatePassword());
	            serverContext = SslContextBuilder.forServer(kmf).build();
	        } catch (Exception e) {
	        	System.err.println("初始化server SSL失败"+e.getMessage());
	            throw new Error("Failed to initialize the server SSLContext", e);
	        }
	        sslContext = serverContext;
	    }
	    public static SslContext createSslContext(){
	    	return sslContext;
	    }
	}
	
	//javax.net.ssl.SSLContext
	public static class HttpSSLContextFactory2 {
	    private static final String PROTOCOL = "SSLv3";//客户端可以指明为SSLv3或者TLSv1.2
	    /**针对于服务器端配置*/
	    private static SSLContext sslContext = null;
	    static {
	        String algorithm = Security
	                .getProperty("ssl.KeyManagerFactory.algorithm");
	        if (algorithm == null) {
	            algorithm = "SunX509";
	        }
	        SSLContext serverContext = null;
	        try {
	            KeyStore ks = KeyStore.getInstance("JKS");
	            ks.load(HttpsKeyStore.getKeyStoreStream(), HttpsKeyStore.getKeyStorePassword());
	            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
	            kmf.init(ks, HttpsKeyStore.getCertificatePassword());
	            serverContext = SSLContext.getInstance(PROTOCOL);
	            serverContext.init(kmf.getKeyManagers(), null, null);
	        } catch (Exception e) {
	        	System.err.println("初始化server SSL失败"+e.getMessage());
	            throw new Error("Failed to initialize the server SSLContext", e);
	        }
	        sslContext = serverContext;
	    }
	    public static SSLEngine createSSLEngine() {
	        SSLEngine sslEngine = sslContext.createSSLEngine();
	        sslEngine.setUseClientMode(false);
	        sslEngine.setNeedClientAuth(false);
	        return sslEngine ;
	    }
	    public static SSLContext createSSLContext(){
	    	return sslContext;
	    }
	}
	
	public static class HttpsKeyStore {

	    /**
	     * 读取密钥
	     * @date 2012-9-11
	     * @version V1.0.0
	     * @return InputStream
	     */
	    public static InputStream getKeyStoreStream() {
	        InputStream inStream = null;
	        try {
	            inStream = new FileInputStream(VarConstant.keystorePath);
	        } catch (FileNotFoundException e) {
	            System.err.println("读取密钥文件失败"+e.getMessage());
	        }
	        return inStream;
	    }

	    /**
	     * 获取安全证书密码 (用于创建KeyManagerFactory)
	     * @date 2012-9-11
	     * @version V1.0.0
	     * @return char[]
	     */
	    public static char[] getCertificatePassword() {
	        return VarConstant.certificatePassword.toCharArray();
	    }

	    /**
	     * 获取密钥密码(证书别名密码) (用于创建KeyStore)
	     * @date 2012-9-11
	     * @version V1.0.0
	     * @return char[]
	     */
	    public static char[] getKeyStorePassword() {
	        return VarConstant.keystorePassword.toCharArray();
	    }
	}
	
	public static class VarConstant{
		public static Boolean sslEnabled = true;
		public static String keystorePath = "D:/test/tomcat.keystore";
		public static String certificatePassword = "123456";
		public static String keystorePassword = "123456";
	}
}
