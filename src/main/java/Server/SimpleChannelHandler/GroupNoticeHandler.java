package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.GroupNoticeMessage;

public class GroupNoticeHandler extends SimpleChannelInboundHandler<GroupNoticeMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupNoticeMessage msg) throws Exception {
        LoadSystem.loadGroupNotice(msg);
        ctx.writeAndFlush(msg);
    }
}
