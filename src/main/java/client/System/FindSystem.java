package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.UserMessage;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class FindSystem {
    public static void FindUid(ChannelHandlerContext ctx,UserMessage user) throws IOException, InterruptedException {
        int read = 0;
        byte[] b = new byte[1024];
        System.out.println("请输入想要查询的用户uid：(ctrl+d退出)");
        read = System.in.read(b);
        if(read == -1){
            return;
        }
        String uid = new String(b,0,read);
        String myUid = Start.uid;
        ctx.channel().writeAndFlush(new UserMessage(myUid));
        Start.count.await();
        UserMessage me = user;

        ctx.channel().writeAndFlush(new UserMessage(uid));
        Start.count.await();
        extracted(ctx,me,user);

    }

    private static void extracted(ChannelHandlerContext ctx,UserMessage me, UserMessage user) throws IOException, InterruptedException {
        boolean flag = false;

        String t = (Objects.equals(user.getGander(), "n") ? "男" : (Objects.equals(user.getGander(), "m") ? "女" : "未知"));
        while(true) {
            System.out.println("----------------------------------------");
            System.out.println("uid:" + user.getUid());
            System.out.println("用户名:" + user.getName());
            System.out.println("性别:" + t);
            System.out.println("年龄:" + user.getAge());
            System.out.println("用户创建时间:" + user.getBuild_time());
            while (!flag) {
                System.out.println("----------------------------------------");
                System.out.println("1.加为好友\t2.发消息\t3.返回\t");
                System.out.println("----------------------------------------");
                char choice = (char) new Scanner(System.in).nextByte();
                switch (choice) {
                    case 1:
                        addFriend(ctx,me,user);
                    case 2:
                        SendMessageSystem.sendFriend(ctx,me,user);
                        flag = true;
                        break;
                    case 3:
                        return;
                    default:
                        System.err.println("Error:无此选项,请重新输入.");
                        break;
                }
            }
        }
    }


    private static void addFriend(ChannelHandlerContext ctx,UserMessage me, UserMessage user){

    }
}
