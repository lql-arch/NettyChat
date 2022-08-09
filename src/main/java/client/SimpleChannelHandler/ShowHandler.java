package client.SimpleChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ShowMessage;

public class ShowHandler extends SimpleChannelInboundHandler<ShowMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ShowMessage msg) throws Exception {
        if(msg.isRequest())
            System.out.println("有申请消息:"+msg.getStr());
        else
            System.out.println(msg.getStr());
    }
}