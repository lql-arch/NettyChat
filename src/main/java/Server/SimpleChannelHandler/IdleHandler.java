package Server.SimpleChannelHandler;

import Server.ChatServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import message.IdleMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdleHandler extends SimpleChannelInboundHandler<IdleMessage> {
    private static final Logger log = LogManager.getLogger(IdleHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IdleMessage msg) throws Exception {
        String[] str = msg.getStr().split("!");
        log.info("{}({}),{}",ChatServer.uidChannelMap.get(str[0]),str[0],str[1]);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.channel().writeAndFlush(new IdleMessage().setStr("server"));
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
