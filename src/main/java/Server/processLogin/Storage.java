package Server.processLogin;

import config.DbUtil;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class Storage {
    private static final Logger log = LogManager.getLogger();

    public static void storageSingleMessage(StringMessage msg) throws SQLException {//存储单聊消息到数据库
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;
        con.prepareStatement("use members");
        ps = con.prepareStatement("insert into members.user_text(recipient_uid, send_uid, time, status, text,isAddFriend,addGroup) values(?,?,?,?,?,null,null)");
        ps.setObject(1,msg.getFriend().getUid());
        ps.setObject(2,msg.getMe().getUid());
        ps.setObject(3,msg.getTime());
        ps.setObject(4,true);
        ps.setObject(5,msg.getMessage());
        ps.execute();

    }

    public static void storageBuildFriends(RequestMessage msg) throws SQLException {//建立好友关系并存储到数据库
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("use members");
        ps.execute();

        ps = con.prepareStatement("insert into members.friends(first, second, first_uid, second_uid) values(?,?,?,?)");
        ps.setObject(1,msg.getRequestPerson().getName());
        ps.setObject(2,msg.getRecipientPerson().getName());
        ps.setObject(3,msg.getRequestPerson().getUid());
        ps.setObject(4,msg.getRecipientPerson().getUid());
        ps.execute();

        ps = con.prepareStatement("insert into members.friends(second,first,second_uid,first_uid) values(?,?,?,?)");
        ps.setObject(1,msg.getRequestPerson().getName());
        ps.setObject(2,msg.getRecipientPerson().getName());
        ps.setObject(3,msg.getRequestPerson().getUid());
        ps.setObject(4,msg.getRecipientPerson().getUid());
        ps.execute();
        //开摆
    }

    public static  void storageRequestMessage(StringMessage msg,boolean addFriend,boolean status) throws SQLException {//addFriend 表示添加好友过程结束
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;

        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());

        con.prepareStatement("use members");
        ps = con.prepareStatement("insert into members.user_text(recipient_uid, send_uid, time, status, text,isAddFriend,addGroup) values(?,?,?,?,?,?,null)");
        ps.setObject(1,msg.getFriend().getUid());
        ps.setObject(2,msg.getMe().getUid());
        ps.setObject(3,ts);
        ps.setObject(4,status);
        ps.setObject(5,msg.getMessage());
        ps.setObject(6,addFriend);

        ps.execute();
    }

    public static void storageFiles(String name,String path,FileMessage msg,boolean status) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;

        UserMessage me = msg.getMe();
        UserMessage user = msg.getUser();

        if(!status) {
            ps = con.prepareStatement("insert into members.store_file(sender_uid,recipient_uid,file_name,file_path,status,time) values(?,?,?,?,?,?)");
            ps.setObject(1, me.getUid());
            ps.setObject(2, user.getUid());
            ps.setObject(3,name);
            ps.setObject(4, path);
            ps.setObject(5, false);
            ps.setObject(6,Timestamp.valueOf(LocalDateTime.now()));
            ps.execute();
        }else {
            ps = con.prepareStatement("update members.store_file set status = true where file_path = ? and sender_uid = ? and file_name = ?");
            ps.setObject(1,path);
            ps.setObject(2,me.getUid());
            ps.setObject(3,name);
        }

    }

    public static void storageFileMsg(StringMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("insert into members.user_text(recipient_uid, send_uid, time, status, text, isAddFriend, addGroup, file) values(?,?,?,?,?,null,null,true)");
        ps.setObject(1,msg.getFriend().getUid());
        ps.setObject(2,msg.getMe().getUid());
        ps.setObject(3,msg.getTime());
        ps.setObject(4,true);
        ps.setObject(5,msg.getMessage());
        ps.execute();
    }

    public static void buildGroup(LoadGroupMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        String gid = getGid(con);
        Timestamp time = Timestamp.valueOf(LocalDateTime.now());
        msg.setGid(gid);
        msg.setTime(time.toString());

        ps = con.prepareStatement("insert into chat_group.`group` (gid, group_name, create_time, update_time,members_num) values (?,?,?,?,?);");
        ps.setObject(1,gid);
        ps.setObject(2,msg.getGroupName());
        ps.setObject(3,time);
        ps.setObject(4,time);
        ps.setObject(5,1);
        ps.execute();

        ps = con.prepareStatement("insert into chat_group.group_user(gid, uid, group_master, last_msg_id, administrator) values (?,?,true,?,false)");
        ps.setObject(1,gid);
        ps.setObject(2,msg.getGroup_master());
        ps.setObject(3,Timestamp.valueOf(LocalDateTime.now()));
        ps.execute();
    }

    public static String getGid(Connection con) throws SQLException {
        String gid;
        PreparedStatement ps = con.prepareStatement("select gid from chat_group.`group` order by gid desc limit 1");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            gid = String.valueOf((Integer.parseInt(rs.getString("gid")) + 1));
        } else {
            gid = "10000000";
        }

        return gid;
    }

    public static void storageGroupNotice(ReviseGroupMemberMessage msg,String text,int level) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        Timestamp time = Timestamp.valueOf(LocalDateTime.now());

        ps = con.prepareStatement("insert into chat_group.group_msg (uid, text, gid, time,isNotice,level) values (?,?,?,?,true,?);");
        ps.setObject(1,msg.getUid());
        ps.setObject(2,text);
        ps.setObject(3,msg.getGid());
        ps.setObject(4,time);
        ps.setObject(5,level);
        ps.execute();

    }

    public static void storageGroupString(GroupStringMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("insert into chat_group.group_msg(uid, text, gid, time, isNotice, level) values (?,?,?,?,false,0)");
        ps.setObject(1,msg.getText().getUid());
        ps.setObject(2,msg.getText().getText());
        ps.setObject(3,msg.getText().getGid());
        ps.setObject(4,Timestamp.valueOf(msg.getText().getDate()));
        ps.execute();

    }

    public static void storageGroupFiles(String path,FileMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("insert into chat_group.group_file (gid, file_name, file_path, sender_uid, time) values (?,?,?,?,?);");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getName());
        ps.setObject(3,path);
        ps.setObject(4,msg.getMyUid());
        ps.setObject(5,Timestamp.valueOf(msg.getTime()));
        ps.execute();
    }

    public static void storageRequestGroupNotice(RequestMessage msg,String str,int type,boolean request) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("insert into chat_group.group_msg(uid, text, gid, time, isNotice, level,isRequest) values(?,?,?,?,?,?,?)");
        ps.setObject(1,msg.getRequestPerson().getUid());
        ps.setObject(2,str);
        ps.setObject(3,msg.getGid());
        ps.setObject(4,Timestamp.valueOf(LocalDateTime.now()));
        ps.setObject(5,true);
        ps.setObject(6,type);
        ps.setObject(7,request);

        ps.execute();
    }

    public static void storageAddGroupMember(RequestMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;
        int members = 1;

        ps = conn.prepareStatement("insert into chat_group.group_user(gid, uid, group_master, last_msg_id, administrator, banned) values (?,?,false,?,false,false);");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getRequestPerson().getUid());
        ps.setObject(3,Timestamp.valueOf(LocalDateTime.now()));
        ps.execute();

        ps = conn.prepareStatement("select members_num from chat_group.`group` where gid = ?");
        ps.setObject(1,msg.getGid());
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            members = rs.getInt("members_num");
        }
        members++;

        ps = conn.prepareStatement("update chat_group.`group` set members_num = ? where gid = ?");
        ps.setObject(1,members);
        ps.setObject(2,msg.getGid());
        ps.execute();

    }
}
