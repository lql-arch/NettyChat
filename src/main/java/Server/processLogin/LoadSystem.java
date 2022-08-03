package Server.processLogin;

import config.DbUtil;
import Server.ChatServer;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Server.ChatServer.addBlack;

public class LoadSystem{
    private static final Logger log = LogManager.getLogger(ChatServer.class);
    //读完后换标记(?)

    public static LoadMessage loadMessage(String uid,int ver) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        LoadMessage loadMessage = new LoadMessage(uid,ver);
        List<String> friends = new ArrayList<>();
        List<Chat_group> Group = new ArrayList<>();
        List<Chat_record> message = new ArrayList<>();
        Map<String,String> uidNameMap = new HashMap<>();
//        Map<String,String> nameUidMap = new HashMap<>();
        Map<String, Boolean> black = new HashMap<>();
        int hasRequest = 0;//0:无消息，1：只有聊天消息，2：只有申请，3：有聊天和申请

        Date time = null;
        String gander;
        int age;
        String name;
        int unread_message = 0;

        PreparedStatement ps2;
        ResultSet rs;
        PreparedStatement ps1 = conn.prepareStatement("use members");
        ps1.execute();

        //得到好友的uid
        ps2 = conn.prepareStatement("select second,second_uid,black from members.friends where first_uid = ?");
        ps2.setObject(1,uid);
        rs = ps2.executeQuery();
        while(rs.next()){
            String friend_uid = rs.getString("second_uid");
            String friend = rs.getString("second");
            Boolean isBlack = rs.getBoolean("black");
            friends.add(friend_uid);
            uidNameMap.put(friend_uid,friend);
            black.put(friend_uid,isBlack);
            if(isBlack)
                addBlack(uid,friend_uid);
        }



        //得到自己的资料
        ps2 = conn.prepareStatement("select name,age,gander,build_time from members.user where uid = ?");
        ps2.setObject(1,uid);
        rs = ps2.executeQuery();
        while(rs.next()){
            name = rs.getString("name");
            age = rs.getInt("age");
            gander = rs.getString("gander");
            time = rs.getDate("build_time");
            loadMessage.setAge(age);
            loadMessage.setGander(gander);
            loadMessage.setName(name);
            loadMessage.setTime(time);
        }

        ps2 = conn.prepareStatement("select text,send_uid,time,status from members.user_text where recipient_uid = ? and isAddFriend is null and addGroup is null");
        ps2.setObject(1, uid);
        rs = ps2.executeQuery();
        while (rs.next()) {
            String send_uid = rs.getString("send_uid");
            String text = rs.getString("text");
            Timestamp datetime = rs.getTimestamp("time");
            boolean status = rs.getBoolean("status");
            if (status) {
                unread_message++;
            }
            Chat_record chat = new Chat_record(uid, send_uid, datetime, text, status);
            chat.setType(0);
            message.add(chat);
        }
        if(unread_message != 0){
            hasRequest++;
        }

        //得到申请消息
        ps2 = conn.prepareStatement("select text,send_uid,time,isAddFriend from members.user_text where recipient_uid = ? and isAddFriend is not null and status = true and addGroup is null");
        ps2.setObject(1, uid);
        rs = ps2.executeQuery();
        while (rs.next()){
            unread_message++;
            String send_uid = rs.getString("send_uid");
            String text = rs.getString("text");
            Timestamp datetime = rs.getTimestamp("time");
            boolean end = rs.getBoolean("isAddFriend");

            Chat_record chat_record = new Chat_record(uid,send_uid,datetime,text,true);
            if(!end)
                chat_record.setType(1);
            else
                chat_record.setType(2);
            hasRequest = (hasRequest == 0 || hasRequest == 2 ) ? 2 : 3 ;
           message.add(chat_record);
        }



        //得到自己的群聊信息
        Timestamp last_msg_time;
        ps1 = conn.prepareStatement("use chat_group");
        ps1.execute();

