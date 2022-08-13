package message;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.io.File;

@Data
public class FileMessage extends Message{

    private String name;
    private String path;
    private long fileLen;
    private byte[] bytes;
    private long startPos;
    private int endPos;
    private boolean person = true;
    private String time;
    //单人文件
    private UserMessage user;
    private UserMessage me;
    //群文件
    private String gid;
    private String uid;
    private String myUid;
    private boolean deleteFile;

    private boolean readOrWrite;//true = read , false = write

    private String sha1sum;
    private boolean First;

    public boolean isFirst() {
        return First;
    }

    public FileMessage setFirst(boolean first) {
        First = first;
        return this;
    }

    public FileMessage setDeleteFile(boolean deleteFile){
        this.deleteFile = deleteFile;
        return this;
    }

    public FileMessage setReadOrWrite(boolean readOrWrite){
        this.readOrWrite = readOrWrite;
        return this;
    }

    @Override
    public int getMessageType() {
        return FileMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }


    public String getSha1sum() {
        return sha1sum;
    }

    public void setSha1sum(String sha1sum) {
        this.sha1sum = sha1sum;
    }

    public static FileMessage clone(FileMessage lgm){
        FileMessage msg = new FileMessage();
        msg.name = lgm.name;
        msg.path = lgm.path;
        msg.fileLen = lgm.fileLen;
        msg.person = lgm.person;
        msg.time = lgm.time;
        msg.startPos = lgm.startPos;
        msg.endPos = lgm.endPos;
        msg.bytes = null;
        //单人文件
        msg.user = lgm.user;
        msg.me = lgm.me;
        //群文件
        msg.gid = lgm.gid;
        msg.uid = lgm.uid;
        msg.myUid = lgm.myUid;
        msg.deleteFile = lgm.deleteFile;
        msg.readOrWrite = lgm.readOrWrite;
        msg.sha1sum = lgm.sha1sum;
        return msg;
    }

}
