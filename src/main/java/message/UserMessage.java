package message;

import java.util.Date;

public class UserMessage extends Message{
    private String uid ;
    private String name;
    private int age;
    private Date build_time;
    private String gander;

    public UserMessage(String uid){
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public Date getBuild_time() {
        return build_time;
    }

    public String getGander() {
        return gander;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBuild_time(Date build_time) {
        this.build_time = build_time;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGander(String gander) {
        this.gander = gander;
    }
    @Override
    public int getMessageType() {
        return UserMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
