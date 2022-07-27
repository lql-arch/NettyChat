package message;

import java.sql.Timestamp;

public class ReviseMessage extends Message{
    private String uid ;
    private String name;
    private int age;
    private String gander;
    private String password;
    private boolean result;


    public ReviseMessage(String uid,boolean result){
        this.uid = uid;
        this.result = result;
    }
    public ReviseMessage(String uid,String name,String Password,String gander){
        this.uid = uid;
        this.password = Password;
        this.gander = gander;
        this.name = name;
        this.age = -1;
    }

    public ReviseMessage(String uid, int age){
        this.uid = uid;
        this.name = null;
        this.age = age;
        this.gander = null;
        this.password = null;
    }

    public String getPassword() {
        return password;
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

    public String getGander() {
        return gander;
    }

    @Override
    public int getMessageType() {
        return ReviseMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
