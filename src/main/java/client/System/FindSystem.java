package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.RequestMessage;
import message.UserMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class FindSystem {
    private static final Logger log = LogManager.getLogger();

    public static void FindUid(ChannelHandlerContext ctx) throws IOException, InterruptedException {
        int read = 0;
        byte[] b = new byte[1024];
        System.out.println("请输入想要查询的用户uid：(ctrl+d退出)");
        if((read = System.in.read(b)) == -1){
            read = 3;
            b = "end".getBytes();
            return;
        }
        String uid = new String(b,0,read-1);
//        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
//        String uid = null;
//        try{
//            if((uid = systemIn.readLine()) == null) {
//                return;
//            }
//        } catch (Exception e) {
//            System.err.println("main_menu exception end:"+e);
//        }
        String myUid = Start.uid;
        ctx.channel().writeAndFlush(new UserMessage(myUid));
        Start.semaphore.acquire(1);
        UserMessage me = Start.friend;

        ctx.channel().writeAndFlush(new UserMessage(uid));
//        Thread.sleep(1000);
//        log.debug(Start.semaphore.availablePermits());
        Start.semaphore.acquire(1);
        UserMessage user = Start.friend;
        if(user.getUid().equals(me.getUid()) || user.getBuild_time() == null){
            System.out.println("查无此人");
            return;
        }
        extracted(ctx,me,user);

    }

    public static void extracted(ChannelHandlerContext ctx,UserMessage me, UserMessage user) throws IOException, InterruptedException {

        String t = (Objects.equals(user.getGander(), "n") ? "男" : (Objects.equals(user.getGander(), "m") ? "女" : "未知"));
        while(true) {
            System.out.println("----------------------------------------");
            System.out.println("uid:" + user.getUid());
            System.out.println("用户名:" + user.getName());
            System.out.println("性别:" + t);
            System.out.println("年龄:" + user.getAge());
            System.out.println("用户创建时间:" + user.getBuild_time());//.format(LocalDateTime.now())
            System.out.println("----------------------------------------");
            System.out.println("1.加为好友\t2.返回\t");
            System.out.println("----------------------------------------");
            char choice = (char) new Scanner(System.in).nextByte();
            switch (choice) {
                case 1:
                    RequestMessage rm = new RequestMessage().setRequestPerson(me).setRecipientPerson(user).setAddOrDelete(true).setFriend(true);
                    ctx.channel().writeAndFlush(rm);
                    break;
                case 2:
                    return;
                default:
                    System.err.println("Error:无此选项,请重新输入.");
                    break;
            }
        }
    }



}
