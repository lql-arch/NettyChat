package Server.SimpleChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ShowMessage;

public class ShowHandler extends SimpleChannelInboundHandler<ShowMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ShowMessage msg) throws Exception {

    }
}
