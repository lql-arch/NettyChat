package client.System;

import client.SimpleChannelHandler.FindGroupHandler;
import client.SimpleChannelHandler.FindHistoricalNews;
import client.Start;
import client.normal.Chat_record;
import io.netty.channel.ChannelHandlerContext;
import message.FindGroupMessage;
import message.HistoricalNews;
import message.RequestMessage;
import message.UserMessage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

import static client.Start.*;

public class FindSystem {

    public static void FindUid(ChannelHandlerContext ctx) throws IOException, InterruptedException {
        int read = 0;
        byte[] b = new byte[1024];
        System.out.println("请输入想要查询的用户uid：");
        if((read = System.in.read(b)) == -1){
            return;
        }
        String uid = new String(b,0,read-1);
        findPerson(ctx, uid);

    }

    public static void findPerson(ChannelHandlerContext ctx, String uid) throws InterruptedException, IOException {
        String myUid = Start.uid;
        ctx.channel().writeAndFlush(new UserMessage(myUid));
        Start.semaphore.acquire();
        UserMessage me = Start.friend;

        ctx.channel().writeAndFlush(new UserMessage(uid));
        Start.semaphore.acquire();
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
            String choice = new Scanner(System.in).next();
            if(!ChatSystem.isDigit(choice)){
                System.err.println("Error:无此选项,请重新输入.");
                continue;
            }
            int result = Integer.parseInt(choice);
            switch (result) {
                case 1:
                    if( !uidNameMap.isEmpty() && uidNameMap.get(user.getUid()) != null){
                        System.out.println("你们已是好友(按下“Entry”继续)");
                        System.in.read();
                        break;
                    }else {
                        RequestMessage rm = new RequestMessage().setRequestPerson(me).setRecipientPerson(user).setAddOrDelete(true).setFriend(true);
                        ctx.channel().writeAndFlush(rm);
                    }
                case 2:
                    return;
                default:
                    System.err.println("Error:无此选项,请重新输入.");
                    break;
            }
        }
    }

    public static void myHistoricalNews(ChannelHandlerContext ctx, UserMessage me, UserMessage friend) throws InterruptedException {
        String date;
        Scanner sc = new Scanner(System.in);
        Timestamp start;
        Timestamp end;

        while(true) {
            System.out.println("----------------------------------------");
            System.out.println("\t\t\t\t请输入想要查找的时间:(year-month-day),(exit退出)");
            date = sc.nextLine();
            System.out.println("----------------------------------------");
            if(date.compareToIgnoreCase("exit") == 0){
                return;
            }
            try {
                String[] dates = date.split("-");
                start = Timestamp.valueOf(date + " 08:00:00");
                dates[2] = String.valueOf(Integer.parseInt(dates[2]) + 1);
                end = Timestamp.valueOf(dates[0] + "-" + dates[1] + "-" + dates[2] + " 08:00:00");
            }catch (Exception e){
                System.err.println("输入错误");
                continue;
            }

            HistoricalNews hn = new HistoricalNews();
            hn.setStartTime(start.toString());
            hn.setEndTime(end.toString());
            hn.setFriendUid(friend.getUid());
            hn.setUid(uid);
            hn.setPersonOrGroup(true);

            ctx.writeAndFlush(hn);
            Start.semaphore.acquire();

            showHistoricalNews(friend);
        }

    }

    public static void showHistoricalNews(UserMessage friend){
        HistoricalNews hn = FindHistoricalNews.historicalNews;
        Iterator<Chat_record> iter = hn.getChat_record().listIterator();
        Timestamp one = null;
        Timestamp second = null;
        if(hn.getChat_record() == null){
            System.out.println("----------------------------------------");
            System.out.println("null");
            System.out.println("----------------------------------------");
            return;
        }

        System.out.println("----------------------------------------");
        while(iter.hasNext()){
            Chat_record cr = iter.next();
            if(one == null){
                one = cr.getTime();
                System.out.println("\t\t"+one);
            }else{
                one = cr.getTime();
                if(one.getTime() - second.getTime() > 1000*60*60){
                    System.out.println("\t\t"+one);
                }
            }
            //chat.getSend_uid().equals(me.getUid()) ? me.getName() : friend.getName())
            System.out.println((cr.getSend_uid().equals(me.getUid()) ? me.getName() : friend.getName())+":"+cr.getText());
            second = one;
        }
        System.out.println("----------------------------------------");
    }

    public static void findGroup(ChannelHandlerContext ctx) throws InterruptedException {
        System.out.println("----------------------------------------");
        int read;
        byte[] b = new byte[1024];
        System.out.println("请输入想要查询的群的gid：");
        try {
            if ((read = System.in.read(b)) == -1) {
                return;
            }
        } catch (IOException e) {
            return;
        }
        String gid = new String(b, 0, read - 1);

        FindGroupMessage fgm = new FindGroupMessage();
        fgm.setGid(gid);

        ctx.writeAndFlush(fgm);
        semaphore.acquire();

        showGroup(ctx);

    }

    public static void showGroup(ChannelHandlerContext ctx) throws InterruptedException {
        FindGroupMessage fgm = FindGroupHandler.fgm;

        if(fgm.getGroupName() == null && fgm.getBuildTime() == null){
            System.out.println("查无此群");
            return;
        }

        boolean flag = true;
        System.out.println("---------------------------------------------");
        System.out.println("\tgid:"+fgm.getGid());
        System.out.println("\t群名："+fgm.getGroupName());
        System.out.println("\t创建者："+fgm.getGroupMaster()+"("+fgm.getMasterUid()+")");
        System.out.println("\t创建时间"+fgm.getBuildTime());
        System.out.println("\t人数："+fgm.getMembersCount());
        System.out.println("---------------------------------------------");
        System.out.println("\t1.申请加入群聊\t2.返回");
        System.out.println("---------------------------------------------");

        while(true) {
            String choice = new Scanner(System.in).nextLine();
            switch (choice){
                case "1":
                    RequestGroup(ctx,fgm);
                case "2":
                    return;
                default:
                    System.err.println("输入错误");
                    break;
            }
        }

    }

    public static void RequestGroup(ChannelHandlerContext ctx,FindGroupMessage fgm) throws InterruptedException {
        RequestMessage rm = new RequestMessage();
        rm.setGid(fgm.getGid())
                .setGroupORSingle(true)
                .setRequestPerson(new UserMessage(uid))
                .setAddOrDelete(true)
                .setClearMsg(false)
                .setFriend(true);

        ctx.channel().writeAndFlush(rm);
    }

}