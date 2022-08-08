package client.normal;

import lombok.Data;

@Data
public class GroupChat_text {
    private String text;
    private String uid;//发送者
    private String date;
    private String gid;
    private String myName;

}
