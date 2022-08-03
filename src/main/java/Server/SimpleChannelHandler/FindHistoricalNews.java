package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.HistoricalNews;

public class FindHistoricalNews extends SimpleChannelInboundHandler<HistoricalNews> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HistoricalNews msg) throws Exception {
        if(msg.isPersonOrGroup()){
            LoadSystem.loadHistory(msg);
            ctx.channel().writeAndFlush(msg);
        }else{

        }
    }
}
