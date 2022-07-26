package message;

//用以登录的字符串
public class LoginStringMessage extends Message{
    private static final int type = LoginStringMessage;
    private String message;

    public LoginStringMessage(String message) {
        this.message = message;
    }

    @Override
    public int getMessageType() {
        return LoginStringMessage;
    }

    @Override
    public int getLength() {
        return message.length();
    }

    public String getMessage(){
        return message;
    }
}
