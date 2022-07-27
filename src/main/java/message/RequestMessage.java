package message;

public class RequestMessage extends Message{

    private UserMessage requestPerson;
    private UserMessage recipientPerson;
    private boolean isFriend;//判断是否已是好友
    private boolean isConfirm;//双方确认添加好友

    public RequestMessage(boolean isFriend){
        this.isFriend = isFriend;
        this.isConfirm = false;
    }
    public RequestMessage(UserMessage requestPerson,UserMessage recipientPerson){
        this.recipientPerson = recipientPerson;
        this.requestPerson = requestPerson;
        this.isConfirm = false;
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
