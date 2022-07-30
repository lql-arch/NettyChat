package client.System;


import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class ChatSystem {
    private static final Logger log = LogManager.getLogger();
    //添加黑名单，在线状态
    public synchronized static void friendSystem(LoadMessage load, ChannelHandlerContext ctx) throws InterruptedException, IOException {
        List<String> friends = load.getFriends();
        Map<String,String> map = new HashMap<>();
        String choice;
        while(true) {
            Iterator<String> iter = friends.iterator();
            int count = 1;
            //分页未实现
            System.out.println("-------------------------------------------");
            System.out.println("------------    我的好友     ----------------");
            System.out.println("-------------------------------------------");
            while (iter.hasNext()) {
                String name = iter.next();
                map.put(String.valueOf(count), name);
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
                } else {
                    String name = map.get(choice);
                    friendMaterial(Start.nameUidMap.get(name),ctx);
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

    private static void friendMaterial(String uid, ChannelHandlerContext ctx) throws InterruptedException, IOException {
//        log.debug(uid);
        String myUid = Start.uid;
        ctx.channel().writeAndFlush(new UserMessage(myUid));
        Start.semaphore.acquire();
        UserMessage me = Start.friend;

        ctx.channel().writeAndFlush(new UserMessage(uid));
        Start.semaphore.acquire();
        UserMessage friend = Start.friend;

        boolean flag = false;
        String t = (Objects.equals(friend.getGander(), "n") ? "男" : (Objects.equals(friend.getGander(), "m") ? "女" : "未知"));
        while(true) {
            System.out.println("----------------------------------------");
            System.out.println("uid:" + friend.getUid());
            System.out.println("用户名:" + friend.getName());
            System.out.println("性别:" + t);
            System.out.println("年龄:" + friend.getAge());
            System.out.println("用户创建时间:" + friend.getBuild_time());
            //好友状态
            //System.out.println("用户状态:" + friend.getStatus());
            while (!flag) {
                System.out.println("----------------------------------------");
                System.out.println("1.发送消息\t2.返回\t");
                System.out.println("----------------------------------------");
                char choice = (char) new Scanner(System.in).nextByte();
                switch (choice) {
                    case 1:
                        SendMessageSystem.sendFriend(ctx,me,friend);
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


    public static void unreadMessage(ChannelHandlerContext ctx,LoadMessage load) throws InterruptedException {
        while(true){
            ctx.channel().writeAndFlush(new LoginStringMessage("flush!"+load.getUid()));
            Start.semaphore.acquire();
            String normal = (load.getHasRequest() != 0 && load.getHasRequest() != 2) ? "new" : " " ;
            String Request = (load.getHasRequest() != 0 && load.getHasRequest() != 1) ? "new" : " " ;
            System.out.println("----------------------------------------");
            System.out.println("--------        1.好友/群消息("+normal+")   ---------");
            System.out.println("--------        2.申请消息("+Request+")      ---------");
            System.out.println("--------        3.返回          ---------");
            System.out.println("----------------------------------------");
            char choice = (char) new Scanner(System.in).nextByte();
            switch (choice) {
                case 1:
                    unreadFriendMsg(ctx,load);
                    break;
                case 2:
                    unreadRequestMsg(ctx,load);
                    break;
                case 3:
                    return;
                default:
                    System.err.println("Error:无此选项,请重新输入.");
                    break;
            }
        }
    }

    public static void unreadFriendMsg(ChannelHandlerContext ctx,LoadMessage load) throws InterruptedException {
        while(true) {
            ctx.channel().writeAndFlush(new LoginStringMessage("flush!"+Start.uid));
            Start.semaphore.acquire();
            int count = 1;
            List<Chat_record> crs = load.getMessage();
            Map<String,String> uidNameMap = load.getUidNameMap();
//            List<Chat_group> group = load.getGroup();
            Map<String,String> countUidMap = new HashMap<>();
            Map<String, Integer> uidSet  = new HashMap<>();//发出消息的好友

            for(Chat_record cr : crs){
                if(cr.getType() != 0)
                    continue;
                uidSet.merge(cr.getSend_uid(),1,Integer::sum);
            }

            System.out.println("------------------------------------------");
            for(Map.Entry<String, Integer> uid : uidSet.entrySet()) {
                countUidMap.put(String.valueOf(count),uid.getKey());
                System.out.println("\t"+(count++)+"." +uidNameMap.get(uid.getKey())+"("+uid.getValue()+")");
            }
            System.out.println("------------------------------------------");
            System.out.println("输入“EXIT”退出");
            String choice;
            try {
                if ((choice = new Scanner(System.in).nextLine()) != null) {
                    if(choice.compareToIgnoreCase("EXIT") == 0){
                        return;
                    }
                    UserMessage me = new UserMessage(load.getUid());
                    me.setName(load.getName());
                    me.setUid(Start.uid);

                    String uid = countUidMap.get(choice);
                    UserMessage friend = new UserMessage(uid);
                    friend.setName(uidNameMap.get(uid));

                    SendMessageSystem.sendFriend(ctx,me,friend);
                }
            }catch (Exception ignored){}
        }
    }

    public static void unreadRequestMsg(ChannelHandlerContext ctx,LoadMessage load) throws InterruptedException {
        while(true) {
            ctx.channel().writeAndFlush(new LoginStringMessage("flush!"+load.getUid()));
            Start.semaphore.acquire();
            int count = 1;
            List<Chat_record> crs = load.getMessage();
            Map<String,String> uidNameMap = new HashMap<>();
            Map<String,Chat_record> countCrMap = new HashMap<>();
            for(Chat_record cr : crs){
                if(cr.getType() == 0)
                    continue;
                ctx.writeAndFlush(new UserMessage(cr.getSend_uid()));
                Start.semaphore.acquire();
                uidNameMap.put(Start.friend.getUid(),Start.friend.getName());
            }

            System.out.println("------------------------------------------");
            for(Chat_record cr : crs){
                if(cr.getType() == 1) {
                    countCrMap.put(String.valueOf(count),cr);
                    System.out.println("\t"+(count++)+"."+uidNameMap.get(cr.getSend_uid())+"发送来了好友申请。");
                }else if(cr.getType() == 2){
                    System.out.println("\t"+"通知："+cr.getText());
                }
            }
            System.out.println("------------------------------------------");
            System.out.println("输入“EXIT”退出");
            String choice;
            try {
                if ((choice = new Scanner(System.in).nextLine()) != null) {
                    if(choice.compareToIgnoreCase("EXIT") == 0){
                        return;
                    }
                    replyToMakeFriends(ctx,load,countCrMap.get(choice),uidNameMap);//同时将所有结束的好友申请标记为已读
                }
            }catch (Exception ignored){}
        }
    }

    public static void replyToMakeFriends(ChannelHandlerContext ctx,LoadMessage load, Chat_record cr, Map<String, String> uidNameMap) throws InterruptedException {

        System.out.println("-------------------------------------");
        System.out.println("\t\t是否同意"+uidNameMap.get(cr.getSend_uid())+"的好友申请？(yes/no)");
        System.out.println("-------------------------------------");
        String t = new Scanner(System.in).nextLine();
        if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
            log.debug("yes");
            RequestMessage rm = new RequestMessage(new UserMessage(cr.getSend_uid(),uidNameMap.get(cr.getSend_uid())),new UserMessage(Start.uid, load.getName()),true,true);

//            rm.setConfirm(true);
//            rm.setFriend(true);
            ctx.writeAndFlush(rm);
            Start.semaphore.acquire();
        }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
            log.debug("no");
            RequestMessage rm = new RequestMessage(new UserMessage(cr.getSend_uid(),uidNameMap.get(cr.getSend_uid())),new UserMessage(Start.uid, load.getName()),false,false);

//            rm.setFriend(false);
            ctx.channel().writeAndFlush(rm);
            Start.semaphore.acquire();
        }else{
            return;
        }

        ctx.channel().writeAndFlush(new RequestMessage(new UserMessage(Start.uid, load.getName())).SetClearMsg(true));
    }

}
