package message;

import com.alibaba.fastjson2.annotation.JSONField;

public class RequestMessage extends Message{
    private boolean clearMsg;
    private UserMessage requestPerson;
    private UserMessage recipientPerson;
    private String notice;
    private boolean Friend;//判断是否已是好友
    private boolean Confirm;//双方确认添加好友
    private boolean AddOrDelete;
    //过程： true:add,false:delete

    public RequestMessage(){}
//    public RequestMessage(boolean isFriend){
//        this.isFriend = isFriend;
//        this.isConfirm = false;
//    }
//    public RequestMessage(UserMessage requestPerson,UserMessage recipientPerson,boolean isAddOrDelete){
//        this.recipientPerson = recipientPerson;
//        this.requestPerson = requestPerson;
//        this.isFriend = true;
//        this.isAddOrDelete = isAddOrDelete;
//    }

//    public RequestMessage(UserMessage requestPerson,UserMessage recipientPerson,boolean isConfirm,boolean isFriend){
//        this.recipientPerson = recipientPerson;
//        this.requestPerson = requestPerson;
//        this.isConfirm = isConfirm;
//        this.isFriend = isFriend;
//    }

    public String getNotice() {
        return notice;
    }

    public RequestMessage setNotice(String notice) {
        this.notice = notice;
        return this;
    }

    public boolean isAddOrDelete() {
        return AddOrDelete;
    }

    public RequestMessage setAddOrDelete(boolean addOrDelete) {
        AddOrDelete = addOrDelete;
        return this;
    }

    public RequestMessage setRequestPerson(message.UserMessage requestPerson) {
        this.requestPerson = requestPerson;
        return this;
    }

    public RequestMessage setRecipientPerson(message.UserMessage recipientPerson) {
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
