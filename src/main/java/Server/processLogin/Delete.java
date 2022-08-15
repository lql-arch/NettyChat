package Server.processLogin;

import Server.ChatServer;
import config.DbUtil;
import io.netty.channel.Channel;
import message.*;
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
        List<String> members = new ArrayList<>();
        List<String> deleteFile = new ArrayList<>();
        String groupName = "";

        PreparedStatement ps = conn.prepareStatement("select uid from chat_group.group_user where gid = ?;");
        ps.setObject(1,msg.getGid());
        ResultSet rs1 = ps.executeQuery();
        while(rs1.next()){
            members.add(rs1.getString("uid"));
        }

        PreparedStatement ps1 = conn.prepareStatement("select file_path from chat_group.group_file where gid = ?;");
        ps1.setObject(1,msg.getGid());
        ResultSet rs = ps1.executeQuery();
        while(rs.next()){
            //检查是否有其他地方使用相同文件
            String path = rs.getString("file_path");

            PreparedStatement ps2 = conn.prepareStatement("select file_name from chat_group.group_file where file_path = ? and gid != ?");
            ps2.setObject(1,path);
            ps2.setObject(2,msg.getGid());
            if(!ps2.executeQuery().next()){
                PreparedStatement pr2 = conn.prepareStatement("select file_name from members.store_file where file_path = ?");
                pr2.setObject(1,path);
                if(!pr2.executeQuery().next()){
                    deleteFile.add(path);
                }else{
                    continue;
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
            PreparedStatement ps3 = conn.prepareStatement("delete from chat_group.group_file where gid = ? and file_path = ?");
            ps3.setObject(1, msg.getGid());
            ps3.setObject(2,path);
            ps3.execute();
        }

//        PreparedStatement pr = conn.prepareStatement("delete from chat_group.group_user where gid = ?");
//        pr.setObject(1,msg.getGid());
//        pr.execute();

        PreparedStatement pr1 = conn.prepareStatement("delete from chat_group.group_msg where gid = ?");
        pr1.setObject(1,msg.getGid());
        pr1.execute();

        PreparedStatement ps5 = conn.prepareStatement("select group_name from chat_group.`group` where gid = ?");
        ps5.setObject(1,msg.getGid());
        ResultSet resultSet = ps5.executeQuery();
        while (resultSet.next()){
            groupName = resultSet.getString("group_name");
        }

        PreparedStatement ps4 = conn.prepareStatement("update chat_group.`group` set isRemoved = true where gid = ?");
        ps4.setObject(1,msg.getGid());
        ps4.execute();


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

        rs.last();

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

        rs.last();

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

    public static void deleteGroupMember(RequestMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;
        int members;

        ps = conn.prepareStatement("delete from chat_group.group_user where gid = ? and uid = ?");
        ps.setObject(1,msg.getGid());
        ps.setObject(2,msg.getRequestPerson().getUid());
        ps.execute();

        ps = conn.prepareStatement("select members_num from chat_group.`group` where gid = ?");
        ps.setObject(1,msg.getGid());
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            members = rs.getInt("members_num");
            members--;
        }else{
            members = 1;
        }


        ps = conn.prepareStatement("update chat_group.`group` set members_num = ? where gid = ?");
        ps.setObject(1,members);
        ps.setObject(2,msg.getGid());
        ps.execute();
    }

    public static void LogOut(LoginMessage msg) throws SQLException {
        Connection conn = DbUtil.getDb().getConn();
        PreparedStatement ps;

        //是群主就删除群
        ps = conn.prepareStatement("select gid,group_master from chat_group.group_user where uid = ?");
        ps.setObject(1,msg.getUid());
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            if(rs.getBoolean("group_master")) {
                String gid = rs.getString("gid");
                ReviseGroupMemberMessage rvm = new ReviseGroupMemberMessage().setGid(gid).setUid(msg.getUid());
                String name = Verify.verifyGroupName(gid);
                List<String> members = DeleteGroup(rvm);
                String notice = msg.getUid() + "注销了帐号,并解散了群聊" + name+"("+gid+")";
                Storage.storageGroupNotice(rvm,notice,0);
                for(String member : members){
                    Channel channel = ChatServer.uidChannelMap.get(member);
                    if(channel != null)
                        channel.writeAndFlush(new ShowMessage().setRequest(false).setStr(notice));
                }
            }
            else
                deleteGroupMember(new RequestMessage().setGid(rs.getString("gid")).setRequestPerson(new UserMessage(msg.getUid())));
        }

        ps = conn.prepareStatement("update members.user set isLogOut = true,name = '帐号已注销' where uid = ?");
        ps.setObject(1,msg.getUid());
        ps.execute();

        ReviseMaterial.reviseOnline(msg.getUid(),false);

    }
}
