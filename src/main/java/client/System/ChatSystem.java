package client.System;


import client.SimpleChannelHandler.FileReadHandler;
import client.Start;
import client.normal.Chat_record;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

import static client.Start.*;

public class ChatSystem {
    private static final Logger log = LogManager.getLogger();
    public synchronized static void friendSystem(ChannelHandlerContext ctx) throws InterruptedException, IOException {
        while(true) {
            ctx.channel().writeAndFlush(new LoginStringMessage("flush!"+Start.uid));
            semaphore.acquire();
            List<String> friends = load.getFriends();
            Map<String,String> map = new HashMap<>();
            String choice;
            Iterator<String> iter = friends.iterator();
            int count = 1;
            System.out.println("\t-------------------------------------------");
            System.out.println("\t------------    我的好友     ----------------");
            System.out.println("\t-------------------------------------------");
            while (iter.hasNext()) {
                String name_uid = iter.next();
                if(name_uid == null || Start.uidNameMap.get(name_uid) == null){
                    System.out.println("检测到好友变化!");
                    break;
                }
                if(load.getBlacklist().get(name_uid)){
                    continue;
                }
                map.put(String.valueOf(count), name_uid);
                System.out.printf("\t\t" + (count++) + ".%20s\t\n", Start.uidNameMap.get(name_uid));
            }
            System.out.println("\t-------------------------------------------");
            System.out.printf("\t\tnumber of pages:%d/%d\n", 1, 1);
            System.out.println("\t-------------------------------------------");

            while (true) {
                System.out.println("请选择需要查看的好友：(输入'q'表示退出)");
                choice = new Scanner(System.in).nextLine();
                if (Objects.equals(choice, "q")) {
                    return;
                }
                if (!isDigit(choice)) {
                    System.err.println("输入错误。");
                    continue;
                }
                int result = Integer.parseInt(choice);
                if(result > 0 && result < count) {
                    String name_uid = map.get(choice);
                    friendMaterial(name_uid, ctx);
                    break;
                }else{
                    System.err.println("输入错误。");
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

    public static void friendMaterial(String uid, ChannelHandlerContext ctx) throws InterruptedException, IOException {
        String myUid = Start.uid;
        ctx.channel().writeAndFlush(new UserMessage(myUid));
        semaphore.acquire();
        UserMessage me = Start.friend;

        ctx.channel().writeAndFlush(new UserMessage(uid));
        semaphore.acquire();
        UserMessage friend = Start.friend;

        String t = (Objects.equals(friend.getGander(), "n") ? "男" : (Objects.equals(friend.getGander(), "m") ? "女" : "未知"));
        while(true) {
            boolean flag = false;
            System.out.println("----------------------------------------");
            System.out.println("uid:" + friend.getUid());
            System.out.println("用户名:" + friend.getName());
            System.out.println("性别:" + t);
            System.out.println("年龄:" + friend.getAge());
            System.out.println("用户创建时间:" + friend.getBuild_time());
            //好友状态
            System.out.println("用户状态:" + (friend.isStatus() ? "online" : " not online"));
            while (!flag) {
                System.out.println("----------------------------------------");
                System.out.println("1.发送消息\t2.发送文件\t3.查询历史消息\n" +
                        "4.加入黑名单\t5.删除好友\t6.返回\t7.刷新\t");
                System.out.println("----------------------------------------");
                String choice =  new Scanner(System.in).nextLine();
                switch (choice) {
                    case "1":
                        SendMessageSystem.sendFriend(ctx,me,friend);
                        flag = true;
                        break;
                    case  "2":
                        SendMessageSystem.sendFileUser(ctx,me,friend);
                        flag = true;
                        break;
                    case "3":
                        FindSystem.myHistoricalNews(ctx,me,friend);
                        break;
                    case "4":
                        if(DeleteSystem.removeBlackFriend(ctx,me,friend))
                            return;
                        else
                            break;
                    case "5":
                        if(DeleteSystem.deleteFriend(ctx,me,friend))
                            return;
                        else
                            break;
                    case "6":
                        return;
                    case  "7":
                        flag = true;
                        break;
                    default:
                        System.err.println("Error:无此选项,请重新输入.");
                        break;
                }
            }
        }
    }


    public static void unreadMessage(ChannelHandlerContext ctx,LoadMessage load) throws InterruptedException, IOException {
        while(true){
            ctx.writeAndFlush(new LoginStringMessage("flush!"+uid));
            semaphore.acquire();
            String normal = (load.getHasRequest() != 0 && load.getHasRequest() != 2) ? "new" : " " ;
            String Request = (load.getHasRequest() != 0 && load.getHasRequest() != 1) ? "new" : " " ;
            System.out.println("---------------------------------------------");
            System.out.println("--------        1.好友消息("+normal+")    \t---------");
            System.out.println("--------        2.申请消息("+Request+")   \t---------");
            System.out.println("--------        3.群消息         \t---------");
            System.out.println("--------        4.文件信息       \t---------");
            System.out.println("--------        5.返回           \t---------");
            System.out.println("---------------------------------------------");
            String choice = new Scanner(System.in).nextLine();
            switch (choice) {
                case "1":
                    unreadFriendMsg(ctx,load);
                    break;
                case "2":
                    unreadRequestMsg(ctx,load);
                    break;
                case "3":
                    GroupSystem.GroupMsg(ctx);
                    break;
                case "4":
                    fileMsg(ctx,null,true);
                    break;
                case "5":
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
            semaphore.acquire();
            int count = 1;
            List<Chat_record> crs = load.getMessage();
            Map<String,String> uidNameMap = load.getUidNameMap();
            Map<String,String> countUidMap = new HashMap<>();
            Map<String, Integer> uidSet  = new HashMap<>();//发出消息的好友

            for(Chat_record cr : crs){
                if(cr.getType() != 0 || !cr.isStatus())
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
                    if(countUidMap.get(choice) == null){
                        continue;
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
            semaphore.acquire();
            int count = 1;
            List<Chat_record> crs = load.getMessage();
            Map<String,String> uidNameMap = new HashMap<>();
            Map<String,Chat_record> countCrMap = new HashMap<>();
            for(Chat_record cr : crs){
                if(cr.getType() == 0)
                    continue;
                ctx.writeAndFlush(new UserMessage(cr.getSend_uid()));
                semaphore.acquire();
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
            RequestMessage rm = new RequestMessage().setRequestPerson(new UserMessage(cr.getSend_uid(),uidNameMap.get(cr.getSend_uid()))).setRecipientPerson(new UserMessage(Start.uid, load.getName())).setConfirm(true).setFriend(true).setAddOrDelete(true);
            ctx.writeAndFlush(rm);
            semaphore.acquire();
        }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
            log.debug("no");
            RequestMessage rm = new RequestMessage().setRequestPerson(new UserMessage(cr.getSend_uid(),uidNameMap.get(cr.getSend_uid()))).setRecipientPerson(new UserMessage(Start.uid, load.getName())).setConfirm(false).setFriend(false).setAddOrDelete(true);
            ctx.channel().writeAndFlush(rm);
            semaphore.acquire();
        }else{
            return;
        }

        ctx.channel().writeAndFlush(new RequestMessage().setClearMsg(true).setRecipientPerson(new UserMessage(Start.uid, load.getName())).setAddOrDelete(true));
    }

    public static void fileMsg(ChannelHandlerContext ctx,String friend_uid,boolean isPub) throws InterruptedException {
        while(true) {
            ctx.writeAndFlush(new FileRead().setCheckFile(true).setUid(uid));
            semaphore.acquire();
            FileRead fileRead = FileReadHandler.fileRead;
            Map<String, String> time = fileRead.getFileTimeMap();
            Map<Integer, String> countMap = new HashMap<>();
            int count = 1;

            System.out.println("------------------------------------------------------------------------------------------");
            System.out.println("\t\t\t输入\"quit\"返回");
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("%5s\t%20s\t%20s\t%20s\n", "id", "file_name", "file_sender", "file_time");
            if(fileRead.getFilePersonMap() == null && fileRead.getFileTimeMap() == null){
                System.out.println("------------------------------------------------------------------------------------------");
                return;
            }
            for (Map.Entry<String, String> person : fileRead.getFilePersonMap().entrySet()) {
                String name = uidNameMap.get(person.getValue());
                if (!isPub && !Objects.equals(friend_uid, person.getValue()))
                    continue;
                if (person.getValue() == null)
                    continue;
                if(Start.load.getBlacklist().get(person.getValue())){
                    continue;
                }
                countMap.put(count, person.getKey());
                System.out.printf("%5d\t%20s\t%20s\t%20s\n", count++, person.getKey(), name, time.get(person.getKey()));
            }
            System.out.println("------------------------------------------------------------------------------------------");

            while (true) {
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
                    FileMessage fm = new FileMessage();
                    String name = countMap.get(result);
                    fm.setName(name);
                    fm.setTime(time.get(name));
                    fm.setMe(new UserMessage(fileRead.getFilePersonMap().get(name)));
                    fm.setUser(new UserMessage(uid));
                    fm.setStartPos(0);
                    fm.setPath(null);//标志物

                    ctx.writeAndFlush(fm.setReadOrWrite(true));
                    break;
                }
            }
        }
    }
}
