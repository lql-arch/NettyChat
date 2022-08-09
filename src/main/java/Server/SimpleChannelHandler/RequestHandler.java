package Server.SimpleChannelHandler;

import Server.ChatServer;
import Server.processLogin.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.RequestMessage;
import message.ShowMessage;
import message.StringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class RequestHandler extends SimpleChannelInboundHandler<RequestMessage> {
    private static final Logger log = LogManager.getLogger();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        if(msg.isGroupORSingle()){
            Channel channel = ChatServer.uidChannelMap.get(msg.getRequestPerson().getUid());
            if(msg.isAddOrDelete()){
                if(!msg.isFriend() || !Verify.verifyIsGroupMember(msg)){
                    if(msg.isConfirm())
                        addGroupMember(ctx, msg);
                    else
                        sendGroupRequest(ctx,msg);
                }else{
                    channel.writeAndFlush(msg.setFriend(true).setConfirm(false));
                }
            }else{
                deleteGroupMsg(channel,msg);
            }
            return;
        }

        if(!msg.isAddOrDelete()){//判断是添加还是删除
//            log.debug("delete:"+ msg.getRequestPerson().getUid()+" "+ msg.getRecipientPerson().getUid());
            Delete.deleteFriend(msg);//删除好友信息
            String str = msg.getRequestPerson().getName()+"将您移除了好友列表。";
            StringMessage sm = new StringMessage(msg.getRequestPerson(), msg.getRecipientPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
            Storage.storageRequestMessage(sm, true, true);//发送通知
            RequestMessage rqm = new RequestMessage().setFriend(false).setNotice("移除成功");
            ChatServer.uidChannelMap.get(msg.getRequestPerson().getUid()).writeAndFlush(rqm);
        }else {
            if (msg.isClearMsg()) {
                ReviseMaterial.reviseRequest(msg);
            } else {
                Channel channel = ChatServer.uidChannelMap.get(msg.getRequestPerson().getUid());
                Channel channel1 = ChatServer.uidChannelMap.get(msg.getRecipientPerson().getUid());
                if (Verify.verifyIsFriend(msg)) {
                    channel.writeAndFlush(new RequestMessage().setFriend(true));
                } else {
                    //向对象发送确认消息
                    if (!msg.isConfirm()) {
                        if (!msg.isFriend()) {//拒绝
                            String str = msg.getRecipientPerson().getName() + "拒绝了你的好友请求";
                            StringMessage sm = new StringMessage(msg.getRecipientPerson(), msg.getRequestPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                            ReviseMaterial.reviseAddFriendMsg(msg);
                            Storage.storageRequestMessage(sm, true, true);//addFriend 表示添加好友过程结束
                            channel.writeAndFlush(new RequestMessage().setFriend(false));//阻塞用
                        } else {//isFriend以false开始保存添加信息到数据库
                            String str = msg.getRequestPerson().getName() + "发起了好友申请";
                            StringMessage sm = new StringMessage(msg.getRequestPerson(), msg.getRecipientPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                            Storage.storageRequestMessage(sm, false, true);
                        }
                    } else {//对方确认添加后添加消息到数据库
                        String str = msg.getRecipientPerson().getName() + "同意了你的好友请求";
                        StringMessage sm = new StringMessage(msg.getRecipientPerson(), msg.getRequestPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                        ReviseMaterial.reviseAddFriendMsg(msg);//修改状态为已读
                        Storage.storageRequestMessage(sm, true, true);
                        Storage.storageBuildFriends(msg);//建立联系

//                        sm.setMessage("我们已经是好友了！");
//                        Storage.storageSingleMessage(sm);
                        RequestMessage rqm = new RequestMessage().setFriend(false).setNotice("添加成功");
                        channel1.writeAndFlush(rqm);//阻塞用
                    }
                }
            }
        }
    }

    private static void addGroupMember(ChannelHandlerContext ctx, RequestMessage msg) throws SQLException {
        String groupName = LoadSystem.loadName(msg);

        Delete.deleteGroupRequestMsg(msg);

        String str = msg.getRequestPerson().getName()+"已进入"+groupName;
        Storage.storageRequestGroupNotice(msg,str,1);

        Storage.storageAddGroupMember(msg);

        Channel channel = ChatServer.uidChannelMap.get(msg.getRequestPerson().getUid());
        str = "你已进入"+groupName;
        channel.writeAndFlush(new ShowMessage().setUid(msg.getRequestPerson().getUid()).setName(msg.getRequestPerson().getName()).setStr(str).setRequest(false));
    }

    private static void sendGroupRequest(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        List<String> adm_uid = LoadSystem.loadAdm(msg);
        if(adm_uid.isEmpty()){
            throw new Exception(msg.getGid()+"数据错误");
        }
        String groupName = LoadSystem.loadName(msg);

        String str = msg.getRequestPerson().getName()+"申请加入"+groupName;
        Storage.storageRequestGroupNotice(msg,str,2);
        for (String s : adm_uid) {
            Channel channel = ChatServer.uidChannelMap.get(s);
            if(channel != null){
                channel.writeAndFlush(new ShowMessage().setUid(msg.getRequestPerson().getUid()).setName(msg.getRequestPerson().getName()).setStr(str).setRequest(true));
            }
        }

    }
    private static void deleteGroupMsg(Channel channel, RequestMessage msg){

    }
}
