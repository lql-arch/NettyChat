package message;

import java.sql.Date;
import java.util.List;

public class LoadMessage extends Message{

    private String uid;
    private List<String> friends;
    private List<Chat_group> Group;
    private List<Chat_record> message;
    private Integer unread_message;
    private int age;
    private String name;
    private String gender;
    private Date build_time;

    public LoadMessage(){}
    @Override
    public int getMessageType() {
        return LoadMessage;
    }

    @Override
    public int getLength() {
        return 1;
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

    public String getGender() {
        return gender;
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

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

}

