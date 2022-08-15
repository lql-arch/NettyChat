package Server.SimpleChannelHandler;

import Server.processLogin.Verify;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FindMessage;

public class FindHandler extends SimpleChannelInboundHandler<FindMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FindMessage msg) throws Exception {
        msg.setResult(Verify.verifyPassword(msg));
        ctx.channel().writeAndFlush(msg);
    }
}
