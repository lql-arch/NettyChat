package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ReviseGroupMemberMessage;

public class ReviseGroupMemberHandler extends SimpleChannelInboundHandler<ReviseGroupMemberMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReviseGroupMemberMessage msg) throws Exception {
        if(msg.isRemoveGroup() ){
            System.out.println("移除成功");
        }else if(msg.isSetManage()){
            System.out.println("已成功设置成管理");
        }else if(msg.isDisbandGroupChat()){
            System.out.println("已成功解散该群.");
        }
        Start.semaphore.release();
    }
}
