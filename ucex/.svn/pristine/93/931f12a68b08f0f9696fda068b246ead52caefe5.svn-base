package api.socketserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.config.SSLProperties;
import api.domain.Rabbit;

@Configuration
@EnableConfigurationProperties(SSLProperties.class)
public class ApiSocketServer implements Runnable {

	@Autowired
	private Rabbit r;

	@Autowired
	private SSLProperties sslProperties;

	private static Logger logger = LoggerFactory.getLogger(ApiSocketServer.class);

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();

	public ApiSocketServer() {
	}

	@Bean
	public Thread start() {

		Thread daemon = new Thread(this);
		daemon.start();
		return daemon;
	}

	@Override
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						public void initChannel(SocketChannel ch) {

							ChannelPipeline p = ch.pipeline();
							p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
							p.addLast(DECODER);
							p.addLast(ENCODER);
							p.addLast(new ApiSocketServerHandler(r));
						}

					});

			ChannelFuture f = b.bind(sslProperties.getApiServerSocketPort()).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error(e.toString(), e);

		} finally {

			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
