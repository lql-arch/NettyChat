package message;

import client.normal.GroupChat_text;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LoadGroupMessage extends Message{

    private List<GroupChat_text> groupMessages ;
    private String uid;//自己的uid
    private String gid;
    private String groupName;
    private List<String> administrator;
    private List<String> members;
    private String group_master;//uid
    private String masterName;
    private Map<String,String> uidNameMap;
    private String time;
    private int membersCount;
    private String LastTime;
    private boolean buildGroup = false;


    @Override
    public int getMessageType() {
        return LoadGroupMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
