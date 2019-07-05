package api.socketserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import api.domain.Rabbit;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class ApiSocketServerHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(ApiSocketServerHandler.class);

	private final String DEL = "" + (char) 20;
	private final String _DEL = "" + (char) 15;

	private Rabbit r;

	public ApiSocketServerHandler(Rabbit r) {
		this.r = r;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.write("UCEX V1\r\n");
		ctx.flush();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		logger.info("[Socket Server RCV] {} ", msg);

		try {
			passQueue(msg);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	private void passQueue(Object msg) throws Exception {
		String rcv = msg.toString().replaceAll(_DEL, DEL);

		String[] tokens = rcv.split(DEL);

		if (tokens.length != 5 || !tokens[0].equals("TUCWebNotice"))
			return;

		r.sendCubeMessage("<TUCWebNotice><UserId>" + tokens[1] + "</UserId><TargetUser>" + tokens[2]
				+ "</TargetUser><Title><![CDATA[" + tokens[3] + "]]></Title><Description>" + tokens[4]
				+ "</Description></TUCWebNotice>");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {

		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error(cause.toString(), cause);
		ctx.close();
	}
}
