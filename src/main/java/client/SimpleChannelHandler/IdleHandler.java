package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import message.IdleMessage;

public class IdleHandler extends SimpleChannelInboundHandler<IdleMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IdleMessage msg) throws Exception {

    }

    // 用来触发特殊事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 触发了写空闲事件
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                ctx.channel().writeAndFlush(new IdleMessage().setStr(Start.uid+"!存活"));
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
