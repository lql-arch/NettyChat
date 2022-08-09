package message;

import lombok.Data;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
public class FileRead extends Message{

    private Map<String ,String> filePersonMap;
    private Map<String ,String> fileTimeMap;
    private String uid;
    private String gid;
    private boolean singleOrGroup;//trueOrFalse

    public FileRead addFilePersonMap(String file,String person){
        if(filePersonMap == null){
            filePersonMap = new HashMap<>();
        }
        filePersonMap.put(file,person);
        return this;
    }

    public FileRead addFileTimeMap(String file, Timestamp time){
        if(fileTimeMap == null){
            fileTimeMap = new HashMap<>();
        }
        fileTimeMap.put(file,time.toString());
        return this;
    }

    public FileRead setSingleOrGroup(boolean singleOrGroup){
        this.singleOrGroup = singleOrGroup;
        return this;
    }

    public FileRead setUid(String uid){
        this.uid = uid;
        return this;
    }

    public FileRead setGid(String gid){
        this.gid = gid;
        return this;
    }

    public int getType(){
        return FileRead;
    }

    @Override
    public int getMessageType() {
        return FileRead;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
