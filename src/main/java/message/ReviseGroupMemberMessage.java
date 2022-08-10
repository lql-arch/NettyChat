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

    public String getManageUid() {
        return manageUid;
    }

    public void setManageUid(String manageUid) {
        this.manageUid = manageUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGid() {
        return gid;
    }

    public ReviseGroupMemberMessage setGid(String gid) {
        this.gid = gid;
        return this;
    }

    public boolean isSetManage() {
        return setManage;
    }

    public void setSetManage(boolean setManage) {
        this.setManage = setManage;
    }

    public boolean isRemoveGroup() {
        return removeGroup;
    }

    public void setRemoveGroup(boolean removeGroup) {
        this.removeGroup = removeGroup;
    }

    public boolean isDisbandGroupChat() {
        return disbandGroupChat;
    }

    public void setDisbandGroupChat(boolean disbandGroupChat) {
        this.disbandGroupChat = disbandGroupChat;
    }

    @Override
    public int getMessageType() {
        return ReviseGroupMemberMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
