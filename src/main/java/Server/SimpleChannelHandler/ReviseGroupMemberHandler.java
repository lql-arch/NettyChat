package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.Delete;
import Server.processLogin.ReviseMaterial;
import Server.processLogin.Storage;
import Server.processLogin.Verify;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ReviseGroupMemberMessage;
import message.ShowMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ReviseGroupMemberHandler extends SimpleChannelInboundHandler<ReviseGroupMemberMessage> {
    private static final Logger log = LogManager.getLogger();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReviseGroupMemberMessage msg) throws Exception {
        if(msg.isRemoveGroup()) {
            Delete.DeleteGroupMember(msg);
            Storage.storageGroupNotice(msg, msg.getUid() + "被" + msg.getManageUid() + "移除群聊",1);
        }
        if(msg.isSetManage()) {
            ReviseMaterial.SetGroupManage(msg);
            if(msg.isRemoveAdm()){
                Storage.storageGroupNotice(msg,msg.getUid() + "被" + msg.getManageUid() + "移除管理",1);
            }else{
                Storage.storageGroupNotice(msg, msg.getUid() + "被" + msg.getManageUid() + "设置为管理", 1);
            }
        }
        if(msg.isDisbandGroupChat()){
            String name = Verify.verifyGroupName(msg.getGid());
            List<String> members = Delete.DeleteGroup(msg);
            String notice = msg.getManageUid() + "解散了群聊" + name+"("+msg.getGid()+")";
            Storage.storageGroupNotice(msg,notice,0);
            for(String member : members){
                Channel channel = ChatServer.uidChannelMap.get(member);
                if(channel != null)
                    channel.writeAndFlush(new ShowMessage().setRequest(false).setStr(notice));
            }
        }
        Channel channel = ChatServer.uidChannelMap.get(msg.getManageUid());
        channel.writeAndFlush(msg);
    }
}
