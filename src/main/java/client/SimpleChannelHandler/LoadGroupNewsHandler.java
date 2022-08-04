package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.LoadGroupMessage;

public class LoadGroupNewsHandler extends SimpleChannelInboundHandler<LoadGroupMessage> {
    public static LoadGroupMessage groupMessage ;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoadGroupMessage msg) throws Exception {
        groupMessage = msg;
        Start.semaphore.release();
    }
}
