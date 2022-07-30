package message;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class ReviseMsgStatusMessage extends Message{

    private String myUid;
    private String friendUid;
    private String date;

    public ReviseMsgStatusMessage(String myUid, String friendUid, String time){
        this.friendUid = friendUid;
        this.myUid = myUid;
        this.date = time;
    }

//    public ReviseMsgStatusMessage(String myUid, String friendUid, Timestamp time){
//        this.friendUid = friendUid;
//        this.myUid = myUid;
//        this.date = time.toString();
//    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }

    public void setTime(@NotNull Timestamp date) {
        this.date = date.toString();
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Timestamp getTime() {
        return Timestamp.valueOf(date);
    }

    public String getMyUid() {
        return myUid;
    }

    public String getFriendUid() {
        return friendUid;
    }

    @Override
    public int getMessageType() {
        return ReviseMagStatusMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
