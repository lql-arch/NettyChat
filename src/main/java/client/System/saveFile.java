package client.System;

import config.Json;
import message.FileMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class saveFile {//client断点续传接收点
    private final static String Username = System.getProperty("user.name");
    private final static String dir = "/home/"+Username+"/bronyaNettyChatFiles/GroupBreakPointResumeSaveDirectory.txt";

    private final static Lock saveLock = new ReentrantLock();
    private final static Lock readLock = new ReentrantLock();
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

    public static void saveFileStart(FileMessage msg,boolean isFirst) {
        File saveFile;
        try {
            saveFile = createFile(dir);
            if(isFirst) {
                try(BufferedWriter out = new BufferedWriter(new FileWriter(saveFile, true))){
                    String save = Json.pojoToJson(msg);
                    out.write(save);
                    out.append(System.getProperty("line.separator"));
                }
            }else{
                // 内存流, 作为临时流
                CharArrayWriter tempStream = new CharArrayWriter();
                try(BufferedReader in = new BufferedReader(new FileReader(saveFile))){
                    String line;
                    while((line = in.readLine()) != null){
                        FileMessage fm = Json.jsonToPojo(line,FileMessage.class);
                        if(fm.getPath().equals(msg.getPath()) && fm.getMyUid().equals(msg.getMyUid()) && fm.getGid().equals(msg.getGid()) && fm.getStartPos() < fm.getFileLen()) {
                            String src = "\"startPos\":" + fm.getStartPos();
                            String replace = "\"startPos\":" + msg.getStartPos();
                            // 替换符合条件的字符串
                            line = line.replaceAll(src, replace);
                        }
                        // 将该行写入内存
                        tempStream.write(line);
                        tempStream.append(System.getProperty("line.separator"));
                    }
                }
                // 将内存中的流 写入 文件
                try {
                    saveLock.lock();
                    try (FileWriter out = new FileWriter(saveFile)) {
                        tempStream.writeTo(out);
                    }
                }finally {
                    saveLock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FileMessage> readFile(String myUid) throws FileNotFoundException {
        boolean isDelete = true;
        List<FileMessage> fileMessages = null;
        File file = new File(dir);
        if(!file.exists()){
            return null;
        }

        try(BufferedReader in = new BufferedReader(new FileReader(file))){
            String line;
            while((line = in.readLine()) != null){
                FileMessage fm = Json.jsonToPojo(line,FileMessage.class);
                if(fm.getStartPos() < fm.getFileLen()){
                    isDelete = false;
                    if(fm.getMyUid().equals(myUid)){
                        if(fileMessages == null)
                            fileMessages = new ArrayList<>();
                        fileMessages.add(fm);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(isDelete && file.exists()){
            try {
                readLock.lock();
                if (!file.delete()) {
                    System.err.println("文件资料删除失败");
                }
            }finally {
                readLock.unlock();
            }
        }

        return fileMessages;
    }
}
