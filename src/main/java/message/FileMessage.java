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
        msg.name = lgm.getName();
        msg.path = lgm.getPath();
        msg.fileLen = lgm.getFileLen();
        msg.person = lgm.isPerson();
        msg.time = lgm.getTime();
        //单人文件
        msg.user = lgm.getUser();
        msg.me = lgm.getMe();
        //群文件
        msg.gid = lgm.getGid();
        msg.uid = lgm.getUid();
        msg.myUid = lgm.getMyUid();
        msg.deleteFile = lgm.isDeleteFile();
        return msg;
    }

}
