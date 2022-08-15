package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.UserMessage;

public class UserHandler extends SimpleChannelInboundHandler<UserMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UserMessage msg) throws Exception {
        String uid = msg.getUid();
        UserMessage um = LoadSystem.friendMaterial(uid);
        ctx.writeAndFlush(um);
    }
}
