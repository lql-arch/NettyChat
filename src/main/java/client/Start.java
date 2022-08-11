package client;

import client.SimpleChannelHandler.*;
import client.System.*;
import config.Decode;
import config.Encode;
import config.FrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import message.*;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Start {
    private LoginMessage login;
    private static AtomicBoolean flag = new AtomicBoolean(false);
    public static int unread_message;
    public static volatile LoadMessage load;
    public static LoadMessage singleLoad;
    public static LoadMessage groupLoad;
    public static volatile UserMessage friend;
    public static Semaphore semaphore = new Semaphore(0);
    public static AtomicBoolean EnterPassword = new AtomicBoolean(true);
    public static String uid ;// myUid
    public static List<StringMessage> message = new ArrayList<>();//登录后的未读消息
    public static AtomicBoolean singleFlag = new AtomicBoolean(false);
    public static Map<String,String> uidNameMap = new HashMap<>();
    public static UserMessage me = new UserMessage();


    public void Begin() throws InterruptedException {
        Bootstrap boot = new Bootstrap();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ChannelFuture channelFuture = boot.group(worker)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Decode()).addLast(new Encode());
                            ch.pipeline().addFirst(new FrameDecoder());
                            ch.pipeline().addLast(new IdleStateHandler(0,20,0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new IdleHandler());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginStringMessage>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    extracted(ctx);
                                    super.channelActive(ctx);
                                }
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginStringMessage msg) throws Exception {
                                    String str = msg.getMessage();
                                    String[] strings = str.split("!");
                                    if (strings[0].startsWith("password error")) {
                                        System.out.println(str);
                                        ctx.channel().writeAndFlush(new LoginStringMessage("err!"));
                                        ctx.channel().close();
                                    }
                                    if(str.startsWith("register success")){
                                        System.out.println(strings[1]);
                                    }
                                    if(str.startsWith("login success")){
                                        System.out.println("登录成功！");
                                        uid = login.getUid();
                                        ctx.channel().writeAndFlush(new LoginStringMessage("start!"+login.getUid()));
                                    }
                                    if(str.startsWith("someone is online")){
                                        System.out.println("已有人在线");
                                        uid = login.getUid();
                                        ctx.channel().writeAndFlush(new LoginStringMessage("start!"+login.getUid()));
                                    }
                                    if(str.startsWith("you have been pushed off the line")){
                                        System.err.println("你已被挤下线");
                                    }
                                }

                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                    if(!flag.get()){
                                        extracted(ctx);
                                    }
                                    super.channelReadComplete(ctx);
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("你已下线");
                                    ctx.channel().close();
                                    super.channelInactive(ctx);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                    ctx.channel().close();
                                    worker.shutdownGracefully();
                                    System.exit(0);
                                    super.exceptionCaught(ctx, cause);
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoadMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoadMessage msg) throws Exception {
                                    if(msg.getStatus() == 0) {
                                        load = msg;
                                        unread_message = msg.getUnread_message();
                                        uidNameMap = load.getUidNameMap();
                                        new Thread(() -> {
                                            try {
                                                main_menu(ctx);
                                                ctx.channel().close();
                                            } catch (Exception e) {
//                                                System.err.println("main_menu exception end:"+e);
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    }
                                    if(msg.getStatus() == 1){
                                        singleLoad = msg;
                                        semaphore.release();
                                    }
                                    if(msg.getStatus() == 2){
                                        groupLoad = msg;
                                        semaphore.release();
                                    }
                                    if(msg.getStatus() == 3){//flush
                                        load = msg;
                                        unread_message = msg.getUnread_message();
                                        uidNameMap = load.getUidNameMap();
                                        semaphore.release();
                                    }
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<UserMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, UserMessage msg) throws Exception {
                                    friend = msg;
                                    semaphore.release();
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<FindMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FindMessage msg) throws Exception {
                                    EnterPassword.compareAndSet(EnterPassword.get(),msg.getResult());
                                    semaphore.release();
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ReviseMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ReviseMessage msg) throws Exception {
                                    if(msg.getUid().equals(uid) && msg.isResult()){
                                        System.out.println("修改成功！");
                                    }else{
                                        System.out.println("修改失败");
                                    }
                                    DeleteSystem.semaphoreFriend.release();
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<StringMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, StringMessage msg) throws Exception {
                                    if(singleFlag.get() && msg.getMe().getUid().compareTo(SendMessageSystem.send[1]) == 0) {
                                        System.out.println(msg.getMe().getName() + ":" + msg.getMessage());
                                    }else if(msg.isDirect()){
                                        System.out.println(msg.getMessage());
                                    }
                                    message.add(msg);
                                }
                            });
                            ch.pipeline().addLast(new RequestHandler());
                            ch.pipeline().addLast(new FileMsgHandler());
                            ch.pipeline().addLast(new FileReadHandler());
                            ch.pipeline().addLast(new FindHistoricalNews());
                            ch.pipeline().addLast(new LoadGroupNewsHandler());
                            ch.pipeline().addLast(new FindGroupHandler());
                            ch.pipeline().addLast(new ReviseGroupMemberHandler());
                            ch.pipeline().addLast(new GroupNoticeHandler());
                            ch.pipeline().addLast(new GroupStringHandler());
                            ch.pipeline().addLast(new ShowHandler());
                            ch.pipeline().addLast(new LoginHandler());

                        }
                    }).connect("192.168.30.100", 8100);

            channelFuture.sync();
            Channel channel = channelFuture.channel();

            channel.closeFuture().sync();

        }finally{
            worker.shutdownGracefully();
        }

    }

    private  void extracted(ChannelHandlerContext ctx) throws Exception {
        boolean error = true;
        while(error) {
            error = false;
            String tmp;
            System.out.println("\t------------------------------------\t");
            System.out.println("\t---------       1.登录     \t--------\t");
            System.out.println("\t--------- 2.快速登录(已弃用) \t--------\t");
            System.out.println("\t---------       3.注册     \t--------\t");
            System.out.println("\t---------       4.退出     \t--------\t");
            System.out.println("\t------------------------------------\t");
            try {
                tmp = new Scanner(System.in).nextLine();
            }catch (Exception e){
                error = true;
                System.err.println("输入错误");
                continue;
            }
            switch (tmp) {
                case "1":
                    login = LoginMessage.LoginUser();
                    ctx.channel().writeAndFlush(login);
                    flag.compareAndSet(false, true);
                    break;
                case "2":
                    break;
                case "3":
                    if((login = LoginMessage.register()) == null) {
                        error = true;
                        break;
                    }
                    ctx.channel().writeAndFlush(login);
                    break;
                case "4":
                    ctx.channel().close();
                    break;
                default:
                    error = true;
                    System.err.println("输入错误");
            }
        }
    }

    private void main_menu(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(new LoginStringMessage("singleLoad!"+uid));
        semaphore.acquire();
        ctx.writeAndFlush(new LoginStringMessage("group!"+uid));
        semaphore.acquire();
        ctx.channel().writeAndFlush(new UserMessage(uid));
        semaphore.acquire();
        me = friend;
        while(true) {
            ctx.channel().writeAndFlush(new LoginStringMessage("flush!"+uid));
            semaphore.acquire();
            int unMessage = unread_message+groupLoad.getGroupMessage();
            System.out.println("\t-----------------------------------------\t");
            System.out.println("\t             欢迎登录，"+load.getName());
            System.out.println("\t-----------------------------------------\t");
            System.out.println("\t---------    1.好友            \t---------\t");
            System.out.println("\t---------    2.群聊            \t---------\t");
            System.out.println("\t---------    3.查询用户（uid） \t---------\t");
            System.out.println("\t---------    4.未读消息(" +unMessage+ ")     \t---------\t");
            System.out.println("\t---------    5.我的资料        \t---------\t");
            System.out.println("\t---------    6.黑名单          \t---------\t");
            System.out.println("\t---------    7.退出登录        \t---------\t");
            System.out.println("\t---------    8.注销            \t---------\t");
            System.out.println("\t---------    9.刷新            \t---------\t");
            System.out.println("\t-----------------------------------------\t");
            String tmp = new Scanner(System.in).nextLine();
            switch (tmp) {
                case "1":
                    ChatSystem.friendSystem(ctx);
                    break;
                case "2":
                    GroupSystem.groupSystem(ctx);
                    break;
                case "3":
                    FindSystem.FindUid(ctx);
                    break;
                case "4":
                    ChatSystem.unreadMessage(ctx);
                    break;
                case "5":
                    MaterialSystem.myMaterial(ctx);
                    break;
                case "6":
                    MaterialSystem.blacklist(ctx);
                    break;
                case "7":
                    ctx.channel().close();
                    return;
                case "8":
                    if(LoginMessage.logOut(ctx)) {
                        ctx.channel().close();
                        return;
                    }
                    break;
                case "9":
                    break;
                default:
                    System.err.println("输入错误，请重新尝试");
                    break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        sun.misc.SignalHandler handler = new sun.misc.SignalHandler() {
            @Override
            public void handle(sun.misc.Signal signal) {
                System.out.println("别ctrl+c了，建议'ps aux' and 'kill'");
            }
        };    // 设置INT信号(Ctrl+C中断执行)交给指定的信号处理器处理，废掉系统自带的功能
        sun.misc.Signal.handle(new sun.misc.Signal("INT"), handler);

        new Start().Begin();

    }
}
