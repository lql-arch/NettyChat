package client.SimpleChannelHandler;

import client.Start;
import client.normal.GroupChat_text;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.LoadGroupMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class LoadGroupNewsHandler extends SimpleChannelInboundHandler<LoadGroupMessage> {
    private static final Logger log = LogManager.getLogger();
    public static LoadGroupMessage groupMessage ;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoadGroupMessage msg) throws Exception {
        groupMessage = msg;
        Start.semaphore.release();
    }
}
