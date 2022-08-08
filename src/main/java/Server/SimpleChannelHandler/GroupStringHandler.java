package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.ReviseMaterial;
import Server.processLogin.Storage;
import Server.processLogin.Verify;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.GroupStringMessage;

import java.util.List;

public class GroupStringHandler extends SimpleChannelInboundHandler<GroupStringMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupStringMessage msg) throws Exception {
        if(msg.isReviseLastTime()){//修改last
            ReviseMaterial.reviseLastTime(msg);
            return;
        }

        Storage.storageGroupString(msg);
        List<String> uids = Verify.verifyGroupMembers(msg);
        for(String uid : uids){
            Channel channel = ChatServer.uidChannelMap.get(uid);
            if(channel != null)
                channel.writeAndFlush(msg);
        }
    }
}
