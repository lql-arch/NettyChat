package client.System;

import client.SimpleChannelHandler.FileMsgHandler;
import client.Start;
import client.normal.Chat_record;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import static client.Start.uid;

public class SendMessageSystem {
    private static final Logger log = LogManager.getLogger(SendMessageSystem.class);

    public static String[] send = new String[2];

    public static void sendFriend(ChannelHandlerContext ctx, UserMessage me, UserMessage friend) throws IOException, InterruptedException {
        Timestamp date = Timestamp.valueOf(LocalDateTime.now());
        String string;

        send[0] = uid;
        send[1] = friend.getUid();
        System.out.println("--------------------------------------------------");
        System.out.println("\t\t\t" + friend.getName() + "(输入EXIT退出)\t");
        System.out.println("--------------------------------------------------");
        showMessage(me, friend);
        Start.singleFlag.set(true);

        while ((string = new Scanner(System.in).nextLine()) != null) {
            if (string.compareToIgnoreCase("exit") == 0) {
                break;
            }
            date = Timestamp.valueOf(LocalDateTime.now());
            System.out.println(me.getName() + ": " + string);
            StringMessage sm = new StringMessage(me, friend, string, date.toString());
            Start.message.add(sm);//本次登录储存起来
            ctx.channel().writeAndFlush(sm);
        }

        Start.singleFlag.set(false);
        System.out.println("--------------------------------------------------");
        //在此处更新数据未读状态(date之前的消息都改为未读)
        ctx.channel().writeAndFlush(new ReviseMsgStatusMessage(me.getUid(), friend.getUid(), date.toString()));//time send_uid rg_id
    }

    private static void showMessage(UserMessage me, UserMessage friend) {
        List<Chat_record> load;
        Timestamp date, lastDate = null;
        if (Start.unread_message != 0) {
            load = Start.singleLoad.getMessage();//登录前未读
            if (load != null) {
                for (Chat_record chat : load) {
                    if (!chat.getSend_uid().equals(me.getUid()) && !chat.getSend_uid().equals(friend.getUid()))
                        continue;
                    date = chat.getTime();
                    if (lastDate == null) {
                        System.out.println(date);
                    } else if (date.getTime() - lastDate.getTime() >= 5 * 60 * 1000) {
                        System.out.println(date);
                    }
                    System.out.println((chat.getSend_uid().equals(me.getUid()) ? me.getName() : friend.getName()) + ":" + chat.getText());
                    lastDate = date;
                }
            }
        }
        if (Start.message != null) {//登录后内容
            for (StringMessage msg : Start.message) {
                if (msg.getFriend().getUid().compareTo(uid) != 0 &&
                        msg.getFriend().getUid().compareTo(friend.getUid()) != 0) {
                    continue;
                }
                date = msg.getTime();
                if (lastDate == null) {
                    System.out.println(date);
                } else if (date.getTime() - lastDate.getTime() >= 5 * 60 * 1000) {
                    System.out.println(date);
                }
                System.out.println(msg.getMe().getName() + ":" + msg.getMessage());
                lastDate = date;
            }
        }
    }

    public static void sendFileUser(ChannelHandlerContext ctx, UserMessage me, UserMessage friend) throws InterruptedException {
        System.out.println("-------------------------------------");
        System.out.println("\t\t\t\t传输文件");
        System.out.println("-------------------------------------");
        System.out.println("请输入您要传输的文件地址：");
        String pass = new Scanner(System.in).nextLine();
        File file = new File(pass);
        if(!file.exists()){
            if (!file.isFile()) {
                System.out.println("Not a file :" + file);
                return;
            }
        }
        FileMsgHandler.sendFile(ctx,file,me,friend);

    }

}

//        Thread time = new Thread(()->{//每五分钟发送一次时间
//            while(true){
//                //LocalDateTime localDateTime = LocalDateTime.now();
//                Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
//                System.out.println("\t"+timestamp);
//                try {
//                    Thread.sleep(5*60*1000);
//                } catch (InterruptedException e) {
//                    log.info(me.getUid()+"--退出单聊");
//                    e.printStackTrace();
//                }
//            }
//        });
//        time.start();
//        try {
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//        }
//        time.interrupt();
//        time.join();