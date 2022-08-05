package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FindGroupMessage;

public class FindGroupHandler extends SimpleChannelInboundHandler<FindGroupMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FindGroupMessage msg) throws Exception {
        LoadSystem.loadGroup(msg);
        ctx.writeAndFlush(msg);
    }
}
