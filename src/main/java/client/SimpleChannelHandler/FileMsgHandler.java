package client.SimpleChannelHandler;

import client.Start;
import config.ToMessage;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import message.FileMessage;
import message.UserMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {
    public static String file_dir ;
    public static Semaphore fileSemaphore = new Semaphore(0);

    public static void sendFile(ChannelHandlerContext ctx, File file,UserMessage me, UserMessage user) throws InterruptedException {
        FileMessage fm = new FileMessage();
        fm.setStartPos(0);

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {
            randomAccessFile.seek(fm.getStartPos());
            int length = (int) ((file.length() / 10) < 1024*1024*2 ? (file.length() / 10) : 1024*1024*2);
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
                fm.setPerson(true);
                ctx.writeAndFlush(fm.setReadOrWrite(false));

                time(read, file.length());
            }else{
                System.out.println("文件已读完");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        fileSemaphore.acquire();
    }

    public static void receiveFiles(ChannelHandlerContext ctx, FileMessage msg){
        int readLen = msg.getEndPos();
        long start = msg.getStartPos();
        String path = file_dir + File.separator + msg.getName();

        byte[] bytes = msg.getBytes();
        File file = new File(path);
        try(RandomAccessFile raf = new RandomAccessFile(file,"rw")){
            raf.seek(start);
            raf.write(bytes);
            start += readLen;
            msg.setStartPos(start);
            if(readLen > 0){
                ctx.writeAndFlush(msg);
            }
            time(start,msg.getFileLen());

            if(readLen <= 0 || start == msg.getFileLen()){
                fileSemaphore.release();
                System.out.println("写入完毕");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        if(msg.isDeleteFile()){
            if(!msg.isPerson()){
                if(msg.getPath() == null){
                    System.err.println("查无此文件，请刷新后重试");
                }else{
                    System.out.println("删除成功");
                }
                Start.semaphore.release();
            }
            return;
        }

        if(msg.isPerson()) {
            if (msg.isReadOrWrite()) {
                receiveFiles(ctx, msg);
                return;
            }
            if (msg.getStartPos() != -1) {
                try (RandomAccessFile raf = new RandomAccessFile(msg.getPath(), "rw")) {
                    long start = msg.getStartPos();
                    int read;
                    int lastLength;

                    raf.seek(start);
                    int length = (int) (Math.min((msg.getFileLen() / 10), 1024 * 1024 * 2));
                    lastLength = length < (msg.getFileLen() - msg.getStartPos()) ? length : (int) (msg.getFileLen() - msg.getStartPos());
                    if (lastLength < 0) {
                        System.err.println("Error：lastLength为负数.");
                        return;
                    }
                    if (lastLength == 0 || start == msg.getFileLen()) {
                        fileSemaphore.release();
                        System.out.println("文件读取完毕");
                        return;
                    }
                    byte[] bytes = new byte[lastLength];
                    time(msg.getStartPos(),msg.getFileLen());
                    if ((read = raf.read(bytes)) != -1) {
                        msg.setEndPos(read);
                        msg.setBytes(bytes);
                        msg.setEndPos(read);
                        ctx.writeAndFlush(msg);

                        start += read;
                        time(start,msg.getFileLen());
                    }
                }
            }
        }else{//group
            if(msg.isReadOrWrite()){
                receiveGroupFiles(ctx, msg);
            }else{
                sendGroupFile(ctx, msg);
            }
        }
    }

    public static void receiveGroupFiles(ChannelHandlerContext ctx, FileMessage msg){
        if(msg.getName().compareTo(msg.getPath()) != 0 && msg.getStartPos() == 0){
            System.err.println("查无此文件");
            return;
        }

        long start = msg.getStartPos();
        int read = msg.getEndPos();
        String path = file_dir + File.separator + msg.getName();

        File file = new File(path);
        byte[] bytes = msg.getBytes();

        try(RandomAccessFile raf = new RandomAccessFile(file,"rw")){
            raf.seek(start);
            raf.write(bytes);
            start += read;

            time(start,msg.getFileLen());
            if(start >= msg.getFileLen()){
                Start.semaphore.release();
            }


        }catch (IOException e){
            e.printStackTrace();
        }

    }
    public static void sendGroupFile(ChannelHandlerContext ctx, FileMessage msg){

        AtomicLong start = new AtomicLong(msg.getStartPos());

        File file = new File(msg.getPath());
        try(RandomAccessFile raf = new RandomAccessFile(file,"rw")){
            raf.seek(start.get());
            int length = (int) (Math.min((msg.getFileLen() / 10), 1024 * 1024 * 2));
            int lastLength = length < (file.length() - start.get()) ? length : (int) (file.length() - start.get());

            byte[] bytes = new byte[lastLength];
            int read ;
            while((read = raf.read(bytes)) != -1){
                msg.setStartPos(start.get());
                msg.setEndPos(read);
                msg.setBytes(bytes);
                ChannelFuture channelFuture = ctx.channel().writeAndFlush(msg);

                start.compareAndSet(start.get(), start.get() + read);
                time(start.get(),file.length());//显示进度条
                lastLength = length < (file.length() - start.get()) ? length : (int) (file.length() - start.get());
                bytes = new byte[lastLength];
                if(start.get() == file.length() ){
                    System.out.println("发送完毕");
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        Start.semaphore.release();

    }

    private static void time(long start,long fileLength){
        ToMessage cpb = new ToMessage(50, '#');
        cpb.show((int) (start * 1.00/fileLength *100));
    }
}