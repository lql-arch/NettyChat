package Server.SimpleChannelHandler;

import Server.processLogin.FileTransfer;
import Server.processLogin.LoadSystem;
import Server.processLogin.Storage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.FileMessage;
import message.StringMessage;

import java.time.LocalDateTime;
import java.util.Map;

public class FileMsgHandler extends SimpleChannelInboundHandler<FileMessage> {
    private static final String file_dir = "~/tempFile";
    Map<String,Channel> uidChannelMap;
    Map<Channel,String> channelUidMap;

    public FileMsgHandler(Map<String,Channel> uidChannelMap,Map<Channel,String> channelUidMap){
        this.uidChannelMap = uidChannelMap;
        this.channelUidMap = channelUidMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMessage msg) throws Exception {
        if(msg.isReadOrWrite()){//向外传输文件


            return;
        }
        if(msg.isPerson()) {//保存文件
            Channel channel = uidChannelMap.get(msg.getUser().getUid());
            StringMessage sm;
            String str = msg.getMe().getName() + "向你发送了一个文件";
            sm = new StringMessage(msg.getMe(), msg.getUser(), str, LocalDateTime.now().toString());
            if(channel != null) {
                channel.writeAndFlush(sm);
            }
            Storage.storageFileMsg(sm);
            FileTransfer.storeFiles(ctx,msg,file_dir);
        }else{

        }

    }
}
