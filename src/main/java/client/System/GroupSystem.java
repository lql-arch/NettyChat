package client.System;

import client.SimpleChannelHandler.LoadGroupNewsHandler;
import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.Chat_group;
import message.LoadGroupMessage;
import message.LoginStringMessage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static client.Start.*;
import static client.System.ChatSystem.friendMaterial;
import static client.System.ChatSystem.isDigit;

public class GroupSystem {

    @NotNull
    private static LoadGroupMessage getLoadGroupMessage(Chat_group cg) {
        LoadGroupMessage lgm = new LoadGroupMessage();

        lgm.setGid(cg.getGid());
        lgm.setUid(uid);
        lgm.setGroupName(cg.getGroupName());
        lgm.setGroup_master(cg.getGroup_master());
        lgm.setAdministrator(cg.getAdministrator());
        lgm.setMembersCount(cg.getMembersNum());
        lgm.setTime(cg.getTime().toString());
        lgm.setLastTime(cg.getLast_msg_time());
        lgm.setMembers(cg.getMembers());


        return lgm;
    }
    public static void groupSystem(ChannelHandlerContext ctx) throws InterruptedException, IOException {
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
                String choice;
                try {
                    choice = new Scanner(System.in).nextLine();
                }catch (Exception e){
                    return;
                }
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

    public static void showGroupSystem(ChannelHandlerContext ctx,int type) throws InterruptedException, IOException {

        while(true) {
            ctx.channel().writeAndFlush(new LoginStringMessage("group!" + uid));
            semaphore.acquire();

            Map<Integer, Chat_group> countMap = new HashMap<>();
            int count = 1;
            List<Chat_group> cgs = groupLoad.getGroup();
            Iterator<Chat_group> iter = cgs.listIterator();


            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t输入\"quit\"返回");
            System.out.println("---------------------------------------------");
            while (iter.hasNext()) {
                Chat_group cg = iter.next();
                if (type == 1 && cg.getGroup_master().compareTo(uid) != 0) {
                    continue;
                }
                if (type == 2 && (cg.getAdministrator().stream().noneMatch(a -> a.compareTo(uid) == 0) && cg.getGroup_master().compareTo(uid) != 0)) {
                    continue;
                }
                countMap.put(count, cg);
                System.out.printf("\t%3d.%20s\n", count++, cg.getGroupName());
            }
            System.out.println("---------------------------------------------");

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
                    LoadGroupMessage lgm = getLoadGroupMessage(countMap.get(result));

                    showChatGroup(ctx, lgm);
                    break;
                }
            }
        }
    }



    public static void showChatGroup(ChannelHandlerContext ctx,LoadGroupMessage lgm) throws InterruptedException, IOException {
        ctx.channel().writeAndFlush(lgm);
        Start.semaphore.acquire();
        LoadGroupMessage msg = LoadGroupNewsHandler.groupMessage;
        lgm.setGroupMessages(msg.getGroupMessages());
        lgm.setMasterName(msg.getMasterName());

        while(true) {
            boolean flag = true;
            System.out.println("---------------------------------------------");
            System.out.println("\tgid:" + lgm.getGid());
            System.out.println("\t群名：" + lgm.getGroupName());
            System.out.println("\t创建者：" + lgm.getMasterName() + "(" + lgm.getGroup_master() + ")");
            System.out.println("\t创建时间" + lgm.getTime());
            System.out.println("\t人数：" + lgm.getMembersCount());
            System.out.print("---------------------------------------------\n");
            System.out.print("\t1.进入群聊\t2.查看群文件\t3.查看群成员\t4.返回\n");
            if (lgm.getGroup_master().compareTo(uid) == 0)
                System.out.println("\t5.禁言群员\t6.解散群聊");
            if (lgm.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0))
                System.out.println("\t5.禁言群员\t");
            System.out.println("---------------------------------------------");
            while (flag) {
                flag = false;
                String choice = new Scanner(System.in).nextLine();
                if (!isDigit(choice)) {
                    flag = true;
                    System.err.println("输入错误");
                    continue;
                }
                switch (Integer.parseInt(choice)) {
                    case 1:
                        enterGroupChat(ctx, lgm);
                        break;
                    case 2:
                        viewGroupFiles(ctx, lgm);
                        break;
                    case 3:
                        viewGroupMembers(ctx, lgm);
                        break;
                    case 4:
                        return;
                    case 5:
                        if (lgm.getGroup_master().compareTo(uid) == 0 || lgm.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)) {
                            bannedMembers(ctx, lgm);
                            break;
                        }
                    case 6:
                        if (lgm.getGroup_master().compareTo(uid) == 0) {
                            disbandTheGroupChat(ctx, lgm);
                            break;
                        }
                    default:
                        flag = true;
                        System.err.println("输入错误");
                        break;
                }
            }
        }
    }

    public static void unreadGroupMsg(ChannelHandlerContext ctx){

    }

    public static void buildGroup(ChannelHandlerContext ctx) throws InterruptedException {
        Scanner sc = new Scanner(System.in);

        System.out.println("---------------------------------------------");
        System.out.println("请输入你想要创建的群聊名称：");
        String name = sc.nextLine();
        System.out.println("请确认是否创建（yes/no）");
        String result = sc.next();
        if(result.compareToIgnoreCase("yes") == 0 || result.compareToIgnoreCase("y") == 0){
            LoadGroupMessage lgm = new LoadGroupMessage();
            lgm.setGroupName(name);
            lgm.setBuildGroup(true);
            lgm.setGroup_master(uid);
            ctx.writeAndFlush(lgm);
            semaphore.acquire();
            System.out.println("创建成功!");
        }
    }

    public static void enterGroupChat(ChannelHandlerContext ctx,LoadGroupMessage msg){
        System.out.print("");
        
    }

    public static void viewGroupFiles(ChannelHandlerContext ctx,LoadGroupMessage msg){

    }

    public static void viewGroupMembers(ChannelHandlerContext ctx,LoadGroupMessage msg) throws IOException, InterruptedException {
        while(true) {
            int count = 1;
            Map<Integer,String> countMap = new HashMap<>();

            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t members:" + msg.getMembersCount());
            System.out.println("---------------------------------------------");
            System.out.println("\t群主:");
            countMap.put(count, msg.getGroup_master());
            System.out.printf("\t%d.%20s\n", count++, msg.getGroupName());
            System.out.println("\t管理员:");
            for (String aUid : msg.getAdministrator()) {
                String name = msg.getUidNameMap().get(aUid);
                countMap.put(count, aUid);
                System.out.printf("\t%d.%20s\n", count++, name);
            }
            System.out.println("\t群员:");
            for (String mUid : msg.getMembers()) {
                String name = msg.getUidNameMap().get(mUid);
                countMap.put(count, mUid);
                System.out.printf("\t%d.%20s", count++, name);
            }

            while (true) {
                System.out.println("---------------------------------------------");
                System.out.println("\t\t\t 输入“exit”退出");
                if(msg.getGroup_master().compareTo(uid) == 0 || msg.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)){
                    System.out.println("\t\t\t输入“manage”进入管理");
                }
                System.out.println("---------------------------------------------");

                String choice = new Scanner(System.in).nextLine();
                if (choice.compareToIgnoreCase("exit") == 0) {
                    return;
                }
                if(choice.compareToIgnoreCase("manage") == 0) {
                    if (msg.getGroup_master().compareTo(uid) == 0) {//群主
                        groupMembersManage(ctx,msg,1);
                        break;
                    }
                    if (msg.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)) {//管理
                        groupMembersManage(ctx,msg,2);
                        break;
                    }
                }
                if (!isDigit(choice)) {
                    System.err.println("输入错误");
                    continue;
                }
                int result = Integer.parseInt(choice);
                if (result > 0 && result < count) {
                    String uid = countMap.get(result);
                    if (uidNameMap.get(uid) != null) {
                        friendMaterial(uid, ctx);
                    }else if(uid.compareTo(Start.uid) == 0){
                        MaterialSystem.myMaterial(ctx);
                    } else {
                        FindSystem.findPerson(ctx, uid);
                    }
                    break;
                } else {
                    System.err.println("输入错误");
                }
            }
        }
    }

    public static void groupMembersManage(ChannelHandlerContext ctx,LoadGroupMessage msg,int type){

    }

    public static void bannedMembers(ChannelHandlerContext ctx,LoadGroupMessage msg){

    }

    public static void disbandTheGroupChat(ChannelHandlerContext ctx,LoadGroupMessage msg){

    }
}
