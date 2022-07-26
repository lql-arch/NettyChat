package Server.session;

import io.netty.channel.Channel;

import java.sql.SQLException;
import java.util.Map;

public interface GroupSession {
    public Group addGroup(String name) throws SQLException;

    public Group removeGroup(String name) throws SQLException;

    public Group addGroupMember(String name,Integer member) throws SQLException;

    public Channel removeGroupMember(String name,Integer member) throws SQLException;

    public Map<Channel,Integer> getGroupMembers(String name) throws SQLException;

}
