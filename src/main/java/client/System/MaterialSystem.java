package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.FindMessage;
import message.ReviseMessage;
import message.UserMessage;

import java.util.*;

import static client.Start.load;
import static client.System.ChatSystem.isDigit;

public class MaterialSystem {
    public static void myMaterial(ChannelHandlerContext ctx) throws InterruptedException {
        while(true) {
            boolean flag = true;
            ctx.channel().writeAndFlush(new UserMessage(Start.uid));
            Start.semaphore.acquire();
            UserMessage me = Start.friend;

            String t = me.getGander().startsWith("n") ? "男" : (me.getGander().startsWith( "m") ? "女" : "未知");

            System.out.println("---------------------------------------------------------");
            System.out.println("\tuid:" + me.getUid());
            System.out.println("\t用户名:" + me.getName());
            System.out.println("\t性别:" + t);
            System.out.println("\t年龄:" + me.getAge());
            System.out.println("\t用户创建时间:" + me.getBuild_time());
            while (flag) {
            System.out.println("---------------------------------------------------------");
            System.out.println("\t1.返回\t2.修改密码\t3.修改用户名\n" +
                                "\t4.修改性别选项\t5.修改年龄\t6.再次显示界面");
            System.out.println("---------------------------------------------------------");
                String choice = new Scanner(System.in).next();
                if (!isDigit(choice)) {
                    System.err.println("输入错误");
                    continue;
                }
                int result = Integer.parseInt(choice);
                switch (result) {
                    case 1:
                        return;
                    case 2:
                        revisePassword(load.getUid(), ctx);
                        break;
                    case 3:
                        reviseName(load.getUid(), ctx);
                        break;
                    case 4:
                        reviseGander(load.getUid(), ctx);
                        break;
                    case 5:
                        reviseAge(load.getUid(), ctx);
                        break;
                    case 6:
                        flag = false;
                        break;
                    default:
                        System.err.println("Error:无此选项,请重新输入.");
                        break;
                }
            }
        }
    }

    public static void revisePassword(String uid, ChannelHandlerContext ctx) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        String newPassword;
        while(true) {
            System.out.println("请输入旧密码");
            String oldPassword = sc.nextLine();
            if(oldPassword.length() > 25){
                System.out.println("输入错误");
                continue;
            }
            ctx.channel().writeAndFlush(new FindMessage(uid, oldPassword));
//            Start.count.await();
            Start.semaphore.acquire();
            if (Start.EnterPassword.get()){
                break;
            }else{
                System.err.println("输入错误");
            }
        }
        while(true) {
            System.out.println("请输入新密码");
            newPassword = sc.nextLine();
            if(newPassword.length() > 25){
                System.err.println("密码字符长度不得超过25");
                continue;
            }
            System.out.println("请再次输入密码");
            if (sc.nextLine().compareTo(newPassword) != 0) {
                System.err.println("两次密码不一致");
                continue;
            }
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,null,newPassword,null));
//                Start.count.await();
                Start.semaphore.acquire();
                break;
            }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
                break;
            }
        }
    }

    public static void reviseName(String uid, ChannelHandlerContext ctx) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        String name;
        while(true) {
            System.out.println("请输入新用户名");
            name = sc.nextLine();
            if(name.length() > 25 ){
                System.err.println("字符长度不得超过25");
                continue;
            }
            if(name.length() < 1){
                System.err.println("字符长度不得少于1");
                continue;
            }
            if(name.equals("帐号已注销")){
                System.err.println("帐号名称不规范");
                continue;
            }
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,name,null,null));
//                Start.count.await();
                Start.semaphore.acquire();
                break;
            }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
                break;
            }
        }
    }

    public static void reviseGander(String uid, ChannelHandlerContext ctx) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        String gander;
        while(true){
            System.out.println("请输入性别(女(m)/男(n))");
            gander = sc.nextLine();
            if(gander.startsWith("女") || gander.startsWith("m")){
                gander = "m";
            }else if(gander.startsWith("男") || gander.startsWith("n")){
                gander = "n";
            }else{
                gander = "b";//无
            }
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,null,null,gander));
//                Start.count.await();
                Start.semaphore.acquire();
                break;
            }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
                break;
            }
        }
    }

    public static void reviseAge(String uid, ChannelHandlerContext ctx) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        int age;
        while(true) {
            System.out.println("请输入年龄");
            String ageStr = sc.nextLine();
            if(!isDigit(ageStr)){
                System.err.println("包含错误字符，请重新输入.");
                continue;
            }
            age = Integer.parseInt(ageStr);
            if(age >= 150){
                System.err.println("？？？");
                continue;
            }
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,age));
//                Start.count.await();
                Start.semaphore.acquire();
                break;
            }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
                break;
            }
        }
    }

    public static void blacklist( ChannelHandlerContext ctx) throws InterruptedException {
        List<String> blacklist = new ArrayList<>();
        for(String friend_uid : load.getFriends()){
            if(load.getBlacklist().get(friend_uid)){
                blacklist.add(friend_uid);
            }
        }
        Map<String,String> map = new HashMap<>();
        String choice;
        while(true) {
            int count = 1;
            Iterator<String> iter = blacklist.iterator();

            System.out.println("\t-------------------------------------------");
            System.out.println("\t------------    我的黑名单   ----------------");
            System.out.println("\t-------------------------------------------");
            while (iter.hasNext()) {
                String name_uid = iter.next();
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
                } else {
                    String name_uid = map.get(choice);
                    if(name_uid == null){
                        System.err.println("输入错误");
                    }else{
                        blacklistSystem(ctx,name_uid);
                    }

                    break;
                }
            }
        }
    }

    public static void blacklistSystem(ChannelHandlerContext ctx,String uid) throws InterruptedException {
        String myUid = Start.uid;
        ctx.channel().writeAndFlush(new UserMessage(myUid));
        Start.semaphore.acquire();
        UserMessage me = Start.friend;

        ctx.channel().writeAndFlush(new UserMessage(uid));
        Start.semaphore.acquire();
        UserMessage friend = Start.friend;

        String t= (Objects.equals(friend.getGander(), "n") ? "男" : (Objects.equals(friend.getGander(), "m") ? "女" : "未知"));

        System.out.println("----------------------------------------");
        System.out.println("uid:" + friend.getUid());
        System.out.println("用户名:" + friend.getName());
        System.out.println("性别:" + t);
        System.out.println("年龄:" + friend.getAge());
        System.out.println("用户创建时间:" + friend.getBuild_time());
        while (true) {
            System.out.println("----------------------------------------");
            System.out.println("1.移除黑名单\t2.返回\t");
            System.out.println("----------------------------------------");
            String choice = new Scanner(System.in).nextLine();//ctrl+d
            if(choice == null){
                continue;
            }
            if(choice.compareToIgnoreCase("1") == 0){
                ReviseMessage rvm = new ReviseMessage();
                rvm.setBlack(1);
                rvm.setUid(Start.uid);
                rvm.setFriend_uid(friend.getUid());
                ctx.channel().writeAndFlush(rvm);
                return;
            }else if(choice.compareToIgnoreCase("2") == 0){
                return;
            }else{
                System.err.println("输入错误");
            }
        }
    }

}
