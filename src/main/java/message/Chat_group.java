package message;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Chat_group { //用户的群资料
    private List<group_content> msg;
    private String groupName;
    private List<String> members;
    private Date date;
    private List<String> administrator;
    private String group_master;
    private long last_msg_id;


    public Chat_group() {
        msg = new ArrayList<>();
        members = new ArrayList<>();
        administrator = new ArrayList<>();
    }

    public group_content setContent(String text,String uid){
        return new group_content(text,uid);
    }

    public Chat_group(String groupName) {
        this.groupName = groupName;
    }

    public Date getDate() {
        return date;
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

    public List<group_content> getMsg() {
        return msg;
    }

    public long getLast_msg_id() {
        return last_msg_id;
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

    public void setMsg(List<group_content> msg) {
        this.msg = msg;
    }

    public void setLast_msg_id(long last_msg_id) {
        this.last_msg_id = last_msg_id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void addMsg(group_content msg){
        this.msg.add(msg);
    }

    public void addMembers(String uid){
        this.members.add(uid);
    }

    public void addAdministrator(String uid){
        this.administrator.add(uid);
    }
    static class group_content{
        private String text;
        private String uid;
        private Date date;
        public group_content(String text,String uid){
            this.text = text;
            this.uid = uid;
        }

        public String getText() {
            return text;
        }

        public String getUid() {
            return uid;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Date getDate() {
            return date;
        }
    }
}
