package client.SimpleChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import message.FileMessage;
import message.UserMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {
    private static final Logger log = LogManager.getLogger();

    private static final NioEventLoopGroup group = new NioEventLoopGroup(1);
    public static FileMessage fm = new FileMessage();


    public static void sendFile(ChannelHandlerContext ctx, File file,UserMessage me, UserMessage user){
        FileMessage fm = new FileMessage();
        fm.setStartPos(0);

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {
            randomAccessFile.seek(fm.getStartPos());
            int length = (int) ((file.length() / 10) < 1024*2 ? (file.length() / 10) : 1024*2);
            byte[] bytes = new byte[length];
            int read;
            fm.setName(file.getName());
            fm.setFileLen(file.length());
            fm.setPath(file.getPath());
            if((read = randomAccessFile.read(bytes)) != -1){
                fm.setBytes(bytes);
                fm.setEndPos(read);
                fm.setUser(user);
                fm.setMe(me);
                ctx.writeAndFlush(fm.setReadOrWrite(false));
            }else{
                System.out.println("文件已读完");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        if(msg.isReadOrWrite()){

            return;
        }
        if(msg.getStartPos() != -1){
            try(RandomAccessFile raf = new RandomAccessFile(msg.getPath(),"rw")) {
                raf.seek(msg.getStartPos());
                int read;
                int lastLength;
                if (msg.getFileLen() < 1024) {
                    lastLength = (int) msg.getFileLen();
                } else {
                    int length = (int) (Math.min((msg.getFileLen() / 10), 1024 * 2));
                    lastLength = length < (msg.getFileLen() - msg.getStartPos()) ? length : (int) (msg.getFileLen() - msg.getStartPos());
                }
                if (lastLength < 0) {
                    System.err.println("Error：lastLength为负数.");
                    return;
                }
                if (lastLength == 0) {
                    System.out.println("文件读取完毕");
                    return;
                }
                byte[] bytes = new byte[lastLength];
//            log.debug("byte 长度：" + bytes.length);
                if ((read = raf.read(bytes)) != -1) {
                    msg.setEndPos(read);
                    msg.setBytes(bytes);
                    ctx.writeAndFlush(msg);
                } else {
                    System.out.println("文件读取完毕!");
                }
            }
        }

    }
}
