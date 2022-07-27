package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.FindMessage;
import message.LoadMessage;
import message.ReviseMessage;
import message.UserMessage;

import java.util.Objects;
import java.util.Scanner;

public class MaterialSystem {
    public static void myMaterial(LoadMessage load, ChannelHandlerContext ctx, UserMessage me) throws InterruptedException {
        boolean flag = false;
        String t = (Objects.equals(me.getGander(), "n") ? "男" : (Objects.equals(me.getGander(), "m") ? "女" : "未知"));

        while(true) {
            ctx.channel().writeAndFlush(new UserMessage(load.getUid()));
            Start.count.await();

            System.out.println("---------------------------------------------------------");
            System.out.println("\tuid:" + me.getUid());
            System.out.println("\t用户名:" + me.getName());
            System.out.println("\t性别:" + t);
            System.out.println("\t年龄:" + me.getAge());
            System.out.println("\t用户创建时间:" + me.getBuild_time());
            while (!flag) {
            System.out.println("---------------------------------------------------------");
            System.out.println("\t1.返回\t2.修改密码\t3.修改用户名\n" +
                                "\t4.修改性别选项\t5.修改年龄\t6.再次显示界面");
            System.out.println("---------------------------------------------------------");
                flag = false;
                char choice = (char) new Scanner(System.in).nextByte();
                switch (choice) {
                    case 1:
                        return;
                    case 2:
                        revisePassword(load.getUid(),ctx);
                        break;
                    case 3:
                        reviseName(load.getUid(),ctx);
                        break;
                    case 4:
                        reviseGander(load.getUid(),ctx);
                        break;
                    case 5:
                        reviseAge(load.getUid(),ctx);
                        break;
                    case 6:
                        flag = true;
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
            ctx.channel().writeAndFlush(new FindMessage(uid, oldPassword));
            Start.count.await();
            if (Start.EnterPassword.get()){
                break;
            }else{
                System.err.println("输入错误");
            }
        }
        while(true) {
            System.out.println("请输入新密码");
            newPassword = sc.nextLine();
            System.out.println("请再次输入密码");
            if (sc.nextLine().equals(newPassword)) {
                System.err.println("两次密码不一致");
                continue;
            }
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,null,newPassword,null));
                Start.count.await();
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
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,name,null,null));
                Start.count.await();
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
                Start.count.await();
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
            age = sc.nextInt();
            if(age >= 150){
                System.err.println("？？？");
                continue;
            }
            System.out.println("是否更改(yes/no)");
            String t = sc.nextLine();
            if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                ctx.channel().writeAndFlush(new ReviseMessage(uid,age));
                Start.count.await();
                break;
            }else if(t.compareToIgnoreCase("no") == 0 || t.compareToIgnoreCase("n") == 0){
                break;
            }
        }
    }
}
