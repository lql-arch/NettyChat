package client.System;


import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.LoadMessage;
import message.UserMessage;

import java.util.*;

public class ChatSystem {
    public synchronized static void friendSystem(LoadMessage load, ChannelHandlerContext ctx,UserMessage friend) throws InterruptedException {
        List<String> friends = load.getFriends();
        Map<Integer,String> map = new HashMap<>();
        Iterator<String> iter = friends.iterator();
        int count = 1;
        String choice ;

        while(true) {
            //分页未实现
            System.out.println("-------------------------------------------");
            while (iter.hasNext()) {
                String name = iter.next();
                map.put(count, name);
                System.out.printf("\t" + (count++) + ".%20s\t\n", name);
            }
            System.out.println("-------------------------------------------");
            System.out.printf("\tnumber of pages:%d/%d\n", 1, 1);
            System.out.println("-------------------------------------------");

            while (true) {
                System.out.println("请选择需要查看的好友：(输入'q'表示退出)");
                choice = new Scanner(System.in).nextLine();
                if (Objects.equals(choice, "q")) {
                    return;
                }
                if (!isDigit(choice)) {
                    System.err.println("输入错误。");
                    continue;
                } else {
                    String uid = map.get(Integer.getInteger(choice));
                    friendMaterial(uid,ctx,friend);
                    break;
                }
            }
        }

    }

    public static boolean isDigit(String str){
        try{
            Integer.valueOf(str);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static void groupSystem(LoadMessage load, ChannelHandlerContext ctx){

    }

    private static void friendMaterial(String uid, ChannelHandlerContext ctx,UserMessage friend) throws InterruptedException {
        ctx.channel().writeAndFlush(new UserMessage(uid));
        Start.count.await();

        boolean flag = false;
        String t = (Objects.equals(friend.getGander(), "n") ? "男" : (Objects.equals(friend.getGander(), "m") ? "女" : "未知"));
        while(true) {
            System.out.println("----------------------------------------");
            System.out.println("uid:" + friend.getUid());
            System.out.println("用户名:" + friend.getName());
            System.out.println("性别:" + t);
            System.out.println("年龄:" + friend.getAge());
            System.out.println("用户创建时间:" + friend.getBuild_time());
            System.out.println("----------------------------------------");
            System.out.println("1.发送消息\t2.返回\t");
            while (!flag) {
                char choice = (char) new Scanner(System.in).nextByte();
                switch (choice) {
                    case 1:
                        SendMessageSystem.sendFriend();
                        flag = true;
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

}
