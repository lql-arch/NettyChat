package client.normal;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
public class Chat_group { //用户的群资料
    private String gid;
    private String groupName;
    private List<String> members;
    private String date;
    private List<String> administrator;
    private String group_master;//uid
    private String last_msg_time;
    private int membersNum;
    private int message;//是否有消息,或者条数


    public Chat_group() {
        members = new ArrayList<>();
        administrator = new ArrayList<>();
    }

    public Chat_group(String groupName) {
        this.groupName = groupName;
    }

    public Timestamp getTime() {
        return Timestamp.valueOf(date);
    }

    public List<String> getAdministrator() {
        return administrator;
    }

    public String getGroup_master() {
        return group_master;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getMembers() {
        return members;
    }

    public String getLast_msg_time() {
        return last_msg_time;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setGroup_master(String group_master) {
        this.group_master = group_master;
    }

    public void setAdministrator(List<String> administrator) {
        this.administrator = administrator;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void setLast_msg_time(String last_msg_time) {
        this.last_msg_time = last_msg_time;
    }

    public void setTime(Timestamp date) {
        this.date = date.toString();
    }

    public void addMembers(String uid){
        this.members.add(uid);
    }

    public void addAdministrator(String uid){
        this.administrator.add(uid);
    }
}
