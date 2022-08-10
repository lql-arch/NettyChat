package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.Delete;
import Server.processLogin.ReviseMaterial;
import Server.processLogin.Storage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ReviseGroupMemberMessage;

import java.util.List;

public class ReviseGroupMemberHandler extends SimpleChannelInboundHandler<ReviseGroupMemberMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReviseGroupMemberMessage msg) throws Exception {
        if(msg.isRemoveGroup()) {
            Delete.DeleteGroupMember(msg);
            Storage.storageGroupNotice(msg, msg.getUid() + "被" + msg.getManageUid() + "移除群聊",1);
        }
        if(msg.isSetManage()) {
            ReviseMaterial.SetGroupManage(msg);
            Storage.storageGroupNotice(msg,msg.getUid() + "被" + msg.getManageUid() + "设置为管理",1);
        }
        if(msg.isDisbandGroupChat()){
            Delete.DeleteGroup(msg);
            Storage.storageGroupNotice(msg,msg.getManageUid() + "解散了群聊" + msg.getGid(),0);
        }
        Channel channel = ChatServer.uidChannelMap.get(msg.getManageUid());
        channel.writeAndFlush(msg);
    }
}