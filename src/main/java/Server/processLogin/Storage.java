package Server.processLogin;

import NettyChat.DbUtil;
import io.netty.channel.ChannelHandlerContext;
import message.RequestMessage;
import message.StringMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Storage {

    public static void storageSingleMessage(StringMessage msg) throws SQLException {//存储单聊消息到数据库
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;
        con.prepareStatement("use members");
        ps = con.prepareStatement("insert into members.user_text(recipient_uid, send_uid, time, status, text) values(?,?,?,?,?)");
        ps.setObject(1,msg.getFriend().getUid());
        ps.setObject(2,msg.getMe().getUid());
        ps.setObject(3,msg.getTime());
        ps.setObject(4,true);
        ps.setObject(5,msg.getMessage());

    }

    public static void storageBuildFriends(ChannelHandlerContext ctx, RequestMessage msg) throws SQLException {//建立好友关系并存储到数据库
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        PreparedStatement ps;

        ps = con.prepareStatement("use member");
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
}
