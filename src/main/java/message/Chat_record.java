package message;

import java.sql.Timestamp;

public class Chat_record {
    private final String uid;
    private  String recipient_uid;
    private final String send_uid ;
    private final String text ;
    private final Timestamp time;
    private boolean status;

    public Chat_record(String uid, String send_uid, Timestamp time, String text, boolean status ){
        this.uid = uid;
        this.send_uid = send_uid;
        this.status = status;
        this.text = text;
        this.time = time;
    }

    public Chat_record(String uid,String recipient_uid, String send_uid, Timestamp time, String text, boolean status ){
        this.uid = uid;
        this.recipient_uid = recipient_uid;
        this.send_uid = send_uid;
        this.status = status;
        this.text = text;
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public String getRecipient_uid() {
        return recipient_uid;
    }

    public String getSend_uid() {
        return send_uid;
    }

    public Timestamp getTime() {
        return time;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
