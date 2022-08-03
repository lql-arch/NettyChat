package message;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
public class StringMessage extends Message {

    private static final int type = StringMessage;
    private String message;
    private UserMessage me;

    private UserMessage friend;
    private String date;
    private boolean direct;
    public StringMessage(UserMessage me, UserMessage friend, String message, String date) {
        this.message = message;
        this.me = me;
        this.friend = friend;
        this.date = date;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public void setMe(message.UserMessage me) {
        this.me = me;
    }

    public void setFriend(message.UserMessage friend) {
        this.friend = friend;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(Timestamp date) {
        this.date = date.toString();
    }

    public Timestamp getTime() {
        return Timestamp.valueOf(date);
    }

    public message.UserMessage getFriend() {
        return friend;
    }

    public message.UserMessage getMe() {
        return me;
    }
    @Override
    public int getMessageType() {
        return StringMessage;
    }

    @Override
    public int getLength() {
        return message.length();
    }

    public String getMessage(){
        return message;
    }

}
