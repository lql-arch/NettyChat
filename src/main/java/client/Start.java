package client;

import client.System.ChatSystem;
import client.System.FindSystem;
import client.System.MaterialSystem;
import config.Decode;
import config.Encode;
import config.FrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Start {
    private  static final Logger log = LogManager.getLogger();
    private LoginMessage login;
    private static AtomicBoolean flag = new AtomicBoolean(false);
    public static int unread_message = 0;
    private  LoadMessage load;
    public static LoadMessage singleLoad;
    public static LoadMessage groupLoad;
    public static volatile UserMessage friend;
    public static Semaphore semaphore = new Semaphore(0);
//    public static CountDownLatch count = new CountDownLatch(1);
    public static AtomicBoolean EnterPassword = new AtomicBoolean(true);
    public static String uid ;// myUid
    public static List<StringMessage> message = new ArrayList<>();//登录后的未读消息
    public static AtomicBoolean singleFlag = new AtomicBoolean(false);
    public static Map<String,String> uidNameMap = new HashMap<>();


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
                            ch.pipeline().addLast(new FrameDecoder());
//                            ch.pipeline().addLast(new ChannelDuplexHandler() {
//                                // 用来触发特殊事件
//                                @Override
//                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
//                                    IdleStateEvent event = (IdleStateEvent) evt;
//                                    // 触发了写空闲事件
//                                    if (event.state() == IdleState.WRITER_IDLE) {
////                                log.debug("3s 没有写数据了，发送一个心跳包");
//                                        ctx.writeAndFlush(new LoginStringMessage("活着没?"));
//                                    }
//                                }
//                            });
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
                                    if (str.equals("password error")) {
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
                                }

                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                    if(!flag.get()){
                                        extracted(ctx);
                                    }
                                    super.channelReadComplete(ctx);
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoadMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoadMessage msg) throws Exception {
                                    if(msg.getStatus() == 0) {
                                        load = msg;
                                        unread_message = msg.getUnread_message();
                                        //FindSystem.addUidNameMap(load,uidNameMap);
                                        new Thread(() -> {
                                            try {
                                                main_menu(ctx);
                                            } catch (Exception e) {
//                                                System.err.println("main_menu exception end:"+e);
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    }
                                    if(msg.getStatus() == 1){
                                        singleLoad = msg;
                                    }
                                    if(msg.getStatus() == 2){
                                        groupLoad = msg;
                                    }
                                    if(msg.getStatus() == 3){//flush
                                        load = msg;
                                        unread_message = msg.getUnread_message();
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
                                    semaphore.release();
//                                    count.countDown();
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<StringMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, StringMessage msg) throws Exception {
                                    if(!singleFlag.get())
                                        message.add(msg);
                                    else
                                        System.out.println(msg.getMe().getName()+":"+msg.getMessage());
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<RequestMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
                                    if(msg.isFriend()){
                                        System.err.println("你与目标已经是好友了！");
                                    }else{
                                        if(!msg.isConfirm()){
                                            //将好友信息添加到目前的好友队列中
                                            String addName = load.getName().equals(msg.getRequestPerson().getName()) ? msg.getRecipientPerson().getName() : msg.getRequestPerson().getName();
                                            load.addFriend(addName);
                                        }
                                            //确认是否添加好友，不添加将confirm改为false,isFriend也改为false
                                            //写未读消息
//                                            msg.setConfirm(false);
//                                            msg.setFriend(false);
//                                            ctx.writeAndFlush(msg);
                                    }
                                }
                            });

                        }
                    }).connect("127.0.0.1", 8100);

            channelFuture.sync();
            Channel channel = channelFuture.channel();

            channel.closeFuture().sync();

        }finally{
            worker.shutdownGracefully();
        }

    }

    private  void extracted(ChannelHandlerContext ctx) throws Exception {
            System.out.println("\t----------------------------------\t");
            System.out.println("\t---------       1.登录     --------\t");
            System.out.println("\t--------- 2.快速登录(已弃用) --------\t");
            System.out.println("\t---------       3.注册     --------\t");
            System.out.println("\t---------       4.退出     --------\t");
            System.out.println("\t-----------------------------------\t");
            char tmp = (char) new Scanner(System.in).nextByte();
            switch (tmp) {
                case 1:
                    login = LoginMessage.LoginUser();
                    ctx.channel().writeAndFlush(login);
                    flag.compareAndSet(false, true);
                    break;
                case 2:
                    break;
                case 3:
                    login = LoginMessage.register();
                    ctx.channel().writeAndFlush(login);
                    break;
                case 4:
                    ctx.channel().close();
                    break;
                default:
                    System.err.println("输入错误");
            }
    }

    private void main_menu(ChannelHandlerContext ctx) throws Exception {
        while(true) {
                ctx.channel().writeAndFlush(new LoginStringMessage("flush!"+login.getUid()));
                semaphore.acquire();
                System.out.println("\t----------------------------------------\t");
                System.out.println("\t---------    1.好友            ---------\t");
                System.out.println("\t---------    2.群聊            ---------\t");
                System.out.println("\t---------    3.查询用户（uid）   ---------\t");
                System.out.println("\t---------    4.未读消息(" + unread_message + ")     ---------\t");
                System.out.println("\t---------    5.我的资料         ---------\t");
                System.out.println("\t---------    6.黑名单           ---------\t");
                System.out.println("\t---------    7.退出登录         ---------\t");
                System.out.println("\t----------------------------------------\t");
            char tmp = (char) new Scanner(System.in).nextByte();
            switch (tmp) {
                case 1:
                    ChatSystem.friendSystem(load,ctx);
                    break;
                case 2:
                    ChatSystem.groupSystem(load,ctx);
                    break;
                case 3:
                    FindSystem.FindUid(ctx);
                    break;
                case 4:
                    ChatSystem.unreadMessage(ctx,load);
                    break;
                case 5:
                    MaterialSystem.myMaterial(load,ctx);
                    break;
                case 6:
                    MaterialSystem.blacklist(load,ctx);
                    break;
                case 7:
                    ctx.channel().close();
                    return;
                default:
                    System.out.println("输入错误，请重新尝试");
                    break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        sun.misc.SignalHandler handler = new sun.misc.SignalHandler() {
//            @Override
//            public void handle(sun.misc.Signal signal) {
//                // 什么都不做
//            }
//        };    // 设置INT信号(Ctrl+C中断执行)交给指定的信号处理器处理，废掉系统自带的功能
//        sun.misc.Signal.handle(new sun.misc.Signal("INT"), handler);

        new Start().Begin();

    }
}
