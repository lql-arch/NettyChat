package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.LoadSystem;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FileRead;

public class FileReadHandler extends SimpleChannelInboundHandler<FileRead> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileRead msg) throws Exception {
        Channel channel = ChatServer.uidChannelMap.get(msg.getUid());
        channel.writeAndFlush(LoadSystem.loadFile(msg,msg.isSingleOrGroup()));
    }
}
