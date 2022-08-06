package Server.processLogin;

import Server.ChatServer;
import Server.SimpleChannelHandler.LoadGroupNewsHandler;
import config.DbUtil;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.Key;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static Server.ChatServer.addBlack;

public class LoadSystem{
    private static final Logger log = LogManager.getLogger(ChatServer.class);
    //读完后换标记(?)

    public static LoadMessage loadMessage(String uid,int ver) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        LoadMessage loadMessage = new LoadMessage(uid,ver);
        List<String> friends = new ArrayList<>();
        List<Chat_record> message = new ArrayList<>();
        Map<String,String> uidNameMap = new HashMap<>();
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

        ps2 = conn.prepareStatement("select text,send_uid,time,status from members.user_text where recipient_uid = ? and isAddFriend is null and addGroup is null order by id");
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
        ps2 = conn.prepareStatement("select text,send_uid,time,isAddFriend from members.user_text where recipient_uid = ? and isAddFriend is not null and status = true and addGroup is null order by id");
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


        loadMessage.setBlacklist(black);
        loadMessage.setHasRequest(hasRequest);
        loadMessage.setMessage(message);//单聊消息
        loadMessage.setFriends(friends);
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

        ps = conn.prepareStatement("select text,recipient_uid,send_uid,time,status from members.user_text where recipient_uid = ? and isAddFriend is null and addGroup is null order by id");
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

    public static FileRead loadFile(FileRead msg) throws SQLException {
        Connection con = DbUtil.loginMysql().getConn();
        FileRead fileRead = new FileRead();
        PreparedStatement ps;

        ps = con.prepareStatement("select sender_uid,file_name,time from members.store_file where recipient_uid = ?;");
        ps.setObject(1,msg.getUid());
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            fileRead.addFilePersonMap(rs.getString("file_name"),rs.getString("sender_uid"));
            fileRead.addFileTimeMap(rs.getString("file_name"),rs.getTimestamp("time"));
        }

