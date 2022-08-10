package message;

public class IdleMessage extends Message{

    private String str;

    public String getStr() {
        return str;
    }

    public IdleMessage setStr(String str) {
        this.str = str;
        return this;
    }

    @Override
    public int getMessageType() {
        return IdleMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
