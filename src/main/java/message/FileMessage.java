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
}
