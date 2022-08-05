package message;

import lombok.Data;

@Data
public class FindGroupMessage extends Message{

    private String gid ;
    private String groupName;
    private String buildTime;
    private String groupMaster;
    private int membersCount;
    private String masterUid;

    @Override
    public int getMessageType() {
        return FindGroupMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
