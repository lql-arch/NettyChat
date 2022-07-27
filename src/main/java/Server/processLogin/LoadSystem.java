package Server.processLogin;

import NettyChat.DbUtil;
import message.LoadMessage;
import message.Chat_group;
import message.Chat_record;
import message.UserMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoadSystem{
    //读完后换标记(?)

    public static LoadMessage loadMessage(String uid) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        LoadMessage loadMessage = new LoadMessage(uid,0);
        List<String> friends = new ArrayList<>();
        List<Chat_group> Group = new ArrayList<>();
        List<Chat_record> message = new ArrayList<>();


        Date time;
        String gander;
        int age;
        String name;
        Integer unread_message = 0;

        PreparedStatement ps2;
        ResultSet rs;
        PreparedStatement ps1 = conn.prepareStatement("use members");
        ps1.execute();

        //得到好友的uid
        ps2 = conn.prepareStatement("select second_uid from members.friends where first_uid = ?");
        ps2.setObject(1,uid);
        rs = ps2.executeQuery();
        while(rs.next()){
            String friend = rs.getString("second_uid");
            friends.add(friend);
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

        ps2 = conn.prepareStatement("select text,send_uid,time,status from members.user_text where recipient_uid = ? ");
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
            message.add(chat);
        }

        //得到自己的群聊信息
        long last_msg_id;
        ps1 = conn.prepareStatement("use chat_group");
        ps1.execute();
        ps2 = conn.prepareStatement("select gid,last_msg_id from group_user where uid = ?");
        ps2.setObject(1,uid);
        rs = ps2.executeQuery();
        while(rs.next()) {
            Chat_group group = new Chat_group();

            String gid = rs.getString("gid");
            last_msg_id = rs.getInt("last_msg_id");
            group.setLast_msg_id(last_msg_id);

            ps2 = conn.prepareStatement("select group_name,create_time from `group` where gid = ?");
            ps2.setObject(1,gid);
            ResultSet rs1 = ps2.executeQuery();
            while (rs1.next()) {
                group.setGroupName(rs1.getString("group_name"));
                group.setTime(rs1.getDate("creat_time"));
            }

            //获取群聊消息(unread限制没写)
            ps2 = conn.prepareStatement("select uid,text from group_msg where gid = ? and id > ? ");
            ps2.setObject(1,gid);
            ps2.setObject(2,last_msg_id);
            rs1 = ps2.executeQuery();
            while (rs1.next()) {
                String rg_uid = rs1.getString("uid");
                String rg_test = rs1.getString("text");
                group.addMsg(group.setContent(rg_uid, rg_test));
            }

            ps2 = conn.prepareStatement("select administrator,group_master,uid from group_user where gid = ?");
            ps2.setObject(1,gid);
            rs1 = ps2.executeQuery();
            while (rs1.next()) {
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


        loadMessage.setMessage(message);//单聊消息
        loadMessage.setFriends(friends);
        loadMessage.setGroup(Group);
        loadMessage.setUnread_message(unread_message);
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
//        if(!ps.execute()){
//            throw new SQLException("not members");
//        }
        ps = conn.prepareStatement("select name,age,build_time,gander from members.user where uid = ?");
        ps.setObject(1,uid);
        rs = ps.executeQuery();
        if(rs.next()){
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            user.setGander(rs.getString("gander"));
            user.setBuild_time(rs.getDate("build_time"));
        }

        return user;
    }

    public static LoadMessage SingleChat(String myUid,String friendUid) throws SQLException {
        //
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        PreparedStatement ps;
        int unread_message = 0;
        LoadMessage loadMessage = new LoadMessage(myUid,1);
        List<Chat_record> message = new ArrayList<>();

        ps = conn.prepareStatement("select text,recipient_uid,send_uid,time,status from members.user_text where recipient_uid = ? or send_uid = ?");
        ps.setObject(1, myUid);
        ps.setObject(2,friendUid);
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
            Chat_record chat = new Chat_record(myUid, recipient_uid, send_uid, datetime, text, status);
            message.add(chat);
        }
        return unread_message;
    }

    public static LoadMessage GroupChat(String gid){
        LoadMessage loadMessage = new LoadMessage(gid,2);


        return loadMessage;
    }

}