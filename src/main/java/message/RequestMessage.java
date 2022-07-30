package message;

public class RequestMessage extends Message{

    private UserMessage requestPerson;
    private UserMessage recipientPerson;
    private boolean isFriend;//判断是否已是好友
    private boolean isConfirm;//双方确认添加好友
    private boolean clearMsg = false;

    public RequestMessage(UserMessage recipientPerson){
        this.recipientPerson = recipientPerson;
    }
    public RequestMessage(boolean isFriend){
        this.isFriend = isFriend;
        this.isConfirm = false;
    }
    public RequestMessage(UserMessage requestPerson,UserMessage recipientPerson){
        this.recipientPerson = recipientPerson;
        this.requestPerson = requestPerson;
        this.isConfirm = false;
        this.isFriend = true;
    }

    public RequestMessage(UserMessage requestPerson,UserMessage recipientPerson,boolean isConfirm,boolean isFriend){
        this.recipientPerson = recipientPerson;
        this.requestPerson = requestPerson;
        this.isConfirm = isConfirm;
        this.isFriend = isFriend;
    }

    public void setRequestPerson(message.UserMessage requestPerson) {
        this.requestPerson = requestPerson;
    }

    public void setRecipientPerson(message.UserMessage recipientPerson) {
        this.recipientPerson = recipientPerson;
    }

    public boolean isClearMsg() {
        return clearMsg;
    }

    public void setClearMsg(boolean clearMsg) {
        this.clearMsg = clearMsg;
    }

    public RequestMessage SetClearMsg(boolean clearMsg) {
        this.clearMsg = clearMsg;
        return this;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public boolean isFriend() {
        return isFriend;
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