        ps2 = conn.prepareStatement("select gid,last_msg_id from chat_group.group_user where uid = ?");
        ps2.setObject(1,uid);
        rs = ps2.executeQuery();
        while(rs.next()) {
            Chat_group group = new Chat_group();

            String gid = rs.getString("gid");
            last_msg_time = rs.getTimestamp("last_msg_id");
            group.setLast_msg_time(last_msg_time.toString());
            group.setGid(gid);

            ps2 = conn.prepareStatement("select group_name,create_time from chat_group.`group` where gid = ?");
            ps2.setObject(1,gid);
            ResultSet rs1 = ps2.executeQuery();
            if(rs1.next()) {
                group.setGroupName(rs1.getString("group_name"));
                group.setTime(rs1.getDate("creat_time"));
            }

            //获取群聊消息(unread限制没写)
            ps2 = conn.prepareStatement("select uid,text,time from chat_group.group_msg where gid = ? and time > ? ");
            ps2.setObject(1,gid);
            ps2.setObject(2,last_msg_time);
            rs1 = ps2.executeQuery();
            while (rs1.next()) {
                String rg_uid = rs1.getString("uid");
                String rg_test = rs1.getString("text");
                Timestamp ts = rs1.getTimestamp("time");
                group.addMsg(group.setContent(rg_uid, rg_test,ts));
            }

            ps2 = conn.prepareStatement("select administrator,group_master,uid from chat_group.group_user where gid = ?");
            ps2.setObject(1,gid);
            rs1 = ps2.executeQuery();
            while (rs1.next()){
                String rg_uid = rs1.getString("uid");
                group.addMembers(rg_uid);
                 if(rs1.getBoolean("administrator")){
                    group.addAdministrator(uid);
                 }else if(rs1.getBoolean("group_master")){
                     group.setGroup_master(uid);
                 }else {
                     group.addMembers(uid);
                 }
            }

            Group.add(group);
        }


        loadMessage.setBlacklist(black);
//        loadMessage.setNameUidMap(nameUidMap);
        loadMessage.setHasRequest(hasRequest);
        loadMessage.setMessage(message);//单聊消息
        loadMessage.setFriends(friends);
        loadMessage.setGroup(Group);
        loadMessage.setUnread_message(unread_message);
        loadMessage.setUidNameMap(uidNameMap);
        return loadMessage;
    }

    public static UserMessage friendMaterial(String uid) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        UserMessage user = new UserMessage(uid);
        PreparedStatement ps;
        ResultSet rs;

        ps = conn.prepareStatement("use members");
        ps.execute();

        ps = conn.prepareStatement("select name,age,build_time,gander,online from members.user where uid = ?");
        ps.setObject(1,uid);
        rs = ps.executeQuery();
        while(rs.next()){
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            user.setGander(rs.getString("gander"));
            user.setBuild_time(rs.getDate("build_time"));
            user.setStatus(rs.getBoolean("online"));
        }

        return user;
    }

    public static LoadMessage SingleChat(String myUid) throws SQLException {
        //
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        PreparedStatement ps;
        int unread_message = 0;
        LoadMessage loadMessage = new LoadMessage(myUid,1);
        List<Chat_record> message = new ArrayList<>();

        ps = conn.prepareStatement("select text,recipient_uid,send_uid,time,status from members.user_text where recipient_uid = ? and isAddFriend is null and addGroup is null");
        ps.setObject(1, myUid);
        unread_message = getUnread_message(myUid, ps, unread_message, message);

        loadMessage.setMessage(message);
        loadMessage.setUnread_message(unread_message);

        return loadMessage;
    }

    private static int getUnread_message(String myUid, PreparedStatement ps, int unread_message, List<Chat_record> message) throws SQLException {
        ResultSet rs;
        rs = ps.executeQuery();
        while (rs.next()) {
            String send_uid = rs.getString("send_uid");
            String recipient_uid = rs.getString("recipient_uid");
            String text = rs.getString("text");
            Timestamp datetime = rs.getTimestamp("time");
            boolean status = rs.getBoolean("status");
            if (status) {
                unread_message++;
            }
            Chat_record chat = new Chat_record();
            chat.setUid(myUid);
            chat.setRecipient_uid(recipient_uid);
            chat.setSend_uid(send_uid);
            chat.setText(text);
            chat.setTime(datetime);
            chat.setStatus(status);
//            myUid, recipient_uid, send_uid, datetime.toString(), text, status

            message.add(chat);
        }
        return unread_message;
    }

    public static LoadMessage GroupChat(String gid){
        LoadMessage loadMessage = new LoadMessage(gid,2);


        return loadMessage;
    }

    public static FileRead loadFile(FileMessage msg) throws SQLException {
        Connection con = DbUtil.loginMysql().getConn();
        FileRead fileRead = new FileRead();
        PreparedStatement ps;

        ps = con.prepareStatement("select sender_uid,file_name,time from members.store_file where recipient_uid = ?;");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            fileRead.addFilePersonMap(rs.getString("file_name"),rs.getString("sender_uid"));
            fileRead.addFileTimeMap(rs.getString("file_name"),rs.getTimestamp("time"));
        }

        return fileRead;
    }

}