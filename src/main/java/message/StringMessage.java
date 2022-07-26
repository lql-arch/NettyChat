package message;

public class StringMessage extends Message {

    private static final int type = StringMessage;
    private String message;

    public StringMessage(String message) {
        this.message = message;
    }

    @Override
    public int getMessageType() {
        return StringMessage;
    }

    @Override
    public int getLength() {
        return message.length();
    }

    public String getMessage(){
        return message;
    }

}
