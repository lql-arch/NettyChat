package Server.SimpleChannelHandler;

import Server.processLogin.ReviseMaterial;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ReviseMsgStatusMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReviseMsgStatusHandler extends SimpleChannelInboundHandler<ReviseMsgStatusMessage> {
    private static final Logger log = LogManager.getLogger();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReviseMsgStatusMessage msg) throws Exception {
        //修改所有time之前的消息为已读
        if(!ReviseMaterial.reviseMessageStatus(msg)){
            log.warn("reviseMessageStatus failed:"+msg.getTime());
        }
    }
}
