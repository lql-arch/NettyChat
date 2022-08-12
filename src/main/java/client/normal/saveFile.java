package client.normal;

import client.Start;
import message.FileMessage;

import java.io.File;
import java.io.IOException;

public class saveFile {//client断点续传接收点
    private Long start;
    private String name;
    private String path;
    private String rep_id;//接收方
    private String send_id;//发送方
    private final static String Username = System.getProperty("user.name");
    private final static String dir = "/home/"+Username+"/bronyaNettyChatFiles/breakPointResumeSaveDirectory.txt";

    public static File createFile(String path) throws IOException {
        File file = new File(path);
        if(file.exists()){
           return file;
        }
        File file1 = file.getParentFile();
        if(!file1.exists()){
            if(!file1.mkdirs()){
                System.err.println("创建文件失败!");
            }
        }
        if(!file.exists()){
            if(!file.createNewFile()){
                System.err.println("创建文件失败!");
            }
        }

        return file;
    }

    public static void saveFileStart(FileMessage msg) {
        File saveFile;
        File file;
        try {
            file = new File(msg.getPath());
            saveFile = createFile(dir);
        }catch (IOException e){
            e.printStackTrace();
        }

        try(){

        }

    }

}
