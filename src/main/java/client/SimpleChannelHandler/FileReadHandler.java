package client.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import client.Start;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FileRead;

public class FileReadHandler extends SimpleChannelInboundHandler<FileRead> {
    public static FileRead fileRead;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileRead msg) throws Exception {
        fileRead = msg;
        Start.semaphore.release();
    }
}
