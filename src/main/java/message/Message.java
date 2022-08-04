package message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Message implements Serializable {

    //private int messageType;

    public abstract int getMessageType();

    public static Class<? extends Message> getMessageClass(int messageType) {
        return map.get(messageType);
    }

    public abstract int getLength();

//    enum message{
//        LoginMessage,StringMessage,LoginStringMessage,LoadMessage
//        ,UserMessage,FindMessage,ReviseMessage
//    }

    public static final int LoginMessage = 1;
    public static final int StringMessage = 2;
    public static final int LoginStringMessage = 3;
    public static final int LoadMessage = 4;
    public static final int UserMessage = 5;
    public static final int FindMessage = 6;
    public static final int ReviseMessage = 7;
    public static final int ReviseMagStatusMessage = 8;
    public static final int RequestMessage = 9;
    public static final int FileMessage = 10;
    public static final int FileRead = 11;
    public static final int HistoricalNews = 12;
    public static final int LoadGroupMessage = 13;

    public static final Map<Integer,Class<? extends Message>> map = new HashMap<>();

    static {
        map.put(LoginMessage, LoginMessage.class);
        map.put(StringMessage,StringMessage.class);
        map.put(LoginStringMessage,LoginStringMessage.class);
        map.put(LoadMessage, LoadMessage.class);
        map.put(UserMessage, UserMessage.class);
        map.put(FindMessage, FindMessage.class);
        map.put(ReviseMessage, ReviseMessage.class);
        map.put(ReviseMagStatusMessage, ReviseMsgStatusMessage.class);
        map.put(RequestMessage, RequestMessage.class);
        map.put(FileMessage, FileMessage.class);
        map.put(FileRead,FileRead.class);
        map.put(HistoricalNews,HistoricalNews.class);
        map.put(LoadGroupMessage,LoadGroupMessage.class);
    }

}
