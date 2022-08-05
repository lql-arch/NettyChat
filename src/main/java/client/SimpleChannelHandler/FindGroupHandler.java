package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FindGroupMessage;

public class FindGroupHandler extends SimpleChannelInboundHandler<FindGroupMessage> {
    public static FindGroupMessage fgm ;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FindGroupMessage msg) throws Exception {
        fgm = msg;
        Start.semaphore.release();
    }
}
