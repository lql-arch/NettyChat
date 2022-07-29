package Server.processLogin;

import config.DbUtil;
import message.RequestMessage;
import message.StringMessage;

import java.sql.*;
import java.time.LocalDateTime;

public class Storage {

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

//    public static boolean storageAddAddFriend(ChannelHandlerContext ctx,RequestMessage msg) throws SQLException {
//        DbUtil db = DbUtil.getDb();
//        Connection con = db.getConn();
//        PreparedStatement ps;
//
//        ps = con.prepareStatement("use member");
//        ps.execute();
//
//        Date date = Date.valueOf(LocalDate.now());
//        String str = msg.getRequestPerson().getName()+"请求加你为好友。";
//
//        ps = con.prepareStatement("insert into members.user_text(recipient_uid, send_uid, time, status, text,isAddFriend,addGroup) values(?,?,?,?,?,false,null)");
//        ps.setObject(1,msg.getRecipientPerson().getUid());
//        ps.setObject(2,msg.getRequestPerson().getName());
//        ps.setObject(3,date);
//        ps.setObject(4,true);
//        ps.setObject(5,str);
//        return !ps.execute();
//
//    }

    public static  void storageRequestMessage(StringMessage msg,boolean addFriend,boolean status) throws SQLException {
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
}
