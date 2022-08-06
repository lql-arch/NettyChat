package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.GroupNoticeMessage;

public class GroupNoticeHandler extends SimpleChannelInboundHandler<GroupNoticeMessage> {
    public static GroupNoticeMessage gnm ;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupNoticeMessage msg) throws Exception {
        gnm = msg;
        Start.semaphore.release();
    }
}
