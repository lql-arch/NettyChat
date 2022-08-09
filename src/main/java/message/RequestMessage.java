package message;

public class RequestMessage extends Message{
    private boolean clearMsg;
    private UserMessage requestPerson;
    private UserMessage recipientPerson;
    private String notice;
    private boolean Friend;//判断是否已是好友/是否已入群
    private boolean Confirm;//双方确认添加好友
    private boolean AddOrDelete;
    //过程： true:add,false:delete

    private String gid;
    private boolean groupORSingle;

    public RequestMessage(){}

    public String getNotice() {
        return notice;
    }

    public RequestMessage setNotice(String notice) {
        this.notice = notice;
        return this;
    }

    public RequestMessage setGid(String gid){
        this.gid = gid;
        return this;
    }

    public String getGid() {
        return gid;
    }

    public boolean isGroupORSingle() {
        return groupORSingle;
    }

    public RequestMessage setGroupORSingle(boolean groupORSingle) {
        this.groupORSingle = groupORSingle;
        return this;
    }

    public boolean isAddOrDelete() {
        return AddOrDelete;
    }

    public RequestMessage setAddOrDelete(boolean addOrDelete) {
        AddOrDelete = addOrDelete;
        return this;
    }

    public RequestMessage setRequestPerson(UserMessage requestPerson) {
        this.requestPerson = requestPerson;
        return this;
    }

    public RequestMessage setRecipientPerson(UserMessage recipientPerson) {
        this.recipientPerson = recipientPerson;
        return this;
    }

    public boolean isClearMsg() {
        return clearMsg;
    }

    public RequestMessage setClearMsg(boolean clearMsg) {
        this.clearMsg = clearMsg;
        return this;
    }

    public RequestMessage setConfirm(boolean confirm) {
        Confirm = confirm;
        return this;
    }

    public RequestMessage setFriend(boolean friend) {
        Friend = friend;
        return this;
    }

    public boolean isConfirm() {
        return Confirm;
    }

    public boolean isFriend() {
        return Friend;
    }

    public message.UserMessage getRecipientPerson() {
        return recipientPerson;
    }

    public message.UserMessage getRequestPerson() {
        return requestPerson;
    }

    @Override
    public int getMessageType() {
        return RequestMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
