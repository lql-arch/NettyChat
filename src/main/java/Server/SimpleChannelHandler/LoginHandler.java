package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.Delete;
import Server.processLogin.ProcessLogin;
import Server.processLogin.ReviseMaterial;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.LoginMessage;
import message.LoginStringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static Server.ChatServer.channelUidMap;
import static Server.ChatServer.uidChannelMap;

public class LoginHandler extends SimpleChannelInboundHandler<LoginMessage> {
    private static final Logger log = LogManager.getLogger(LoginHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) throws Exception {
        if(msg.isLogOut()){
            Delete.LogOut(msg);
            uidChannelMap.get(msg.getUid()).writeAndFlush(msg);
            return;
        }
        if(!ProcessLogin.login(msg)){
            ctx.channel().writeAndFlush(new LoginStringMessage("password error!"));
        }else {
            if(msg.getIsLogin()) {
                String uid = msg.getUid();
                Channel channel = ctx.channel();
                Channel channel1 = uidChannelMap.get(uid);
                if(channel1 != null){
                    channelUidMap.remove(channel1,uid);
                    channelUidMap.put(channel,uid);
                    uidChannelMap.replace(uid,channel1,channel);
                    channel1.writeAndFlush(new LoginStringMessage("you have been pushed off the line"));
                    channel.writeAndFlush(new LoginStringMessage("someone is online!"));
                    channel1.close();
                    ReviseMaterial.reviseOnline(uid,true);
                }else {
                    ReviseMaterial.reviseOnline(uid,true);
                    channelUidMap.put(channel, uid);
                    uidChannelMap.put(uid, channel);
                    channel.writeAndFlush(new LoginStringMessage("login success!"));
                }
                log.info(uid + "已登录");
            }else{
                ctx.channel().writeAndFlush(new LoginStringMessage("register success!" +
                        "你的uid为"+msg.getUid()));
            }
        }
    }
}
