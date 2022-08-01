package Server.SimpleChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FileMessage;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {
    private String file_dir = "/home/bronya/temp";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {

    }
}