        return fileRead;
    }

    public static void loadReadFile(FileMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

//        log.debug(msg.getName()+" "+msg.getMe().getUid()+" "+msg.getTime());
        ps = con.prepareStatement("select file_path from members.store_file where file_name = ? and sender_uid = ? and time = ?");
        ps.setObject(1,msg.getName());
        ps.setObject(2,msg.getMe().getUid());
        ps.setObject(3,Timestamp.valueOf(msg.getTime()));
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            msg.setPath(rs.getString("file_path"));
        }
    }

    public static void loadHistory(HistoricalNews msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;
        List<Chat_record> crs = new ArrayList<>();

        ps = con.prepareStatement("select time, text from members.user_text where isAddFriend is null and addGroup is null and file = false and recipient_uid = ? and send_uid = ? and time >= ? and time < ? order by id");
        ps.setObject(1,msg.getFriendUid());
        ps.setObject(2,msg.getUid());
        ps.setObject(3,Timestamp.valueOf(msg.getStartTime()));
        ps.setObject(4,Timestamp.valueOf(msg.getEndTime()));
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            String recipient_uid = msg.getFriendUid();
            String send_uid = msg.getUid();
            String time = rs.getTimestamp("time").toString();
            String text = rs.getString("text");
            Chat_record cr = new Chat_record();
            cr.setDate(time);
            cr.setText(text);
            cr.setSend_uid(send_uid);
            cr.setRecipient_uid(recipient_uid);
            crs.add(cr);
        }

        ps = con.prepareStatement("select time, text from members.user_text where isAddFriend is null and addGroup is null and file = false and recipient_uid = ? and send_uid = ? order by id");
        ps.setObject(1,msg.getUid());
        ps.setObject(2,msg.getFriendUid());
        rs = ps.executeQuery();
        while(rs.next()){
            String recipient_uid = msg.getUid();
            String send_uid = msg.getFriendUid();
            String time = rs.getTimestamp("time").toString();
            String text = rs.getString("text");
            Chat_record cr = new Chat_record();
            cr.setDate(time);
            cr.setText(text);
            cr.setSend_uid(send_uid);
            cr.setRecipient_uid(recipient_uid);
            crs.add(cr);
        }

        msg.setChat_record(crs);

    }

    public static LoadMessage GroupChat(String uid) throws SQLException {
        LoadMessage loadMessage = new LoadMessage(uid,2);
        Connection con = DbUtil.getDb().getConn();

        PreparedStatement ps;
        ResultSet rs;

        List<Chat_group> chat_groups = new ArrayList<>();
        int messages = 0;

        ps = con.prepareStatement("select gid,last_msg_id from chat_group.group_user where uid = ?");
        ps.setObject(1,uid);
        rs = ps.executeQuery();
        while(rs.next()){
            Chat_group group = new Chat_group();
            String gid = rs.getString("gid");
            Timestamp lastTime = rs.getTimestamp("last_msg_id");

            group.setLast_msg_time(lastTime.toString());
            group.setGid(gid);

            ps = con.prepareStatement("select group_name,create_time,members_num from chat_group.`group` where gid = ?");
            ps.setObject(1,gid);
            ResultSet rs1 = ps.executeQuery();
            if(rs1.next()) {
                group.setGroupName(rs1.getString("group_name"));
                group.setTime(rs1.getTimestamp("create_time"));
                group.setMembersNum(rs1.getInt("members_num"));
            }

            //获取群聊消息(unread限制没写)
            ps = con.prepareStatement("select uid,text,time from chat_group.group_msg where gid = ? and time > ?  order by id");
            ps.setObject(1,gid);
            ps.setObject(2,lastTime);
            rs1 = ps.executeQuery();
            if(rs1.next()) {//先读取时，读取是否有消息，进入群聊后在详细读取
                messages++;
                group.setMessage(1);//有消息
            }

            ps = con.prepareStatement("select administrator,group_master,uid from chat_group.group_user where gid = ?");
            ps.setObject(1,gid);
            rs1 = ps.executeQuery();
            while (rs1.next()){
                String rg_uid = rs1.getString("uid");
                if(rs1.getBoolean("administrator")){
                    group.addAdministrator(rg_uid);
                }else if(rs1.getBoolean("group_master")){
                    group.setGroup_master(rg_uid);
                }else {
                    group.addMembers(rg_uid);
                }
            }

            chat_groups.add(group);
        }

        loadMessage.setGroupMessage(messages);
        loadMessage.setGroup(chat_groups);

        return loadMessage;
    }

    public static LoadGroupMessage loadGroupMessages(LoadGroupMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();

        PreparedStatement ps;
        ResultSet rs;

        List<GroupChat_text> gcts = new ArrayList<>();
        Map<String, String> uidNameMap = new HashMap<>();

        ps = con.prepareStatement("select uid,text,time from chat_group.group_msg where gid = ? and time >= ? and isNotice = false order by id");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getLastTime());
        rs = ps.executeQuery();
        while(rs.next()){
            String rg_uid = rs.getString("uid");
            String rg_test = rs.getString("text");
            Timestamp ts = rs.getTimestamp("time");
            GroupChat_text gct = new GroupChat_text();
            gct.setTime(ts);
            gct.setUid(rg_uid);
            gct.setText(rg_test);
            gcts.add(gct);
        }

        ps = con.prepareStatement("select name from members.user where uid = ?;");
        ps.setObject(1,msg.getUid());
        rs = ps.executeQuery();
        if(rs.next()){
            msg.setMasterName(rs.getString("name"));
        }

        for (String t : msg.getAdministrator()) {
            ps = con.prepareStatement("select name from members.user where uid = ?;");
            ps.setObject(1, t);
            rs = ps.executeQuery();
            if(rs.next()){
                uidNameMap.put(t,rs.getString("name"));
            }
        }

        for (String t : msg.getMembers()) {
            ps = con.prepareStatement("select name from members.user where uid = ?;");
            ps.setObject(1, t);
            if(rs.next()){
                uidNameMap.put(t,rs.getString("name"));
            }
        }

        msg.setUidNameMap(uidNameMap);
        msg.setGroupMessages(gcts);
        return msg;
    }

    public static void loadGroup(FindGroupMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("select group_name,create_time,members_num from chat_group.`group` where gid = ? order by id ");
        ps.setObject(1,msg.getGid());
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            msg.setGroupName(rs.getString("group_name"));
            msg.setBuildTime(rs.getTimestamp("create_time").toString());
            msg.setMembersCount(rs.getInt("members_num"));
        }


        ps = con.prepareStatement("select uid from chat_group.group_user where gid = ? and group_master = true order by id ");
        ps.setObject(1,msg.getGid());
        rs = ps.executeQuery();
        if(rs.next()){
            msg.setMasterUid(rs.getString("uid"));
        }

        ps = con.prepareStatement("select name from members.user where uid = ?;");
        ps.setObject(1,msg.getMasterUid());
        rs = ps.executeQuery();
        if(rs.next()){
            msg.setGroupMaster(rs.getString("name"));
        }
    }

    public static void loadGroupNotice(GroupNoticeMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;
        ResultSet rs;
        Map<String,Integer> groups = new HashMap<>();

        ps = con.prepareStatement("select gid,administrator,group_master from chat_group.group_user where uid = ?;");
        ps.setObject(1,msg.getUid());
        rs = ps.executeQuery();
        while(rs.next()){
            boolean group_master = rs.getBoolean("group_master");
            boolean administrator = rs.getBoolean("administrator");
            String gid = rs.getString("gid");

            if(group_master){
                groups.put(gid,3);
            }else if(administrator){
                groups.put(gid,2);
            }else{
                groups.put(gid,0);
            }
        }

        for(Map.Entry<String, Integer> level : groups.entrySet()){
            ps = con.prepareStatement("select text,gid,time from chat_group.group_msg where gid = ? and isNotice = true and (level <= ? or (uid = ? and level = 1)) order by id ;");
            ps.setObject(1,level.getKey());
            ps.setObject(2,level.getValue());
            ps.setObject(3,msg.getUid());
            rs = ps.executeQuery();
            while (rs.next()){
                String gid = rs.getString("gid");

                GroupNoticeMessage.Notice notice = new GroupNoticeMessage.Notice();
                notice.setNotice(rs.getString("text"));
                notice.setGid(gid);
                notice.setTime(rs.getString("time"));

                msg.addNotice(notice);
            }
        }


    }
}