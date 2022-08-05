package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import Server.processLogin.Storage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.LoadGroupMessage;

import java.sql.Timestamp;

public class LoadGroupNewsHandler extends SimpleChannelInboundHandler<LoadGroupMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoadGroupMessage msg) throws Exception {
        if(msg.isBuildGroup()){
            Storage.buildGroup(msg);
            ctx.writeAndFlush(msg);
            return;
        }
        LoadSystem.loadGroupMessages(msg);
        ctx.channel().writeAndFlush(msg);
    }


}