package client.System;

import client.SimpleChannelHandler.*;
import client.Start;
import client.normal.Chat_group;
import client.normal.GroupChat_text;
import config.ToMessage;
import config.execToVerify;
import io.netty.channel.ChannelHandlerContext;
import message.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static client.Start.*;
import static client.Start.uid;
import static client.System.ChatSystem.friendMaterial;
import static client.System.ChatSystem.isDigit;

public class GroupSystem {
    public static AtomicBoolean groupChat = new AtomicBoolean(false);
    public static volatile Map<String,List<GroupChat_text>> groupChat_texts = new HashMap<>();
    public static Map<String,Boolean> groupIsRead = new HashMap<>();
    private static String path1;

    public static GroupChat_text addGroupChat_texts(String uid,String gid,String time,String name,String text){
        GroupChat_text groupChat_text = new GroupChat_text();
        groupChat_text.setText(text);
        groupChat_text.setUid(uid);
        groupChat_text.setDate(time);
        groupChat_text.setMyName(name);
        groupChat_text.setGid(gid);

        return groupChat_text;
    }

    @NotNull
    public static LoadGroupMessage getLoadGroupMessage(Chat_group cg) {
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
    public static void groupSystem(ChannelHandlerContext ctx) throws Exception {
        while(true) {
            boolean flag = true;
            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t输入\"exit\"返回");
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
                if (choice.compareToIgnoreCase("exit") == 0) {
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

    public static void showGroupSystem(ChannelHandlerContext ctx,int type) throws Exception {

        while(true) {
            ctx.channel().writeAndFlush(new LoginStringMessage("group!" + uid));
            semaphore.acquire();

            Map<Integer, Chat_group> countMap = new HashMap<>();
            int count = 1;
            List<Chat_group> cgs = groupLoad.getGroup();
            Iterator<Chat_group> iter = cgs.listIterator();


            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t输入\"exit\"返回");
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
                if (choice.compareToIgnoreCase("exit") == 0) {
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
                }else{
                    System.err.println("输入错误");
                }
            }
        }
    }



    public static void showChatGroup(ChannelHandlerContext ctx,LoadGroupMessage lgm) throws Exception {
        while(true) {
            ctx.channel().writeAndFlush(lgm);
            Start.semaphore.acquire();
            lgm = LoadGroupNewsHandler.groupMessage;

            boolean flag = true;
            System.out.println("---------------------------------------------");
            System.out.println("\tgid:" + lgm.getGid());
            System.out.println("\t群名：" + lgm.getGroupName());
            System.out.println("\t创建者：" + lgm.getMasterName() + "(" + lgm.getGroup_master() + ")");
            System.out.println("\t创建时间" + lgm.getTime());
            System.out.println("\t人数：" + lgm.getMembersCount());
            System.out.print("---------------------------------------------\n");
            System.out.print("\t1.进入群聊\t2.查看群历史记录\t3.群文件\n\t4.查看群成员\t5.返回\n");
            if (lgm.getGroup_master().compareTo(uid) == 0)
                System.out.println("\t6.禁言群员\t7.解散群聊");
            else if (lgm.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0))
                System.out.println("\t6.禁言群员\t8.退出该群");
            else
                System.out.println("\t8.退出该群");
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
                        verifyHistoricalNews(ctx,lgm);
                        break;
                    case 3:
                        GroupFiles(ctx, lgm);
                        break;
                    case 4:
                        viewGroupMembers(ctx, lgm);
                        break;
                    case 5:
                        return;
                    case 6:
                        if (lgm.getGroup_master().compareTo(uid) == 0 || lgm.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)) {
                            bannedMembers(ctx, lgm);
                            break;
                        }
                    case 7:
                        if (lgm.getGroup_master().compareTo(uid) == 0) {
                            disbandTheGroupChat(ctx, lgm);
                            return;
                        }
                    case 8:
                        RequestMessage rm = new RequestMessage();
                        rm.setGid(lgm.getGid())
                                .setGroupORSingle(true)
                                .setRequestPerson(new UserMessage(uid))
                                .setAddOrDelete(false)
                                .setClearMsg(false)
                                .setFriend(false)
                                .setConfirm(false);

                        ctx.channel().writeAndFlush(rm);
                        System.out.println("输入“Entry”继续");
                        new Scanner(System.in).nextLine();
                        return;
                    default:
                        flag = true;
                        System.err.println("输入错误");
                        break;
                }
            }
        }
    }

    public static void GroupMsg(ChannelHandlerContext ctx) throws InterruptedException, IOException {
        boolean flag = true;
        ctx.writeAndFlush(new LoginStringMessage("group!"+uid));
        semaphore.acquire();
        String notice = groupLoad.getHasRequest() >= 4 ? "new" : "0";
        String request = (groupLoad.getHasRequest() == 2 || groupLoad.getHasRequest() ==3 || groupLoad.getHasRequest() == 5 || groupLoad.getHasRequest() == 7) ? "new" : "0";
        System.out.println("---------------------------------------------");
        System.out.println("\t\t\t1.群通知("+notice+")");
        System.out.println("\t\t\t2.群申请("+request+")");
        System.out.println("\t\t\t3.返回");
        System.out.println("---------------------------------------------");

        while(flag) {
            flag = false;
            String choice = new Scanner(System.in).nextLine();
            if (!isDigit(choice)) {
                System.err.println("输入错误");
                continue;
            }
            switch (Integer.parseInt(choice)){
                case 1:
                    groupNotice(ctx);
                    break;
                case 2:
                    groupRequest(ctx);
                    break;
                case 3:
                    return;
                default:
                    flag = true;
                    System.err.println("输入错误");
                    break;
            }
        }
    }

    public static void groupNotice(ChannelHandlerContext ctx) throws InterruptedException, IOException {
        GroupNoticeMessage gnm = new GroupNoticeMessage();
        gnm.setUid(uid);
        gnm.setRequestOrNo(false);
        ctx.writeAndFlush(gnm);
        semaphore.acquire();

        gnm = GroupNoticeHandler.gnm;
        Iterator<GroupNoticeMessage.Notice> notices = gnm.getNotices().listIterator();
        Timestamp one = null,second = null;
        System.out.println("---------------------------------------------");
        while(notices.hasNext()){
            GroupNoticeMessage.Notice iter = notices.next();
            if(one == null) {
                one = Timestamp.valueOf(iter.getTime());
                System.out.println(one);
            }else {
                one = Timestamp.valueOf(iter.getTime());
                if (one.getTime() - second.getTime() > 60 * 1000 * 5) {
                    System.out.println(one);
                }
            }
            System.out.println(iter.getGid()+":"+iter.getNotice());
            second = one;
        }
        System.out.println("---------------------------------------------");

        System.out.println("按下“Entry”继续");
        new Scanner(System.in).nextLine();

    }

    public static void groupRequest(ChannelHandlerContext ctx) throws InterruptedException {
        while(true) {
            GroupNoticeMessage gnm = new GroupNoticeMessage();
            gnm.setUid(uid);
            gnm.setRequestOrNo(true);
            ctx.writeAndFlush(gnm);
            semaphore.acquire();

            gnm = GroupNoticeHandler.gnm;
            int count = 1;
            Map<Integer, GroupNoticeMessage.Notice> countMap = new HashMap<>();
            Iterator<GroupNoticeMessage.Notice> notices = gnm.getNotices().listIterator();
            System.out.println("---------------------------------------------");
            while (notices.hasNext()) {
                GroupNoticeMessage.Notice iter = notices.next();

                countMap.put(count, iter);
                System.out.println(Timestamp.valueOf(iter.getTime()));
                System.out.println((count++) + "." + iter.getGid() + ":" + iter.getNotice());
            }
            System.out.println("---------------------------------------------");
            System.out.println("输入“EXIT”退出");
            String choice;
            while(true){
                if ((choice = new Scanner(System.in).nextLine()) != null) {
                    if(choice.compareToIgnoreCase("EXIT") == 0){
                        return;
                    }
                    if(!isDigit(choice)){
                        System.err.println("输入错误");
                        continue;
                    }
                    int result = Integer.parseInt(choice);
                    if(result > 0 && result < count){
                        GroupNoticeMessage.Notice notice = countMap.get(result);
                        RequestMessage rm = new RequestMessage();
                        rm.setGid(notice.getGid())
                                .setGroupORSingle(true)
                                .setRequestPerson(new UserMessage(notice.getSender_uid()))
                                .setAddOrDelete(true)
                                .setClearMsg(false)
                                .setFriend(false)
                                .setConfirm(true);

                        ctx.channel().writeAndFlush(rm);

                        break;
                    }else{
                        System.err.println("输入错误");
                    }
                }
            }
        }
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

    public static void enterGroupChat(ChannelHandlerContext ctx,LoadGroupMessage msg) throws InterruptedException {
        ctx.channel().writeAndFlush(msg);
        Start.semaphore.acquire();

        LoadGroupMessage lgm = LoadGroupNewsHandler.groupMessage;
        String str;
        Timestamp date = Timestamp.valueOf(LocalDateTime.now());
        try{
            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t" + msg.getGroupName() + "(输入EXIT退出)\t");
            System.out.println("---------------------------------------------");
            showHistory(ctx,lgm, msg.getGid());
            groupChat.compareAndSet(false, true);
            if(!lgm.getUidBanned().get(uid)) {
                while ((str = new Scanner(System.in).nextLine()).compareToIgnoreCase("exit") != 0) {
                    date = Timestamp.valueOf(LocalDateTime.now());
                    GroupChat_text text = addGroupChat_texts(lgm.getUid(), lgm.getGid(), date.toString(), lgm.getUidNameMap().get(lgm.getUid()), str);
                    ctx.writeAndFlush(new GroupStringMessage().setText(text));
                }
            }else{
                System.out.println("你已被禁言！");
                while ((str = new Scanner(System.in).nextLine()).compareToIgnoreCase("exit") != 0) {
                    System.out.println("你已被禁言！");
                }
            }
            groupChat.compareAndSet(true, false);
            System.out.println("---------------------------------------------");
        }finally {
            ctx.writeAndFlush(new GroupStringMessage().setReviseLastTime(true).setGid(lgm.getGid()).setUid(uid).setTime(date.toString()));//修改last_time
        }
    }

    public static void showHistory(ChannelHandlerContext ctx,LoadGroupMessage msg,String gid) throws InterruptedException {
        ctx.channel().writeAndFlush(msg);
        Start.semaphore.acquire();

        msg = LoadGroupNewsHandler.groupMessage;
        Timestamp one = null,second = null;
        if(groupIsRead.get(gid) == null){
            groupIsRead.put(gid,true);
            List<GroupChat_text> messages = msg.getGroupMessages();
            if(messages == null){
                messages = new ArrayList<>();
            }

            synchronized (GroupSystem.class) {
                List<GroupChat_text> t;
                if ((t = groupChat_texts.get(gid)) != null && !messages.isEmpty()) {
                    GroupChat_text gct = messages.get(messages.size() - 1);
                    for (GroupChat_text groupChat_text : t) {
                        if (Timestamp.valueOf(gct.getDate()).before(Timestamp.valueOf(groupChat_text.getDate()))) {
                            messages.add(groupChat_text);
                        }
                    }
                    groupChat_texts.put(gid, messages);
                } else {
                    groupChat_texts.put(gid, messages);
                }
            }
        }
        for(GroupChat_text gct : groupChat_texts.get(gid)){
            if(one == null){
                one = Timestamp.valueOf(gct.getDate());
                System.out.println(one);
            }else{
                one = Timestamp.valueOf(gct.getDate());
                if(one.getTime() - second.getTime() >= 5*60*1000){
                    System.out.println(one);
                }
            }
            if(gct.getUid().equals(msg.getGroup_master())){
                System.out.println(msg.getMasterName()+":"+gct.getText());
            }else
             System.out.println(gct.getMyName()+":"+gct.getText());
            second = one;
        }

    }

    public static void GroupFiles(ChannelHandlerContext ctx,LoadGroupMessage msg) throws Exception {
        while (true) {
        System.out.println("---------------------------------------------");
        System.out.println("\t1.发送群文件\t2.查看群文件\t3.返回");
        System.out.println("---------------------------------------------");
            String choice = new Scanner(System.in).nextLine();
            if (!isDigit(choice)) {
                System.err.println("输入错误");
                continue;
            }
            switch (Integer.parseInt(choice)){
                case 1:
                    sendGroupFile(ctx,msg);
                    break;
                case 2:
                    viewGroupFile(ctx,msg);
                    break;
                case 3:
                    return;
                default:
                    System.err.println("查无此选项");
                    break;
            }

        }
    }

    public static void viewGroupFile(ChannelHandlerContext ctx,LoadGroupMessage msg) throws Exception {
        while (true) {
            FileRead fileRead = new FileRead();
            ctx.writeAndFlush(fileRead.setUid(uid).setGid(msg.getGid()).setSingleOrGroup(false));
            semaphore.acquire();
            ctx.channel().writeAndFlush(msg);
            Start.semaphore.acquire();
            msg = LoadGroupNewsHandler.groupMessage;

            fileRead = FileReadHandler.fileRead;
            Map<String, String> time = fileRead.getFileTimeMap();
            Map<Integer, String> countMap = new HashMap<>();
            int count = 1;

            System.out.println("------------------------------------------------------------------------------------------------------------");
            System.out.println("\t\t\t输入\"exit\"返回");
            System.out.println("------------------------------------------------------------------------------------------------------------");
            System.out.printf("%5s %50s %20s\t%20s\n", "id", "file_name", "file_sender", "file_time");
            if(fileRead.getFilePersonMap() == null || fileRead.getFileTimeMap() == null){
                System.out.println("------------------------------------------------------------------------------------------------------------");
                return;
            }
            for (Map.Entry<String, String> person : fileRead.getFilePersonMap().entrySet()) {
                String name = msg.getUidNameMap().get(person.getValue());
                countMap.put(count, person.getKey());
                System.out.printf("%5d %50s %20s\t%20s\n", count++, person.getKey(), name, time.get(person.getKey()));
            }
            System.out.println("------------------------------------------------------------------------------------------------------------");
            if(msg.getGroup_master().compareTo(uid) == 0 || msg.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)){
                System.out.println("输入“delete”进入删除界面");
                System.out.println("------------------------------------------------------------------------------------------------------------");
            }

            boolean flag = true;
            String choice = new Scanner(System.in).nextLine();
            if (choice.compareToIgnoreCase("exit") == 0) {
                return;
            }
            if(choice.compareToIgnoreCase("delete") == 0 && (msg.getGroup_master().compareTo(uid) == 0 || msg.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0))){
                deleteFile(ctx,time,fileRead,msg,count,countMap);
                break;
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
                fm.setMyUid(uid);
                fm.setUid(fileRead.getFilePersonMap().get(name));
                fm.setGid(msg.getGid());
                fm.setStartPos(0);
                fm.setPath(null);//标志物
                fm.setPerson(false);

                if(FileMsgHandler.file_dir == null){
                    while(true) {
                        System.out.println("请设置文件接收地址（绝对地址）(输入“exit”退出)：");
                        String dir = new Scanner(System.in).nextLine();
                        if(dir.compareToIgnoreCase("exit") == 0){
                            flag = false;
                            break;
                        }
                        FileMsgHandler.file_dir = dir;
                        File folder = new File(FileMsgHandler.file_dir);
                        if (!folder.exists() && !folder.isDirectory()) {
                            System.out.println("地址不存在");
                            continue;
                        }
                        break;
                    }
                }else{
                    System.out.println("是否修改接收路径（yes/no）(输入“exit”退出)(上次路径："+FileMsgHandler.file_dir+")");
                    String t = new Scanner(System.in).next();
                    if(t.compareToIgnoreCase("exit") == 0){
                        break;
                    }
                    if(t.compareToIgnoreCase("yes") == 0 || t.compareToIgnoreCase("y") == 0){
                        while(true) {
                            System.out.println("请设置文件接收地址（绝对地址）(输入“exit”退出)：");
                            t = new Scanner(System.in).next();
                            if(t.compareToIgnoreCase("exit") == 0){
                                flag = false;
                                break;
                            }
                            FileMsgHandler.file_dir = t;
                            File folder = new File(FileMsgHandler.file_dir);
                            if (!folder.exists() && !folder.isDirectory()) {
                                System.out.println("地址不存在");
                                continue;
                            }
                            break;
                        }
                    }
                }
                if(!flag){
                    break;
                }

                path1 = FileMsgHandler.file_dir + File.separator + name;

                ctx.writeAndFlush(fm.setReadOrWrite(true));

                semaphore.acquire();

                if(execToVerify.equal(FileMsgHandler.sum,path1)){
                    System.out.println("sha1sum值正确");
                }else{
                    System.out.println("sa1sum值错误，请重试传输，或通知人员修复");
                }

                break;
            }else{
                System.err.println("查无此选项");
            }
        }
    }

    public static void deleteFile(ChannelHandlerContext ctx, Map<String, String> time, FileRead fileRead, LoadGroupMessage msg, int count, Map<Integer, String> countMap) throws InterruptedException {
        System.out.println("请输入想要删除的文件序列:");
        while(true) {
            String choice = new Scanner(System.in).nextLine();
            if (choice.compareToIgnoreCase("exit") == 0) {
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
                fm.setMyUid(uid);
                fm.setUid(fileRead.getFilePersonMap().get(name));
                fm.setGid(msg.getGid());
                fm.setPerson(false);

                ctx.writeAndFlush(fm.setDeleteFile(true));
                semaphore.acquire();
                return;
            }
        }
    }

    public static void sendGroupFile(ChannelHandlerContext ctx,LoadGroupMessage msg) throws Exception {
        System.out.println("-------------------------------------");
        System.out.println("\t\t\t\t传输文件");
        System.out.println("-------------------------------------");
        System.out.println("请输入您要传输的文件地址：");
        String pass = new Scanner(System.in).nextLine();
        File file = new File(pass);
        if(!file.exists()){
            if (!file.isFile()) {
                System.out.println("Not a file :" + file);
                return;
            }
        }

        FileMessage fm = new FileMessage();
        fm.setStartPos(0);

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {
            randomAccessFile.seek(fm.getStartPos());
            int length = (int) ((file.length() / 10) < 1024*1024*2 ? (file.length() / 10) : 1024*1024*2);
            byte[] bytes = new byte[length];
            int read;
            fm.setName(file.getName());
            fm.setFileLen(file.length());
            fm.setPath(file.getPath());
            fm.setSha1sum(execToVerify.sha1Verify(file.getPath()));
            if((read = randomAccessFile.read(bytes)) != -1){
                fm.setEndPos(read);
                fm.setGid(msg.getGid());
                fm.setMyUid(uid);
                fm.setPerson(false);
                fm.setReadOrWrite(false);
                fm.setTime(Timestamp.valueOf(LocalDateTime.now()).toString());

                saveFile.saveFileStart(fm,fm.getStartPos() == 0);

                fm.setBytes(bytes);//byte不写入存储文件

                System.out.println("等待文件发送完毕");
                ctx.writeAndFlush(fm.setFirst(true));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        semaphore.acquire();

    }

    public static void viewGroupMembers(ChannelHandlerContext ctx,LoadGroupMessage msg) throws IOException, InterruptedException {
        while(true) {
            ctx.writeAndFlush(msg);
            semaphore.acquire();
            msg = LoadGroupNewsHandler.groupMessage;

            int count = 1;
            Map<Integer,String> countMap = new HashMap<>();

            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t members:" + msg.getMembersCount());
            System.out.println("---------------------------------------------");
            System.out.println("\t群主:");
            countMap.put(count, msg.getGroup_master());
            System.out.printf("\t%d.%20s\n", count++, msg.getMasterName());
            System.out.println("\t管理员:");
            for (String aUid : msg.getAdministrator()) {
                String name = msg.getUidNameMap().get(aUid);
                countMap.put(count, aUid);
                System.out.printf("\t%d.%20s(%3s)\n", count++, name,msg.getUidBanned().get(aUid) ? "已禁言" : "未禁言");
            }
            System.out.println("\t群员:");
            for (String mUid : msg.getMembers()) {
                String name = msg.getUidNameMap().get(mUid);
                countMap.put(count, mUid);
                System.out.printf("\t%d.%20s(%3s)\n", count++, name,msg.getUidBanned().get(mUid) ? "已禁言" : "未禁言");
            }

            while (true) {
                System.out.println("---------------------------------------------");
                System.out.println("\t\t\t 输入“exit”退出");
                if(msg.getGroup_master().compareTo(uid) == 0 || msg.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)){
                    System.out.println("\t\t\t输入“manage”进入管理界面");
                }
                System.out.println("---------------------------------------------");

                String choice = new Scanner(System.in).nextLine();
                if (choice.compareToIgnoreCase("exit") == 0) {
                    return;
                }
                if(choice.compareToIgnoreCase("manage") == 0) {
                    if (msg.getGroup_master().compareTo(uid) == 0) {//群主
                        groupMembersManage(ctx,msg,1,count,countMap);
                        break;
                    }
                    if (msg.getAdministrator().stream().anyMatch(a -> a.compareTo(uid) == 0)) {//管理
                        groupMembersManage(ctx,msg,2,count,countMap);
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

    public static void groupMembersManage(ChannelHandlerContext ctx,LoadGroupMessage msg,int type,int max,Map<Integer,String> countMap) throws InterruptedException {
        String uid;
        int min = 0;
        int result;

        System.out.println("请输入想要处理的序号");
        while(true) {
            String choice = new Scanner(System.in).nextLine();
            if (choice.compareToIgnoreCase("exit") == 0) {
                return;
            }
            if (!isDigit(choice)) {
                System.err.println("输入错误");
                continue;
            }
            result = Integer.parseInt(choice);
            if(type == 1)
                min = 1;
            else
                min = 1 + msg.getAdministrator().size();
            if (result > min && result < max) {
                uid = countMap.get(result);
                break;
            }else if(result <= min && result > 0){
                System.out.println("您没有权限对此作出更改.");
            }else{
                System.err.println("输入错误");
            }
        }
        boolean isAdm = result <= 1 + msg.getAdministrator().size();
        if(type == 1){
            System.out.println("---------------------------------------------");
            System.out.println("1.移除群聊\t"+ ( isAdm ? "2.移除管理":"2.设为管理")+"\t3.返回");
            System.out.println("---------------------------------------------");
        }else if(type == 2){
            System.out.println("---------------------------------------------");
            System.out.println("1.移除群聊\t2/3.返回");
            System.out.println("---------------------------------------------");
        }

        while(true) {
            String c = new Scanner(System.in).nextLine();
            if (!isDigit(c)) {
                System.err.println("输入错误");
            }
            switch (Integer.parseInt(c)){
                case 1:
                     removeGroup(ctx,uid,msg,type);
                     return;
                case 2:
                    if(type == 1) {
                        if ( result > 1 + msg.getAdministrator().size()) {
                            setManager(ctx, uid, msg,false);
                        } else if (isAdm) {
                            setManager(ctx,uid,msg,true);
                        }
                    }
                case 3:
                    return;
                default:
                    System.err.println("输入错误");
                    break;
            }
        }
    }

    @NotNull
    private static ReviseGroupMemberMessage getReviseGroupMemberMessage(String uid, LoadGroupMessage msg, boolean setManage, boolean removeGroup,boolean disbandGroupChat,boolean removeAdm) {
        ReviseGroupMemberMessage rgmm = new ReviseGroupMemberMessage();
        rgmm.setUid(uid);
        rgmm.setGid(msg.getGid());
        rgmm.setSetManage(setManage);
        rgmm.setRemoveGroup(removeGroup);
        rgmm.setManageUid(Start.uid);
        rgmm.setDisbandGroupChat(disbandGroupChat);
        rgmm.setRemoveAdm(removeAdm);
        return rgmm;
    }

    public static void removeGroup(ChannelHandlerContext ctx, String uid, LoadGroupMessage msg, int type) throws InterruptedException {
        ctx.writeAndFlush(msg);
        semaphore.acquire();
        msg = LoadGroupNewsHandler.groupMessage;

        if(!msg.getGroup_master().equals(Start.uid) && msg.getAdministrator().stream().noneMatch(a -> a.compareTo(Start.uid) == 0)){
            System.err.println("你已被取消管理员身份");
            return;
        }

        ReviseGroupMemberMessage rgmm = getReviseGroupMemberMessage(uid, msg, false, true,false,false);

        ctx.writeAndFlush(rgmm);
        semaphore.acquire();

        if(!msg.getMembers().remove(uid) && type == 1)
            msg.getAdministrator().remove(uid);

    }

    public static void setManager(ChannelHandlerContext ctx,String uid,LoadGroupMessage msg,boolean remove) throws InterruptedException {
        ctx.writeAndFlush(msg);
        semaphore.acquire();
        msg = LoadGroupNewsHandler.groupMessage;

        if( !msg.getGroup_master().equals(Start.uid) && msg.getAdministrator().stream().noneMatch(a -> a.compareTo(Start.uid) == 0)){
            System.err.println("你已被取消管理员身份");
            return;
        }

        ReviseGroupMemberMessage rgmm = getReviseGroupMemberMessage(uid, msg, true, false,false,remove);

        ctx.writeAndFlush(rgmm);
        semaphore.acquire();

        if(!remove) {
            msg.getAdministrator().add(uid);
            msg.getMembers().remove(uid);
        }else{
            msg.getAdministrator().remove(uid);
            msg.getMembers().add(uid);
        }
    }



    public static void bannedMembers(ChannelHandlerContext ctx,LoadGroupMessage msg) throws InterruptedException {
        while(true) {
            ctx.writeAndFlush(msg);
            semaphore.acquire();
            msg = LoadGroupNewsHandler.groupMessage;

            int count = 1;
            Map<Integer, String> countMap = new HashMap<>();

            System.out.println("---------------------------------------------");
            System.out.println("\t\t\t members:" + msg.getMembersCount());
            System.out.println("---------------------------------------------");
            if(msg.getGroup_master().compareTo(uid) == 0) {
                System.out.println("\t管理员:");
                for (String aUid : msg.getAdministrator()) {
                    String name = msg.getUidNameMap().get(aUid);
                    countMap.put(count, aUid);
                    System.out.printf("\t%d.%20s(%3s)\n", count++, name,msg.getUidBanned().get(aUid) ? "已禁言" : "未禁言");
                }
            }
            System.out.println("\t群员:");
            for (String mUid : msg.getMembers()) {
                String name = msg.getUidNameMap().get(mUid);
                countMap.put(count, mUid);
                System.out.printf("\t%d.%20s(%3s)\n", count++, name,msg.getUidBanned().get(mUid) ? "已禁言" : "未禁言");
            }
            System.out.println("---------------------------------------------");

            while(true){
                System.out.println("请输入想要禁言/解除禁言对象的序号:(禁言/解除禁言全体输入“all”)(输入\"exit\"退出)");
                String choice = new Scanner(System.in).nextLine();
                if(choice.compareToIgnoreCase("exit") == 0){
                    return;
                }
                if(choice.compareToIgnoreCase("all") == 0){
                    ctx.writeAndFlush(new GroupStringMessage().setGid(msg.getGid()).setBanned(true).setUid(null));//无uid即禁言全体
                    semaphore.acquire();
                    break;
                }
                if(!isDigit(choice)){
                    System.err.println("输入错误");
                    continue;
                }
                int result = Integer.parseInt(choice);
                if(result > 0 && result < count){
                    ctx.writeAndFlush(new GroupStringMessage().setGid(msg.getGid()).setUid(countMap.get(result)).setBanned(true));
                    semaphore.acquire();
                }else{
                    System.err.println("查无此选项");
                }
            }
        }
    }

    public static void disbandTheGroupChat(ChannelHandlerContext ctx,LoadGroupMessage msg) throws InterruptedException {
        System.out.println("您真的确定要解散群聊名？（yes/no）");
        String result = new Scanner(System.in).nextLine();
        if(result.compareToIgnoreCase("yes") == 0 || result.compareToIgnoreCase("y") == 0 ){
            ReviseGroupMemberMessage rgmm = getReviseGroupMemberMessage(uid,msg,false,false,true,false);
            ctx.writeAndFlush(rgmm);
            semaphore.acquire();
        }
    }

    public static void verifyHistoricalNews(ChannelHandlerContext ctx,LoadGroupMessage msg) throws InterruptedException {
        String date;
        Scanner sc = new Scanner(System.in);
        Timestamp start;
        Timestamp end;

        while(true) {
            System.out.println("----------------------------------------");
            System.out.println("\t\t\t\t请输入想要查找的时间:(year-month-day),(exit退出)");
            date = sc.nextLine();
            System.out.println("----------------------------------------");
            if (date.compareToIgnoreCase("exit") == 0) {
                return;
            }
            try {
                String[] dates = date.split("-");
                start = Timestamp.valueOf(date + " 08:00:00");
                dates[2] = String.valueOf(Integer.parseInt(dates[2]) + 1);
                end = Timestamp.valueOf(dates[0] + "-" + dates[1] + "-" + dates[2] + " 08:00:00");
            } catch (Exception e) {
                System.err.println("输入错误");
                continue;
            }

            HistoricalNews hn = new HistoricalNews();
            hn.setStartTime(start.toString());
            hn.setEndTime(end.toString());
            hn.setGid(msg.getGid());
            hn.setUid(uid);
            hn.setPersonOrGroup(false);

            ctx.writeAndFlush(hn);
            Start.semaphore.acquire();

            showGroupHistory();
        }
    }

    public static void showGroupHistory(){
        HistoricalNews hn = FindHistoricalNews.groupHistoricalNews;
        Iterator<GroupChat_text> iter = hn.getGroupChat_texts().listIterator();
        Timestamp one = null;
        Timestamp second = null;
        if(hn.getGroupChat_texts() == null){
            System.out.println("----------------------------------------");
            System.out.println("null");
            System.out.println("----------------------------------------");
            return;
        }

        System.out.println("----------------------------------------");
        while(iter.hasNext()){
            GroupChat_text gct = iter.next();
            if(one == null){
                one = Timestamp.valueOf(gct.getDate());
                System.out.println("\t\t"+one);
            }else{
                one = Timestamp.valueOf(gct.getDate());
                if(one.getTime() - second.getTime() > 1000*60*60){
                    System.out.println("\t\t"+one);
                }
            }
            System.out.println(gct.getMyName()+":"+gct.getText());
            second = one;
        }
        System.out.println("----------------------------------------");
    }

}
