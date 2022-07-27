package message;

public class FindMessage extends Message{//查询密码正确性
    private final String uid;
    private final String password;
    private boolean result;

    public FindMessage(String uid,String password){
        this.uid = uid;
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }
    public boolean getResult(){
        return result;
    }

    public void setResult(boolean result){
        this.result = result;
    }

    @Override
    public int getMessageType() {
        return FindMessage;
    }

    @Override
    public int getLength() {
        return uid.length()+password.length();
    }
}
