package Server.processLogin;

import config.DbUtil;
import io.netty.channel.ChannelHandlerContext;
import message.FileMessage;
import message.RequestMessage;
import message.StringMessage;
import message.UserMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

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

}
