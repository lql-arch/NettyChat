package message;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Chat_group { //用户的群资料
    private String gid;
    private List<group_content> msg;
    private String groupName;
    private List<String> members;
    private String date;
    private List<String> administrator;
    private String group_master;
    private String last_msg_time;


    public Chat_group() {
        msg = new ArrayList<>();
        members = new ArrayList<>();
        administrator = new ArrayList<>();
    }

    public group_content setContent(String text, String uid, Timestamp ts){
        return new group_content(text,uid,ts);
    }

    public Chat_group(String groupName) {
        this.groupName = groupName;
    }

    public Date getTime() {
        return Date.valueOf(date);
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

    public Timestamp getLast_msg_time() {
        return Timestamp.valueOf(last_msg_time);
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

    public void setMsg(List<group_content> msg) {
        this.msg = msg;
    }

    public void setLast_msg_time(String last_msg_time) {
        this.last_msg_time = last_msg_time;
    }

    public void setTime(Date date) {
        this.date = date.toString();
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

    public class group_content{
        private String text;
        private String uid;//发送者
        private String date;
        public group_content(String text,String uid,Timestamp date){
            this.text = text;
            this.uid = uid;
            this.date = date.toString();
        }

        public String getText() {
            return text;
        }

        public String getUid() {
            return uid;
        }

        public void setTime(Date date) {
            this.date = date.toString();
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Date getTime() {
            return Date.valueOf(date);
        }
    }
}
