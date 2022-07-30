package message;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadMessage extends Message{

    private String uid;
    private String gid;
    private List<String> friends;
    private Map<String,String> uidNameMap;
    private Map<String,String> nameUidMap;
    private List<Chat_group> Group;
    private List<Chat_record> message;
    private Integer unread_message;
    private int age;
    private String name;
    private String gander;
//    private Date build_time;
    private String date;
    private int status;//0:登录获取资料，1：好友聊天查询实时消息，2：群聊查询实时消息,4:刷新资料
    private int hasRequest;//0:无消息，1：只有聊天消息，2：只有申请，3：有聊天和申请

    public LoadMessage(String id,int status){
        this.status = status;
        if(status == 0 || status == 1) {
            this.uid = id;
        }else if(status == 2){
            this.gid = id;
        }
        friends = new ArrayList<>();
        Group = new ArrayList<>();
        message = new ArrayList<>();
    }
    @Override
    public int getMessageType() {
        return LoadMessage;
    }

    @Override
    public int getLength() {
        return 1;
    }

    public Map<String, String> getNameUidMap() {
        return nameUidMap;
    }

    public void setNameUidMap(Map<String, String> nameUidMap) {
        this.nameUidMap = nameUidMap;
    }

    public int getHasRequest() {
        return hasRequest;
    }

    public void setHasRequest(int hasRequest) {
        this.hasRequest = hasRequest;
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

    public Date getTime() {
        return Date.valueOf(date);
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

    public Map<String, String> getUidNameMap() {
        return uidNameMap;
    }

    public void setUidNameMap(Map<String, String> uidNameMap) {
        this.uidNameMap = uidNameMap;
    }

    public String getDate() {
        return date;
    }

    public void addFriend(String friends){
        this.friends.add(friends);
    }

    public void setGroup(List<Chat_group> group) {
        Group = group;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setGid(String gid) {
        this.gid = gid;
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
    public void setDate(String date){//json
        this.date = date;
    }//json

    public void setTime(Date build_time) {
        this.date = build_time.toString();
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

