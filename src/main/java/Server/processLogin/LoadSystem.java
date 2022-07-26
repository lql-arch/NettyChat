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
    public static LoadMessage loadMessage(String uid) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        LoadMessage loadMessage = new LoadMessage();
        List<String> friends = new ArrayList<>();
        List<Chat_group> Group = new ArrayList<>();
        List<Chat_record> message = new ArrayList<>();

        Date time;
        String gender = null;
        int age = 0;
        String name;


        Integer unread_message = 0;

        PreparedStatement ps1 = conn.prepareStatement("use members");
        ps1.execute();

        //得到好友的uid
        PreparedStatement ps2 = conn.prepareStatement("select first_uid from friends where second_uid = uid");
        ResultSet rs = ps2.executeQuery();
        while(rs.next()){
            String friend = rs.getString("first");
            friends.add(friend);
        }
        ps2 = conn.prepareStatement("select second_uid from friends where first_uid = uid");
        rs = ps2.executeQuery();
        while(rs.next()){
            String friend = rs.getString("second");
            friends.add(friend);
        }

        //得到自己的资料
        ps2 = conn.prepareStatement("select name,age,gender,build_time form user where uid = uid");
        rs = ps2.executeQuery();
        while(rs.next()){
            name = rs.getString("name");
            age = rs.getInt("age");
            gender = rs.getString("gender");
            time = rs.getDate("build_time");
            loadMessage.setAge(age);
            loadMessage.setGender(gender);
            loadMessage.setName(name);
            loadMessage.setBuild_time(time);
        }

        ps2 = conn.prepareStatement("select text,send_uid,time,status from user_test where recipient_uid = uid ");
        rs = ps2.executeQuery();
        while (rs.next()){
            String send_uid = rs.getString("send_uid");
            String text = rs.getString("text");
            Timestamp datetime = rs.getTimestamp("time");
            boolean status = rs.getBoolean("status");
            if(status){
                unread_message++;
            }
            Chat_record chat = new Chat_record(uid,send_uid,datetime,text,status);
            message.add(chat);
        }


        //得到自己的群聊信息
        ps1 = conn.prepareStatement("use chat_group");
        ps1.execute();
        ps2 = conn.prepareStatement("select gid,last_msg_id from group_user where uid = uid");
        rs = ps2.executeQuery();
        while(rs.next()) {
            Chat_group group = new Chat_group();
            List<String> administrator = new ArrayList<>();

            String gid = rs.getString("gid");
            long last_msg_id = rs.getInt("last_msg_id");
            group.setLast_msg_id(last_msg_id);

            PreparedStatement ps3 = conn.prepareStatement("select group_name,creat_time from group where gid = gid");
            ResultSet rs1 = ps3.executeQuery();
            while (rs1.next()) {
                group.setGroupName(rs1.getString("group_name"));
                group.setDate(rs1.getDate("creat_time"));
            }

            ps3 = conn.prepareStatement("select uid,text from group_msg where gid = gid");
            rs1 = ps3.executeQuery();
            while (rs1.next()) {
                String rg_uid = rs1.getString("uid");
                String rg_test = rs1.getString("text");
                group.addMsg(group.setContent(rg_uid, rg_test));
            }

            ps3 = conn.prepareStatement("select administrator,group_master,uid from group_user where gid = gid");
            rs1 = ps3.executeQuery();
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
        if(!ps.execute()){
            throw new SQLException("not members");
        }
        ps = conn.prepareStatement("select name,age,build_time,gander from user where uid = uid");
        rs = ps.executeQuery();
        while(rs.next()){
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            user.setGander(rs.getString("gander"));
            user.setBuild_time(rs.getDate("build_time"));
        }

        return user;
    }

}