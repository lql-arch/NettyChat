package Server.SimpleChannelHandler;

import Server.processLogin.Storage;
import Server.processLogin.Verify;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.StringMessage;

import static Server.ChatServer.blackMap;
import static Server.ChatServer.uidChannelMap;

public class StringHandler extends SimpleChannelInboundHandler<StringMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StringMessage msg) throws Exception {
        if(blackMap.get(msg.getMe().getUid()) == null || blackMap.get(msg.getMe().getUid()).compareTo(msg.getFriend().getUid()) != 0) {
            if(!Verify.verifyBlack(msg)) {
                Channel channel = uidChannelMap.get(msg.getFriend().getUid());
                if (channel != null) {
                    channel.writeAndFlush(msg);
                }//在线
                Storage.storageSingleMessage(msg);
            }
        }
    }
}
