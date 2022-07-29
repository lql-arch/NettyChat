package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SendMessageSystem {
    private static final Logger log = LogManager.getLogger(SendMessageSystem.class);
    public static void sendFriend(ChannelHandlerContext ctx, UserMessage me,UserMessage friend) throws IOException, InterruptedException {
        byte b[]= new byte[1024];
        Timestamp date = Timestamp.valueOf(LocalDateTime.now());

        System.out.println("--------------------------------------------------");
        System.out.println("\t\t\t"+friend.getName()+"\t");
        System.out.println("--------------------------------------------------");
        ctx.channel().writeAndFlush(new LoginStringMessage("singleLoad!"+me.getUid()+"!"+friend.getUid()));
        showMessage(me,friend);
        Start.singleFlag.set(true);
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
        for(int read;(read = System.in.read(b)) != -1;) {
            String string = new String(b, 0, read);
            date = Timestamp.valueOf(LocalDateTime.now());
            System.out.println(me.getName() + ": " + string);
            ctx.channel().writeAndFlush(new StringMessage(me,friend,string,date));
        }
        System.out.println("--------------------------------------------------");
        Start.singleFlag.set(false);
        //在此处更新数据未读状态(date之前的消息都改为未读)
        ctx.channel().writeAndFlush(new ReviseMsgStatusMessage(me.getUid(),friend.getUid(),date));//time send_uid rg_id
//        time.interrupt();
//        time.join();
    }

    private static void showMessage(UserMessage me,UserMessage friend){
        List<StringMessage> message = Start.message;//登录后未读
        List<Chat_record> load = null;
        if(Start.unread_message != 0) {
            load = Start.singleLoad.getMessage();//登录前未读
        }
        Timestamp date, lastDate = null;
        if(load != null) {
            for (Chat_record chat : load) {
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
        if(message != null) {
            for (StringMessage msg : message) {
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

}
