package client.System;

import client.SimpleChannelHandler.LoadGroupNewsHandler;
import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.Chat_group;
import message.LoadGroupMessage;
import message.LoginStringMessage;

import java.util.*;

import static client.Start.*;
import static client.System.ChatSystem.isDigit;

public class GroupSystem {
    public static void groupSystem(ChannelHandlerContext ctx) throws InterruptedException {
        while(true) {
            boolean flag = true;
            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t输入\"quit\"返回");
            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t1.展示群列表");
            System.out.println("\t\t\t2.查询群聊（gid）");
            System.out.println("\t\t\t3.创建群聊");
            System.out.println("\t\t\t4.显示我创建的群聊");
            System.out.println("\t\t\t5.显示我管理的群聊");
            System.out.println("\t\t\t6.返回");
            System.out.println("---------------------------------------------");

            while (flag) {
                flag = false;
                String choice = new Scanner(System.in).nextLine();
                if (choice.compareToIgnoreCase("quit") == 0) {
                    return;
                }
                if (!isDigit(choice)) {
                    System.err.println("输入错误");
                    continue;
                }
                switch (choice) {
                    case "1":
                        showGroupSystem(ctx,0);
                        break;
                    case "2":
                        FindSystem.findGroup(ctx);
                        break;
                    case "3":
                        buildGroup(ctx);
                        break;
                    case "4":
                        showGroupSystem(ctx,1);
                        break;
                    case "5":
                        showGroupSystem(ctx,2);
                        break;
                    case "6":
                        return;
                    default:
                        flag = true;
                        System.err.println("输入错误");
                        break;
                }
            }
        }

    }

    public static void showGroupSystem(ChannelHandlerContext ctx,int type) throws InterruptedException {
        ctx.channel().writeAndFlush(new LoginStringMessage("group!"+uid));
        semaphore.acquire();

        Map<Integer,Chat_group> countMap = new HashMap<>();
        int count = 1;
        List<Chat_group> cgs = groupLoad.getGroup();
        Iterator<Chat_group> iter = cgs.listIterator();


        System.out.println("---------------------------------------------");
        System.out.println("\t\t\t输入\"quit\"返回");
        System.out.println("---------------------------------------------");
        while(iter.hasNext()){
            Chat_group cg = iter.next();
            if(type == 1 && cg.getGroup_master().compareTo(uid) != 0){
                continue;
            }
            if(type == 2 && (cg.getAdministrator().stream().noneMatch(a -> a.compareTo(uid) == 0) && cg.getGroup_master().compareTo(uid) != 0)){
                continue;
            }
            countMap.put(count,cg);
            System.out.printf("\t%3d.%20s\n",count++,cg.getGroupName());
        }
        System.out.println("---------------------------------------------");

        while(true) {
            String choice = new Scanner(System.in).nextLine();
            if (choice.compareToIgnoreCase("quit") == 0) {
                return;
            }
            if (!isDigit(choice)) {
                System.err.println("输入错误");
                continue;
            }
            int result = Integer.parseInt(choice);
            if (result < count && result > 0) {
                LoadGroupMessage lgm = new LoadGroupMessage();
                Chat_group cg = countMap.get(result);

                lgm.setGid(cg.getGid());
                lgm.setUid(uid);
                lgm.setGroupName(cg.getGroupName());
                lgm.setGroup_master(cg.getGroup_master());
                lgm.setAdministrator(cg.getAdministrator());
                lgm.setMembersCount(cg.getMembersNum());
                lgm.setTime(cg.getTime().toString());
                lgm.setLastTime(cg.getLast_msg_time());

                ctx.channel().writeAndFlush(lgm);
                Start.semaphore.acquire();


                showChatGroup(ctx);
            }
        }

    }

    public static void showChatGroup(ChannelHandlerContext ctx){
        LoadGroupMessage lgm = LoadGroupNewsHandler.groupMessage;

        boolean flag = true;
        System.out.println("---------------------------------------------");
        System.out.println("\tgid:"+lgm.getUid());
        System.out.println("\t群名："+lgm.getGroupName());
        System.out.println("\t创建者："+lgm.getGroup_master());
        System.out.println("\t创建时间"+lgm.getTime());
        System.out.println("\t人数："+lgm.getGroupName());
        System.out.println("---------------------------------------------");
        System.out.println("\t1.进入群聊2.查看群文件3.返回");
        System.out.println("---------------------------------------------");

        while(flag) {
            flag = false;
            String choice = new Scanner(System.in).nextLine();
            switch (choice){
                case "1":

                    break;
                case "2":

                    break;
                case "3":
                    return;
                default:
                    flag = true;
                    System.err.println("输入错误");
                    break;
            }
        }
    }

    public static void unreadGroupMsg(ChannelHandlerContext ctx){

    }

    public static void buildGroup(ChannelHandlerContext ctx){

    }
}
