package message;

import java.sql.Date;
import java.sql.Timestamp;

public class GroupChat_text {
    private String text;
    private String uid;//发送者
    private String date;

    public String getText() {
        return text;
    }

    public String getUid() {
        return uid;
    }

    public void setTime(Timestamp date) {
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

    public Timestamp getTime() {
        return Timestamp.valueOf(date);
    }
}
