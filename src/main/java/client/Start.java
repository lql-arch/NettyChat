package client;

import client.System.ChatSystem;
import config.Decode;
import config.Encode;
import config.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import message.LoadMessage;
import message.LoginMessage;
import message.LoginStringMessage;
import message.UserMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Start {
    private  static final Logger log = LogManager.getLogger();
    private LoginMessage login;
    private static AtomicBoolean flag = new AtomicBoolean(false);
    private static int unread_message = 0;
    private LoadMessage load;
    public volatile UserMessage friend;
    //public static Semaphore semaphore = new Semaphore(1);
    public static CountDownLatch count = new CountDownLatch(1);

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
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
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
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    extracted(ctx);
                                    super.channelActive(ctx);
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginStringMessage>() {
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
                                    load = msg;
                                    unread_message = msg.getUnread_message();
                                    new Thread(() -> {
                                        try {
                                            main_menu(ctx);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).start();
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<UserMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, UserMessage msg) throws Exception {
                                    friend = msg;
                                    count.countDown();
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
        synchronized (this) {
            System.out.println("\t----------------------------------\t");
            System.out.println("\t---------       1.登录     --------\t");
            System.out.println("\t--------- 2.快速登录(已弃用) --------\t");
            System.out.println("\t---------       3.注册     --------\t");
            System.out.println("\t---------       4.退出     --------\t");
            System.out.println("\t-----------------------------------\t");
        }
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
                    throw new Exception();
            }
    }

    private void main_menu(ChannelHandlerContext ctx) throws Exception {
        boolean flag = true;
        while(true) {
            synchronized (this) {
                System.out.println("\t----------------------------------------\t");
                System.out.println("\t---------    1.好友            ---------\t");
                System.out.println("\t---------    2.群聊            ---------\t");
                System.out.println("\t---------    3.退出登录         ---------\t");
                System.out.println("\t---------    4.未读消息(" + unread_message + ")     ---------\t");
                System.out.println("\t----------------------------------------\t");
            }
            char tmp = (char) new Scanner(System.in).nextByte();
            switch (tmp) {
                case 1:
                    ChatSystem.friendSystem(load,ctx,friend);
                    break;
                case 2:
                    ChatSystem.groupSystem(load,ctx);
                    break;
                case 3:
                    flag = false;
                    break;
                case 4:
                    break;
            }
            if(!flag){
                break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Start().Begin();
    }
}
