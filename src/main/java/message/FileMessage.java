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
    private int startPos;
    private int endPos;
    private boolean person = true;
    private UserMessage user;
    private UserMessage me;
    private String time;
    private boolean readOrWrite;//true = read , false = write

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
