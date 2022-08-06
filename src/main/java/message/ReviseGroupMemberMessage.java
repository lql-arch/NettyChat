package message;

import lombok.Data;

@Data
public class ReviseGroupMemberMessage extends Message{

    private String manageUid;
    private String uid;
    private String gid;
    private boolean setManage;//设置uid为管理
    private boolean removeGroup;//将uid移除群聊
    private boolean disbandGroupChat;//解散群聊


    @Override
    public int getMessageType() {
        return ReviseGroupMemberMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
