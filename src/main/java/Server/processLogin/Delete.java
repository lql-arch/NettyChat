package Server.processLogin;

import config.DbUtil;
import message.FileMessage;
import message.RequestMessage;
import message.ReviseGroupMemberMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Delete {
    private static final Logger log = LogManager.getLogger(Delete.class);
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

    public static void DeleteGroupMember(ReviseGroupMemberMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("delete from chat_group.group_user where uid = ? and gid = ?;");
        ps.setObject(1,msg.getUid());
        ps.setObject(2,msg.getGid());

        ps.execute();

    }

    public static List<String> DeleteGroup(ReviseGroupMemberMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;
        ResultSet rs;
        List<String> members = new ArrayList<>();
        List<String> deleteFile = new ArrayList<>();

        ps = conn.prepareStatement("select uid from chat_group.group_user where gid = ?;");
        ps.setObject(1,msg.getGid());
        rs = ps.executeQuery();
        while(rs.next()){
            members.add(rs.getString("uid"));
        }

        ps = conn.prepareStatement("delete from chat_group.group_user where gid = ?");
        ps.setObject(1,msg.getGid());
        ps.execute();

        ps = conn.prepareStatement("delete from  chat_group.group_msg where gid = ?");
        ps.setObject(1,msg.getGid());
        ps.execute();

        ps = conn.prepareStatement("select file_path from chat_group.group_file where gid = ?;");
        ps.setObject(1,msg.getGid());
        rs = ps.executeQuery();
        while(rs.next()){
            //检查是否有其他地方使用相同文件
            String path = rs.getString("file_path");

            PreparedStatement ps1 = conn.prepareStatement("select file_name from chat_group.group_file where file_path = ? and gid != ?");
            ps1.setObject(1,path);
            ps1.setObject(2,msg.getGid());
            if(!ps.executeQuery().next()){
                ps1 = conn.prepareStatement("select file_name from members.store_file where file_path = ?");
                ps1.setObject(1,path);
                if(ps.executeQuery().next()){
                   continue;
                }else{
                    deleteFile.add(path);
                }
            }else {
                continue;
            }

            try{
                File file = new File(path);
                if(!file.exists()){
                    log.warn(path + "文件不存在!");
                }
                if(file.delete()){
                    log.info(file.getName() + " 文件已被删除！");
                }else{
                    log.warn(path + "文件删除失败！");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        for (String path : deleteFile) {
            ps = conn.prepareStatement("delete from chat_group.group_file where gid = ? and file_path = ?");
            ps.setObject(1, msg.getGid());
            ps.setObject(2,path);
            ps.execute();
        }

        ps = conn.prepareStatement("update chat_group.`group` set isRemoved = true where gid = ?");
        ps.setObject(1,msg.getGid());
        ps.execute();


        return members;
    }

    public static void DeleteGroupFile(FileMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;
        ResultSet rs;
        String deleteFile;
        boolean delete = true;

        ps = conn.prepareStatement("select file_path from chat_group.group_file where gid = ? and sender_uid = ? and file_name = ? and time = ?;");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getUid());
        ps.setObject(3,msg.getName());
        ps.setObject(4,Timestamp.valueOf(msg.getTime()));
        rs = ps.executeQuery();
        if(rs.next()) {
            deleteFile = rs.getString("file_path");
            msg.setPath(msg.getName());
        }else{
            msg.setPath(null);//表示没有此文件
            return;
        }

        //检查是否有其他地方使用相同文件
        ps = conn.prepareStatement("select file_name from chat_group.group_file where file_path = ? and gid != ?");
        ps.setObject(1, deleteFile);
        ps.setObject(2, msg.getGid());
        if (!ps.executeQuery().next()) {
            ps = conn.prepareStatement("select file_name from members.store_file where file_path = ?");
            ps.setObject(1, deleteFile);
            if (ps.executeQuery().next()) {
                delete = false;
                //说明有别处使用此文件，删除本群储存记录
            }
        } else {
            delete = false;
            //说明有别处使用此文件，删除本群储存记录
        }

        if(delete) {
            try {
                File file = new File(deleteFile);
                if (!file.exists()) {
                    log.warn(deleteFile + "文件不存在!");
                }
                if (file.delete()) {
                    log.info(file.getName() + " 文件已被删除！");
                } else {
                    log.warn(deleteFile + "文件删除失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        ps = conn.prepareStatement("delete from chat_group.group_file where gid = ? and file_path = ? and sender_uid = ? and time = ?;");
        ps.setObject(1, msg.getGid());
        ps.setObject(2,deleteFile);
        ps.setObject(3,msg.getUid());
        ps.setObject(4,Timestamp.valueOf(msg.getTime()));
        ps.execute();
    }

    public static void deleteGroupRequestMsg(RequestMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;

        ps = conn.prepareStatement("delete from chat_group.group_msg where gid = ? and uid = ? and isRequest = true and level = 2");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getRequestPerson().getUid());
        ps.execute();

    }
}
