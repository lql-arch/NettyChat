package message;

import lombok.Data;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
public class FileRead extends FileMessage{

    private Map<String ,String> filePersonMap;
    private Map<String ,String> fileTimeMap;
    private boolean checkFile;

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

    public FileRead setCheckFile(boolean checkFile){
        this.checkFile = checkFile;
        return this;
    }

    public int getType(){
        return FileRead;
    }
}
