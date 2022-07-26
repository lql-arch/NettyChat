package message;

public class IntegerMessage extends Message {
    private Integer integer;

    public IntegerMessage(int integer){
        this.integer = integer;
    }

    @Override
    public int getMessageType() {
        return IntegerMessage;
    }

    public int getMessage(){
        return integer;
    }

    @Override
    public int getLength() {
        return 1;
    }
}
