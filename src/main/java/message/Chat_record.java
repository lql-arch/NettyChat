package message;

import java.sql.Timestamp;

public class Chat_record {
    public String uid;
    public String send_uid ;
    public String text ;
    public Timestamp time ;
    public boolean status;

    public Chat_record(String uid, String send_uid, Timestamp time, String text, boolean status ){
        this.uid = uid;
        this.send_uid = send_uid;
        this.status = status;
        this.text = text;
        this.status = status;
    }
}
