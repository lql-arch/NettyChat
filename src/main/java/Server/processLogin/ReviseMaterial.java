package Server.processLogin;

import config.DbUtil;
import message.RequestMessage;
import message.ReviseMsgStatusMessage;
import message.ReviseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        ps = con.prepareStatement("update members.user_text set status = false where status = true and time > ? and send_uid = ? and recipient_uid = ? and isAddFriend is null and addGroup is null");
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
}
