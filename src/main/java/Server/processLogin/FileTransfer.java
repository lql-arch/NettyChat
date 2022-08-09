package Server.processLogin;

import io.netty.channel.ChannelHandlerContext;
import message.FileMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import static Server.processLogin.Storage.storageFiles;

public class FileTransfer {
    private static final Logger log = LogManager.getLogger();
    public static void storeFiles(ChannelHandlerContext ctx, FileMessage msg, String file_dir) throws FileNotFoundException, SQLException {
        int readLen = msg.getEndPos();
        long start = msg.getStartPos();
        String path = file_dir + File.separator + msg.getName();
        if(start == 0)
            storageFiles(msg.getName(),path,msg, false);

        byte[] bytes = msg.getBytes();
        File file = new File(path);
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw");){
            raf.seek(start);
            raf.write(bytes);
            start += readLen ;
            msg.setStartPos(start);

            if(readLen > 0){
                ctx.writeAndFlush(msg);
            }else{
                log.debug("读入完毕");
            }
            if(readLen != 1024 * 1024 * 2){
                storageFiles(msg.getName(),path,msg, true);
                log.debug("写入完毕");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void transferFile(ChannelHandlerContext ctx, FileMessage msg)  {
        File file = new File(msg.getPath());
        try(RandomAccessFile raf = new RandomAccessFile(file,"r")){
            raf.seek(msg.getStartPos());
            int read;
            int lastLength;
            if (file.length() < 1024) {
                lastLength = (int) file.length();
            } else {
                int length = (int) (Math.min((file.length() / 10), 1024 * 1024*2));
                lastLength = length < (file.length() - msg.getStartPos()) ? length : (int) (file.length() - msg.getStartPos());

            }
            byte[] bytes = new byte[lastLength];

            if (lastLength == 0) {
                System.out.println("文件读取完毕");
                return;
            }

            if((read = raf.read(bytes)) != -1 ){
                msg.setEndPos(read);
                msg.setFileLen(file.length());
                msg.setBytes(bytes);
                ctx.writeAndFlush(msg);
            }else{
                log.debug("文件读取完毕");
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
