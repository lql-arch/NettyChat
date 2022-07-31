package message;

import java.sql.Timestamp;

public class Chat_record {
    private  String uid;
    private  String recipient_uid;
    private  String send_uid ;
    private  String text ;
    private  String date;
    private boolean status;
    private int type = 0;//0:聊天记录，1：申请好友记录，2：好友通知
    public Chat_record(){}

    public Chat_record(String uid, String send_uid, Timestamp time, String text, boolean status ){
        this.uid = uid;
        this.send_uid = send_uid;
        this.status = status;
        this.text = text;
        this.date = time.toString();
    }

    public String getDate() {
        return date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setRecipient_uid(String recipient_uid) {
        this.recipient_uid = recipient_uid;
    }

    public void setSend_uid(String send_uid) {
        this.send_uid = send_uid;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(Timestamp date) {
        this.date = date.toString();
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
        return Timestamp.valueOf(date);
    }

    public void setDate(String time) {
        this.date = time;
    }//json

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
