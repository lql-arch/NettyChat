package Server.session;

import NettyChat.DbUtil;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Group implements GroupSession {
    private static final Logger log = LogManager.getLogger(Group.class);
    private int gid;
    private String name;
    private Map<Channel,Integer> member;//存uid

    public Group(String name){
        this.name = name;
        this.gid = addGid();
        member = new ConcurrentHashMap<>();
    }

    public Group addGroup(String name) throws SQLException {//未完成
        DbUtil db = DbUtil.getDb();
        Connection conn = db.getConn();
        if(conn.isClosed()){
            throw new SQLException();
        }

        PreparedStatement ps1 = conn.prepareStatement("use char_group");
        ps1.execute();

        //ps1 = conn.prepareStatement("select ");



        log.trace("聊天组已成功建立。");

        return new Group(name);
    }

    @Override
    public Group removeGroup(String name) throws SQLException {

        return null;
    }

    @Override
    public Group addGroupMember(String name, Integer member) throws SQLException {
        return null;
    }

    @Override
    public Channel removeGroupMember(String name, Integer member) throws SQLException {
        return null;
    }

    @Override
    public Map<Channel, Integer> getGroupMembers(String name) throws SQLException {
        return null;
    }

    public Group addMember(Channel channel, int uid){
        member.put(channel,uid);
        return this;
    }

    private int addGid(){
        int gid = 0;
        //算出gid

        return gid;
    }

    public int getGid() {
        return gid;
    }

    public String getName() {
        return name;
    }

    public Integer getMember(Channel channel){
        return member.get(channel);
    }


}

//        String config = name+"_config";
//        PreparedStatement ps3 = conn.prepareStatement("CREATE TABLE "+config+" (" +
//                "  `id` int NOT NULL AUTO_INCREMENT," +
//                "  `gid` int DEFAULT NULL," +
//                "  `last_msg_id` int DEFAULT NULL," +
//                "  PRIMARY KEY (`id`)" +
//                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
//        if (!ps3.execute()){
//            log.trace("组成员信息表已建立。");
//        }else{
//            log.warn("组成员信息表建立失败。");
//            throw new SQLException();
//        }
//PreparedStatement ps2 = conn.prepareStatement("create table groupMsg (" +
//        "`id` int NOT NULL AUTO_INCREMENT," +
//        "  `gid` int DEFAULT NULL," +
//        "  `content` text DEFAULT NULL," +
//        "  `create_time` datetime DEFAULT NULL," +
//        "  `update_time` datetime DEFAULT NULL," +
//        "  PRIMARY KEY (`id`)" +
//        ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
//            if (!ps2.execute()) {
//                    log.trace("组信息表已建立。");
//                    } else {
//                    log.warn("组信息表建立失败。");
//                    throw new SQLException();
//                    }
