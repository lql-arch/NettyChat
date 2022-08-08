package message;

import client.normal.GroupChat_text;
import lombok.Data;

@Data
public class GroupStringMessage extends Message{
    private GroupChat_text text;
    private boolean reviseLastTime;
    private String gid;
    private String uid;
    private String time;

    public GroupStringMessage setText(GroupChat_text groupChat_text){
        this.text = groupChat_text;
        return this;
    }

    public GroupStringMessage setGid(String gid){
        this.gid = gid;
        return this;
    }

    public GroupStringMessage setUid(String uid){
        this.uid = uid;
        return this;
    }

    public GroupStringMessage setTime(String time){
        this.time = time;
        return this;
    }

    public GroupStringMessage setReviseLastTime(boolean reviseLastTime){
        this.reviseLastTime = reviseLastTime;
        return this;
    }

    public boolean isReviseLastTime() {
        return reviseLastTime;
    }

    public GroupChat_text getText() {
        return text;
    }

    @Override
    public int getMessageType() {
        return GroupStringMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }


}
