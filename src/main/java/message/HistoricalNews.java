package message;

import client.normal.Chat_record;
import client.normal.GroupChat_text;
import lombok.Data;

import java.util.List;

@Data
public class HistoricalNews extends Message{

    private String startTime;
    private String endTime;
    private String uid;
    private String gid;
    private String friendUid;
    private List<Chat_record> chat_record;
    private List<GroupChat_text> groupChat_texts;
    private boolean personOrGroup;//true:person , false:group

    @Override
    public int getMessageType() {
        return HistoricalNews;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
