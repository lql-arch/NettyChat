package Server.processLogin;

import NettyChat.DbUtil;
import message.ReviseMsgStatusMessage;
import message.ReviseMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviseMaterial {
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

        ps = con.prepareStatement("update members.user_text set status = false where status = true and time <= ? and send_uid = ? and recipient_uid = ?");
        ps.setObject(1,msg.getTime());
        ps.setObject(2,msg.getFriendUid());
        ps.setObject(3,msg.getMyUid());

        return !ps.execute();
    }

}
