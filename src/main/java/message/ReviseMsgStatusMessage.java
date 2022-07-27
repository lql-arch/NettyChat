package message;

import java.sql.Timestamp;

public class ReviseMsgStatusMessage extends Message{

    private String myUid;
    private String friendUid;
    private Timestamp time;

    public ReviseMsgStatusMessage(String myUid, String friendUid, Timestamp time){
        this.friendUid = friendUid;
        this.myUid = myUid;
        this.time = time;
    }

    public Timestamp getTime() {
        return time;
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
