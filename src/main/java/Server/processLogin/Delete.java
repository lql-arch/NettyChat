package Server.processLogin;

import config.DbUtil;
import message.RequestMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Delete {
    public static void deleteFriend(RequestMessage msg) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("delete from members.friends where first_uid = ? and second_uid = ?");
        ps.setObject(1,msg.getRequestPerson().getUid());
        ps.setObject(2,msg.getRecipientPerson().getUid());

        ps.execute();

        ps = conn.prepareStatement("delete from members.friends where first_uid = ? and second_uid = ?");
        ps.setObject(1,msg.getRecipientPerson().getUid());
        ps.setObject(2,msg.getRequestPerson().getUid());

        ps.execute();
    }
}
