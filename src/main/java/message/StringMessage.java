package message;

import java.sql.Date;
import java.sql.Timestamp;

public class StringMessage extends Message {

    private static final int type = StringMessage;
    private String message;
    private UserMessage me;
    private UserMessage friend;
    private Timestamp date;
    public StringMessage(UserMessage me, UserMessage friend, String message, Timestamp date) {
        this.message = message;
        this.me = me;
        this.friend = friend;
        this.date = date;
    }

    public Timestamp getDate() {
        return date;
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
