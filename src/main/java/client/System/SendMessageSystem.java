package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SendMessageSystem {
    private static final Logger log = LogManager.getLogger(SendMessageSystem.class);
    public static void sendFriend(ChannelHandlerContext ctx, UserMessage me,UserMessage friend) throws IOException, InterruptedException {
        byte b[]= new byte[1024];
        int read;
        Timestamp date = Timestamp.valueOf(DateTimeFormatter.ofPattern("HH:mm:ss").toString());

        System.out.println("--------------------------------------------------");
        System.out.println("\t"+friend.getName()+"\t");
        System.out.println("--------------------------------------------------");
        ctx.channel().writeAndFlush(new LoginStringMessage("singleLoad!"+me.getUid()+"!"+friend.getUid()));
        showMessage(me,friend);
        Start.singleFlag.set(true);
        Thread time = new Thread(()->{//每五分钟发送一次时间
            while(true){
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                System.out.println(dtf);
                try {
                    Thread.sleep(5*60*1000);
                } catch (InterruptedException e) {
                    log.info(me.getUid()+"退出单聊");
                    e.printStackTrace();
                }
            }
        });
        time.start();
        while((read = System.in.read(b)) != -1) {
            String string = new String(b, 0, read);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            date = Timestamp.valueOf(dtf.toString());
            System.out.println(me.getName() + ": " + string);
            ctx.channel().writeAndFlush(new StringMessage(me,friend,string,date));
        }
        System.out.println("--------------------------------------------------");
        Start.singleFlag.set(false);
        //在此处更新数据未读状态(date之前的消息都改为未读)
        ctx.channel().writeAndFlush(new ReviseMsgStatusMessage(me.getUid(),friend.getUid(),date));//time send_uid rg_id
        time.interrupt();
        time.join();
    }

    private static void showMessage(UserMessage me,UserMessage friend){
        List<StringMessage> message = Start.message;//登录后未读
        List<Chat_record> load = Start.singleLoad.getMessage();//登录前未读
        Timestamp date,lastDate = null;
        for(Chat_record chat: load){
            date = chat.getTime();
            if(lastDate == null){
                System.out.println(date);
            }else if(date.getTime() - lastDate.getTime() >= 5 * 60 * 1000){
                System.out.println(date);
            }
            System.out.println((chat.getSend_uid().equals(me.getUid()) ? me.getName() : friend.getName())+":"+chat.getText());
            lastDate = date;
        }
        for(StringMessage msg : message){
            date = msg.getTime();
            if(lastDate == null){
                System.out.println(date);
            }else if(date.getTime() - lastDate.getTime() >= 5 * 60 * 1000){
                System.out.println(date);
            }
            System.out.println(msg.getMe().getName()+":"+msg.getMessage());
            lastDate = date;
        }
    }

}
