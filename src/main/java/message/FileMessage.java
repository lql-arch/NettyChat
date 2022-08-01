package message;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.io.File;

@Data
public class FileMessage extends Message{

    private String name;
    private File file;
    private byte[] bytes;
    private int startPos;
    private int endPos;

    @Override
    public int getMessageType() {
        return FileMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
