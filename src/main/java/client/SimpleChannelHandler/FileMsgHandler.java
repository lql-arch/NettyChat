package client.SimpleChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import message.FileMessage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {

//    private static FileMessage fm = new FileMessage();
    private static final NioEventLoopGroup group = new NioEventLoopGroup(1);


    public static void sendFile(ChannelHandlerContext ctx,File file){
        FileMessage fm = new FileMessage();
        fm.setStartPos(0);

        group.next().submit(() -> {
            try(RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {
                randomAccessFile.seek(fm.getStartPos());
                int length = (int) ((file.length() / 10) < 1024 ? (file.length() / 10) : 1024);
                byte[] bytes = new byte[length];
                int read;
                fm.setName(file.getName());
                if((read = randomAccessFile.read(bytes)) != -1){
                    fm.setBytes(bytes);
                    fm.setEndPos(read);
                    ctx.writeAndFlush(fm);
                }else{
                    String str = "文件已读完";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return "文件传输";
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {

    }
}
