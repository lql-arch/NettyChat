package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.FileTransfer;
import Server.processLogin.LoadSystem;
import Server.processLogin.Storage;
import config.ToMessage;
import config.execToVerify;
import io.netty.channel.*;
import message.FileMessage;
import message.ShowMessage;
import message.StringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static Server.ChatServer.uidChannelMap;
import static Server.processLogin.Delete.DeleteGroupFile;
import static Server.processLogin.Storage.storageGroupFiles;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {
    private static final Logger log = LogManager.getLogger();
    private static final String file_dir = "/home/bronya/tempFile";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        if(msg.isDeleteFile()){
            if(!msg.isPerson()){
                DeleteGroupFile(msg);
                uidChannelMap.get(msg.getMyUid()).writeAndFlush(msg);
            }
            return;
        }
        if(msg.isReadOrWrite()){//向外传输文件
            String path;
            if(msg.isPerson()) {
                if (msg.getPath() == null)
                    LoadSystem.loadReadFile(msg);
                FileTransfer.transferFile(ctx, msg);
            }else{//群文件
                if (msg.getPath() == null)
                    path = LoadSystem.loadReadFile(msg);//path为null代表文件不存在
                else
                    path = msg.getPath();
                sendForClient(ctx,msg,path);
            }
            return;
        }
        //保存文件
        if(msg.isPerson()) {//个人文件
            if(msg.getStartPos() == 0) {
                Channel channel = uidChannelMap.get(msg.getUser().getUid());
                StringMessage sm;
                String str = msg.getMe().getName() + "向你发送了一个文件";
                sm = new StringMessage(msg.getMe(), msg.getUser(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                sm.setDirect(true);
                if (channel != null) {
                    channel.writeAndFlush(sm);
                    str = "你向对方发送了一个文件"+msg.getName();
                    sm.setMessage(str);
                    ctx.channel().writeAndFlush(sm);
                }
                Storage.storageFileMsg(sm);
            }
            FileTransfer.storeFiles(ctx,msg,file_dir);
        }else{//群文件
            Channel channel = uidChannelMap.get(msg.getMyUid());
            if(msg.getStartPos() == 0){//第一次
                firstSend(ctx,msg);
            }else{//后面
                GroupSend(ctx,msg);
            }
        }

    }

    public static void firstSend(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        String path = file_dir + File.separator + msg.getName();
        int readLen = msg.getEndPos();
        long start = msg.getStartPos();
        if(start == 0)
            storageGroupFiles(path,msg);

        byte[] bytes = msg.getBytes();
        File file = new File(path);
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw");){
            raf.seek(start);
            raf.write(bytes);
            start += readLen ;
            msg.setStartPos(start);

            if(readLen > 0){
                ctx.channel().writeAndFlush(msg);
            }
            else{
                log.debug("读入完毕");
            }
            if(readLen != 1024 * 1024 * 2){
                log.debug("写入完毕");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void GroupSend(ChannelHandlerContext ctx,FileMessage msg){
        String path = file_dir + File.separator + msg.getName();
        int readLen = msg.getEndPos();
        long start = msg.getStartPos();

        byte[] bytes = msg.getBytes();
        File file = new File(path);
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw");){
            raf.seek(start);
            raf.write(bytes);
            start += readLen ;

            time(start,msg.getFileLen());

            if(start == msg.getFileLen()){
                String sum = msg.getSha1sum();
                msg.setPath(path);
                if(!execToVerify.equal(sum,file.getPath())){
                    DeleteGroupFile(msg);
                    String str = "sa1sum值错误，请重试传输，或通知人员修复";
                    ctx.writeAndFlush(new ShowMessage().setStr(str).setRequest(false).setUid(msg.getUid()));
                    log.warn(str);
                }else
                    log.debug("写入完毕");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void sendForClient(ChannelHandlerContext ctx, FileMessage msg,String path) throws Exception {
        if(msg.getPath() == null || path == null){
            log.error("查无此文件");
            msg.setName(null);
            ctx.channel().writeAndFlush(msg);
            return;
        }
        msg.setSha1sum(execToVerify.sha1Verify(path));

        EventLoopGroup executors = new DefaultEventLoopGroup(16);
        executors.execute(() -> {
            log.debug("defaultEventLoopGroup启动");
            long start = msg.getStartPos();
            Channel channel = uidChannelMap.get(msg.getMyUid());

            File file = new File(path);
            if(!file.exists()){
                log.error("文件已消失");
                msg.setName(null);
                msg.setPath(null);
                channel.writeAndFlush(msg);
                return;
            }

            msg.setFileLen(file.length());
            int length = (int)(file.length()/10 < 1024 * 1024 * 4 ? file.length() / 10 : 1024 * 1024 * 4);
            long endLen = file.length() - start;
            int lastLength = length < endLen ? length : ( endLen > 0 ? (int)endLen : 0);
            byte[] bytes = new byte[lastLength];

            try(RandomAccessFile raf = new RandomAccessFile(file,"rw")) {
                int read ;
                raf.seek(msg.getStartPos());
                while ((read = raf.read(bytes)) != -1){
                    msg.setEndPos(read);
                    msg.setBytes(bytes);
                    msg.setStartPos(start);

                    ChannelFuture channelFuture = channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            time(msg.getStartPos() + msg.getEndPos(), msg.getFileLen());
                        }
                        else{
                            log.debug("Failed to send message.");
                            return;
                        }
                        Throwable cause = future.cause();
                        if (cause != null) {
                            log.warn(cause);
                        }
                    });
                    channelFuture.awaitUninterruptibly(1,TimeUnit.SECONDS);

                    start += read;

                    endLen = file.length() - start;
                    lastLength = length < endLen ? length : ( endLen > 0 ? (int)endLen : 0);
                    bytes = new byte[lastLength];
                    if(start == file.length() || lastLength == 0){
                        log.debug("发送完毕");
                        break;
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    private static void time(long start,long fileLength){
        ToMessage cpb = new ToMessage(50, '#');
        cpb.show((int) (start * 1.00/fileLength *100));
    }
}
