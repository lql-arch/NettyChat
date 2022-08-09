package client.SimpleChannelHandler;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.HistoricalNews;

public class FindHistoricalNews extends SimpleChannelInboundHandler<HistoricalNews> {
    public static HistoricalNews historicalNews;
    public static HistoricalNews groupHistoricalNews;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HistoricalNews msg) throws Exception {
        if(msg.isPersonOrGroup()){
            historicalNews = msg;
            Start.semaphore.release();
        }else{
            groupHistoricalNews = msg;
            Start.semaphore.release();
        }
    }
}
