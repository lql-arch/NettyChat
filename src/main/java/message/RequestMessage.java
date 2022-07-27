package message;

public class RequestMessage extends Message{

    private final UserMessage requestPerson;
    private final UserMessage recipientPerson;

    public RequestMessage(UserMessage requestPerson,UserMessage recipientPerson){
        this.recipientPerson = recipientPerson;
        this.requestPerson = requestPerson;
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
