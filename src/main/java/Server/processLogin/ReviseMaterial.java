package Server.processLogin;

import Server.ChatServer;
import client.Start;
import client.System.ChatSystem;
import config.DbUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class ReviseMaterial {
    private static final Logger log = LogManager.getLogger();
    public static boolean revisePassword(ReviseMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps = con.prepareStatement("update members.user set password = ? where uid = ?");
        ps.setObject(1,msg.getPassword());
        ps.setObject(2,msg.getUid());
        boolean result = ps.execute();
        return !result;
    }

    public static boolean reviseName(ReviseMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps = con.prepareStatement("update members.user set name = ? where uid = ?");
        ps.setObject(1,msg.getName());
        ps.setObject(2,msg.getUid());

        boolean result = ps.execute();

        PreparedStatement pre= con.prepareStatement("update members.friends set first = ? where first_uid = ?");
        pre.setObject(1,msg.getName());
        pre.setObject(2,msg.getUid());
        pre.execute();

        PreparedStatement prepareStatement= con.prepareStatement("update members.friends set second = ? where second_uid = ?");
        prepareStatement.setObject(1,msg.getName());
        prepareStatement.setObject(2,msg.getUid());
        prepareStatement.execute();

        return !result;
    }

    public static boolean reviseAge(ReviseMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps = con.prepareStatement("update members.user set age = ? where uid = ?");
        ps.setObject(1,msg.getAge());
        ps.setObject(2,msg.getUid());
        boolean result = ps.execute();
        return !result;
    }

    public static boolean reviseGander(ReviseMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps = con.prepareStatement("update members.user set gander = ? where uid = ?");
        ps.setObject(1,msg.getGander());
        ps.setObject(2,msg.getUid());
        boolean result = ps.execute();
        return !result;
    }

    public static boolean reviseMessageStatus(ReviseMsgStatusMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();

        PreparedStatement ps = con.prepareStatement("use members");
        ps.execute();

        ps = con.prepareStatement("update members.user_text set status = false where status = true and time < ? and send_uid = ? and recipient_uid = ? and isAddFriend is null and addGroup is null");
        ps.setObject(1,msg.getTime());
        ps.setObject(2,msg.getFriendUid());
        ps.setObject(3,msg.getMyUid());
        log.debug("success");

        return !ps.execute();
    }


    public static boolean reviseAddFriendMsg(RequestMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();

        PreparedStatement ps = con.prepareStatement("use members");
        ps.execute();

        ps = con.prepareStatement("update members.user_text set status = false,isAddFriend = ? where isAddFriend = false  and status = true and send_uid = ? and recipient_uid = ?");
        ps.setObject(1,true);//addFriend 表示添加好友过程结束
        ps.setObject(2,msg.getRequestPerson().getUid());
        ps.setObject(3,msg.getRecipientPerson().getUid());

        return !ps.execute();
    }

    public static void reviseRequest(RequestMessage msg) throws SQLException {//清除掉某位接受者收到的通知
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("update members.user_text set status = false  where isAddFriend = true and recipient_uid = ?");
        ps.setObject(1,msg.getRecipientPerson().getUid());
        ps.execute();
    }

    public static boolean reviseBlack(ReviseMessage msg,boolean black) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("update members.friends set black = ? where first_uid = ? and second_uid = ?");
        ps.setObject(1,black);
        ps.setObject(2,msg.getUid());
        ps.setObject(3,msg.getFriend_uid());
        if(black)
            ChatServer.addBlack(msg.getUid(),msg.getFriend_uid());
        else
            ChatServer.removeBlack(msg.getUid(),msg.getFriend_uid());

        return !ps.execute();
    }

    public static void reviseOnline(String uid,boolean online) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("update members.user set online = ? where uid = ?");
        ps.setObject(1,online);
        ps.setObject(2,uid);

        ps.execute();
    }

    public static void SetGroupManage(ReviseGroupMemberMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        log.debug("adm:"+msg.getUid());
        if(msg.isRemoveAdm()) {
            ps = con.prepareStatement("update chat_group.group_user set administrator = false where uid = ? and gid = ?;");
        }else{
            ps = con.prepareStatement("update chat_group.group_user set administrator = true where uid = ? and gid = ?;");
        }
        ps.setObject(1, msg.getUid());
        ps.setObject(2, msg.getGid());

        ps.execute();
    }

    public static void reviseLastTime(GroupStringMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("update chat_group.group_user set last_msg_id = ? where gid = ? and uid = ?");
        ps.setObject(1, Timestamp.valueOf(msg.getTime()));
        ps.setObject(2,msg.getGid());
        ps.setObject(3,msg.getUid());
        ps.execute();
    }

    public static void reviseGroupBanned(GroupStringMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;
        ResultSet rs;
        boolean banned = true;

        if(msg.getUid() == null) {
            ps = con.prepareStatement("select banned from chat_group.group_user where gid = ? and group_master = false and administrator = false");
            ps.setObject(1,msg.getGid());
            rs = ps.executeQuery();
            while(rs.next()){
                banned = rs.getBoolean("banned") & banned ;
            }

            ps = con.prepareStatement("update chat_group.group_user set banned = ? where gid = ? ");
            ps.setObject(1,!banned);
            ps.setObject(2,msg.getGid());

        }else{
            ps = con.prepareStatement("select banned from chat_group.group_user where gid = ? and uid = ?");
            ps.setObject(1,msg.getGid());
            ps.setObject(2,msg.getUid());
            rs = ps.executeQuery();
            if(rs.next()){
                banned = rs.getBoolean("banned");
            }

            ps = con.prepareStatement("update chat_group.group_user set banned = ? where gid = ? and uid = ?");
            ps.setObject(1,!banned);
            ps.setObject(2,msg.getGid());
            ps.setObject(3,msg.getUid());
        }

        ps.execute();

    }

    public static void reviseGroupFileStatus(FileMessage msg) throws SQLException {
        Connection con = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("update chat_group.group_file set status = true where gid = ? and sender_uid = ? and file_path = ?");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getUid());
        ps.setObject(3,msg.getPath());
        ps.execute();
    }

}
