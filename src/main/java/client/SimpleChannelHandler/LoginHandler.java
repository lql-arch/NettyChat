package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.LoginMessage;

public class LoginHandler extends SimpleChannelInboundHandler<LoginMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) throws Exception {
        if(msg.isLogOut()){
            System.out.println("注销成功");
            Start.semaphore.release();
        }
    }
}
