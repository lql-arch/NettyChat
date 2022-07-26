package message;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {

    private int messageType;

    public abstract int getMessageType();

    public static Class<? extends Message> getMessageClass(int messageType) {
        return map.get(messageType);
    }

    public abstract int getLength();

    public static final int LoginMessage = 1;
    public static final int StringMessage = 2;
    public static final int LoginStringMessage = 3;
    public static final int IntegerMessage = 4;
    public static final  int LoadMessage = 5;
    public static final int UserMessage = 6;

    public static final Map<Integer,Class<? extends Message>> map = new HashMap<>();

    static {
        map.put(LoginMessage, LoginMessage.class);
        map.put(StringMessage,StringMessage.class);
        map.put(LoginStringMessage,LoginStringMessage.class);
        map.put(IntegerMessage,IntegerMessage.class);
        map.put(LoadMessage, LoadMessage.class);
        map.put(UserMessage, UserMessage.class);
    }

}
