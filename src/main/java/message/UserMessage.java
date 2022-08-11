package message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;

public class UserMessage extends Message{
    private static final Logger log = LogManager.getLogger();
    private String uid ;
    private String name;
    private int age;
//    private Date build_time;
    private String data;
    private String gander;
    private boolean status;

    public UserMessage(String uid){
        this.uid = uid;
    }

    public UserMessage(String uid,String name){
        this.uid = uid;
        this.name = name;
    }

    public UserMessage() {

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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
        if(data == null)
            return null;
        return Date.valueOf(data);
    }

    public String getGander() {
        return gander;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public UserMessage setName(String name) {
        this.name = name;
        return this;
    }

    public void setData(String date) {//为json反序列化准备
        this.data = date;
    }

    public void setBuild_time(Date build_time) {
        this.data = build_time.toString();
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
