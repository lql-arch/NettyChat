package message;

import java.sql.Date;
import java.util.List;

public class LoadMessage extends Message{

    private String uid;
    private String gid;
    private List<String> friends;
    private List<Chat_group> Group;
    private List<Chat_record> message;
    private Integer unread_message;
    private int age;
    private String name;
    private String gander;
    private Date build_time;
    private int status;//0:登录获取资料，1：好友聊天查询实时消息，2：群聊查询实时消息

    public LoadMessage(String id,int status){
        this.status = status;
        if(status == 0 || status == 1) {
            this.uid = id;
        }else if(status == 2){
            this.gid = id;
        }
    }
    @Override
    public int getMessageType() {
        return LoadMessage;
    }

    @Override
    public int getLength() {
        return 1;
    }

    public String getUid() {
        return uid;
    }
    public String getGid() {
        return gid;
    }

    public List<String> getFriends() {
        return friends;
    }

    public List<Chat_group> getGroup() {
        return Group;
    }

    public List<Chat_record> getMessage() {
        return message;
    }

    public Integer getUnread_message() {
        return unread_message;
    }

    public Date getBuild_time() {
        return build_time;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getStatus() {
        return status;
    }

    public String getGander() {
        return gander;
    }

    public void setGroup(List<Chat_group> group) {
        Group = group;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public void setMessage(List<Chat_record> message) {
        this.message = message;
    }

    public void setUnread_message(Integer unread_message) {
        this.unread_message = unread_message;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setBuild_time(Date build_time) {
        this.build_time = build_time;
    }

    public void setGander(String gander) {
        this.gander = gander;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

