package message;

public class ShowMessage extends Message{

    private String str;
    private String uid;
    private String name;
    private boolean Request;

    public boolean isRequest() {
        return Request;
    }

    public ShowMessage setRequest(boolean request) {
        Request = request;
        return this;
    }

    public String getStr() {
        return str;
    }

    public ShowMessage setStr(String str) {
        this.str = str;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public ShowMessage setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getName() {
        return name;
    }

    public ShowMessage setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int getMessageType() {
        return ShowMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
