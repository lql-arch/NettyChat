package Server.processLogin;

import NettyChat.DbUtil;
import message.FindMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Verify {
    public static boolean verifyPassword(FindMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        conn.prepareStatement("use members").execute();
        PreparedStatement ps = conn.prepareStatement("select password from user where uid = ? limit 1");
        ps.setObject(1,msg.getUid());
        ResultSet rs = ps.executeQuery();
        rs.next();
        if(rs.getString("password").equals(msg.getPassword())){
            return true;
        }else{
            return false;
        }
    }
}
