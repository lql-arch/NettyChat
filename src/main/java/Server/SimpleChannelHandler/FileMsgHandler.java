package Server.SimpleChannelHandler;

import Server.processLogin.FileTransfer;
import Server.processLogin.LoadSystem;
import Server.processLogin.Storage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FileMessage;
import message.LoadMessage;
import message.StringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {
    private static final Logger log = LogManager.getLogger();
    private static final String file_dir = "/home/bronya/tempFile";
    Map<String,Channel> uidChannelMap;
    Map<Channel,String> channelUidMap;

    public FileMsgHandler(Map<String,Channel> uidChannelMap,Map<Channel,String> channelUidMap){
        this.uidChannelMap = uidChannelMap;
        this.channelUidMap = channelUidMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        if(msg.isReadOrWrite()){//向外传输文件
//            log.debug(msg.getTime()+" "+msg.getMe().getUid()+" "+ msg.getName());
            if(msg.getPath() == null)
                LoadSystem.loadReadFile(msg);
            FileTransfer.transferFile(ctx,msg);
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

        }

    }
}
