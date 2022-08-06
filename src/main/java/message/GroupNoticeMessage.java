package message;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupNoticeMessage extends Message{

    private List<Notice> notices;
    private String uid;
    public GroupNoticeMessage(){
        notices = new ArrayList<>();
    }

    public GroupNoticeMessage addNotice(Notice notice){
        this.notices.add(notice);
        return this;
    }

    @Data
     public static class Notice {
        private String notice;
        private String gid;
        private String time;
     }

    @Override
    public int getMessageType() {
        return GroupNoticeMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
