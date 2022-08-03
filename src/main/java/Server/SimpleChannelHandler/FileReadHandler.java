package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FileRead;

public class FileReadHandler extends SimpleChannelInboundHandler<FileRead> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileRead msg) throws Exception {
        if(msg.isCheckFile()){
            ctx.writeAndFlush(LoadSystem.loadFile(msg));
        }
    }
}
