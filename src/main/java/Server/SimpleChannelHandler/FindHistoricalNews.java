package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.LoadSystem;
import client.normal.GroupChat_text;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.HistoricalNews;

import java.sql.Timestamp;
import java.util.Iterator;

public class FindHistoricalNews extends SimpleChannelInboundHandler<HistoricalNews> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HistoricalNews msg) throws Exception {
        Channel channel = ChatServer.uidChannelMap.get(msg.getUid());
        if(msg.isPersonOrGroup()){
            LoadSystem.loadHistory(msg);
            channel.writeAndFlush(msg);
        }else{
            LoadSystem.loadGroupHistory(msg);
            channel.writeAndFlush(msg);
        }
    }
}
