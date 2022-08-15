package Server.SimpleChannelHandler;

import Server.processLogin.LoadSystem;
import Server.processLogin.ReviseMaterial;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.LoadMessage;
import message.LoginStringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static Server.ChatServer.channelUidMap;
import static Server.ChatServer.uidChannelMap;

public class LoginStringHandler extends SimpleChannelInboundHandler<LoginStringMessage> {
    private static final Logger log = LogManager.getLogger();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginStringMessage msg) throws Exception {
        String str = msg.getMessage();
        String[] strings = str.split("!");
        if(str.compareTo(("Goodbye!")) == 0) {
            log.info(channelUidMap.get(ctx.channel())+":Goodbye!");
            log.info(channelUidMap.get(ctx.channel()) + "已退出！");
        } else if(str.equals("err!")){
        } else if(str.startsWith("start")){
            LoadMessage lm = LoadSystem.loadMessage(strings[1],0);
            log.info("start!");
            ctx.channel().writeAndFlush(lm);
        }else if(str.startsWith("flush")){
            LoadMessage lm = LoadSystem.loadMessage(strings[1],3);
            ctx.channel().writeAndFlush(lm);
        }
        else if(str.startsWith("singleLoad")){
            log.info("读取单聊信息："+strings[1]);
            ctx.channel().writeAndFlush(LoadSystem.SingleChat(strings[1]));
        }else if(str.startsWith("group")){
            log.info("读取群聊消息："+strings[1]);
            ctx.channel().writeAndFlush(LoadSystem.GroupChat(strings[1]));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        Channel channel = ctx.channel();
        String uid =  channelUidMap.get(channel);
        //设置为离线状态
        ReviseMaterial.reviseOnline(uid,false);
        //当有客户端断开连接的时候,就移除对应的通道
        channelUidMap.remove(channel,uid);
        uidChannelMap.remove(uid,channel);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        String uid =  channelUidMap.get(channel);
        //当有客户端断开连接的时候,就移除对应的通道
        uidChannelMap.remove(uid,channel);
        channelUidMap.remove(channel,uid);
        super.exceptionCaught(ctx, cause);
    }
}
