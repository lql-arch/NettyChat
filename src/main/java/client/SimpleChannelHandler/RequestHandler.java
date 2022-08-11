package client.SimpleChannelHandler;

import client.System.DeleteSystem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.RequestMessage;

public class RequestHandler extends SimpleChannelInboundHandler<RequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        if(msg.isGroupORSingle()){
            if(!msg.isConfirm() && msg.isFriend()){
                System.err.println("你已经进入该群了");
            }else{
                System.out.println("你已退出该群");
            }
            return;
        }
        if(msg.isFriend()){
            System.err.println("你与目标已经是好友了！");
        }else{
            System.out.println(msg.getNotice());
            DeleteSystem.semaphoreFriend.release();
        }
    }
}
