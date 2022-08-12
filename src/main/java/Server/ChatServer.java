package Server;

import Server.SimpleChannelHandler.*;
import Server.processLogin.*;
import config.DbUtil;
import config.Decode;
import config.Encode;
import config.FrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ChatServer {
    private static final Logger log = LogManager.getLogger(ChatServer.class);
    public static Map<String,Channel> uidChannelMap = new HashMap<>();
    public static Map<Channel,String> channelUidMap = new HashMap<>();
    private static Map<String,String> blackMap = new HashMap<>();

    private static Semaphore semaphore = new Semaphore(0);

    public void server() throws SQLException {
        ServerBootstrap boot = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        System.setProperty("io.netty.eventLoop.maxPendingTasks", "2048");
        try{
            ChannelFuture future = boot.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)// 9次
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 *1024* 1024 , 32 * 1024 * 1024))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new FrameDecoder());
                            ch.pipeline().addLast(new Decode()).addLast(new Encode());
                            ch.pipeline().addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            ch.pipeline().addLast("myHandler", new IdleHandler());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginMessage>(){
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) throws Exception {
                                    if(msg.isLogOut()){
                                        Delete.LogOut(msg);
                                        uidChannelMap.get(msg.getUid()).writeAndFlush(msg);
                                        return;
                                    }
                                    if(!ProcessLogin.login(msg)){
                                        ctx.channel().writeAndFlush(new LoginStringMessage("password error!"));
                                    }else {
                                        if(msg.getIsLogin()) {
                                            String uid = msg.getUid();
                                            Channel channel = ctx.channel();
                                            Channel channel1 = uidChannelMap.get(uid);
                                            if(channel1 != null){
                                                channelUidMap.remove(channel1,uid);
                                                channelUidMap.put(channel,uid);
                                                uidChannelMap.replace(uid,channel1,channel);
                                                channel1.writeAndFlush(new LoginStringMessage("you have been pushed off the line"));
                                                channel.writeAndFlush(new LoginStringMessage("someone is online!"));
                                                channel1.close();
                                                ReviseMaterial.reviseOnline(uid,true);
                                            }else {
                                                ReviseMaterial.reviseOnline(uid,true);
                                                channelUidMap.put(channel, uid);
                                                uidChannelMap.put(uid, channel);
                                               channel.writeAndFlush(new LoginStringMessage("login success!"));
                                            }
                                            log.info(uid + "已登录");
                                        }else{
                                            ctx.channel().writeAndFlush(new LoginStringMessage("register success!" +
                                                    "你的uid为"+msg.getUid()));
                                        }
                                    }
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginStringMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginStringMessage msg) throws Exception {
                                    String str = msg.getMessage();
                                    String[] strings = str.split("!");
                                    if(str.compareTo(("Goodbye!")) == 0) {
                                        log.info(channelUidMap.get(ctx.channel())+":Goodbye!");
                                        log.info(channelUidMap.get(ctx.channel()) + "已退出！");
                                    } else if(str.equals("err!")){
                                    } else if(str.startsWith("start")){
                                        LoadMessage lm = LoadSystem.loadMessage(strings[1],0);
                                        log.info("start!");
                                        ctx.channel().writeAndFlush(lm);
                                    }else if(str.startsWith("flush")){
                                        LoadMessage lm = LoadSystem.loadMessage(strings[1],3);
                                        ctx.channel().writeAndFlush(lm);
                                    }
                                    else if(str.startsWith("singleLoad")){
                                        log.info("读取单聊信息："+strings[1]);
                                        ctx.channel().writeAndFlush(LoadSystem.SingleChat(strings[1]));
                                    }else if(str.startsWith("group")){
                                        log.info("读取群聊消息："+strings[1]);
                                        ctx.channel().writeAndFlush(LoadSystem.GroupChat(strings[1]));
                                    }
                                }
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception
                                {
                                    Channel channel = ctx.channel();
                                    String uid =  channelUidMap.get(channel);
                                    //设置为离线状态
                                    ReviseMaterial.reviseOnline(uid,false);
                                    //当有客户端断开连接的时候,就移除对应的通道
                                    channelUidMap.remove(channel,uid);
                                    uidChannelMap.remove(uid,channel);
                                    super.channelInactive(ctx);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    Channel channel = ctx.channel();
                                    String uid =  channelUidMap.get(channel);
                                    //当有客户端断开连接的时候,就移除对应的通道
                                    uidChannelMap.remove(uid,channel);
                                    channelUidMap.remove(channel,uid);
                                    super.exceptionCaught(ctx, cause);
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<UserMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, UserMessage msg) throws Exception {
                                    String uid = msg.getUid();
                                    UserMessage um = LoadSystem.friendMaterial(uid);
                                    ctx.writeAndFlush(um);
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<FindMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FindMessage msg) throws Exception {
                                    msg.setResult(Verify.verifyPassword(msg));
                                    ctx.channel().writeAndFlush(msg);
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ReviseMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ReviseMessage msg) throws Exception {
                                    boolean result;
                                    boolean end = true;
                                    if(msg.getGander() != null){
                                        result = ReviseMaterial.reviseGander(msg);
                                        end = result;
                                    }
                                    if(msg.getPassword() != null){
                                        result = ReviseMaterial.revisePassword(msg);
                                        end = end && result;
                                    }
                                    if(msg.getName() != null){
                                        result = ReviseMaterial.reviseName(msg);
                                        end = end && result;
                                    }
                                    if(msg.getAge() != -1){
                                        result = ReviseMaterial.reviseAge(msg);
                                        end = end && result;
                                    }
                                    if(msg.getBlack() != 0){
                                        if(msg.getBlack() == 1)
                                            result = ReviseMaterial.reviseBlack(msg,false);
                                        else
                                            result = ReviseMaterial.reviseBlack(msg,true);
                                        end = end && result;
                                    }
                                    ctx.writeAndFlush(new ReviseMessage(msg.getUid(),end));
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<StringMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, StringMessage msg) throws Exception {
                                    if(blackMap.get(msg.getMe().getUid()) == null || blackMap.get(msg.getMe().getUid()).compareTo(msg.getFriend().getUid()) != 0) {
                                        if(!Verify.verifyBlack(msg)) {
                                            Channel channel = uidChannelMap.get(msg.getFriend().getUid());
                                            if (channel != null) {
                                                channel.writeAndFlush(msg);
                                            }//在线
                                            Storage.storageSingleMessage(msg);
                                        }
                                    }
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ReviseMsgStatusMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ReviseMsgStatusMessage msg) throws Exception {
                                    //修改所有time之前的消息为已读
                                    if(!ReviseMaterial.reviseMessageStatus(msg)){
                                        log.warn("reviseMessageStatus failed:"+msg.getTime());
                                    }
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

                        }
                    }).bind(8100);

            Channel channel = future.sync().channel();

            channel.closeFuture().sync();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    public static void addBlack(String uid,String friend_uid){
        blackMap.put(uid,friend_uid);
        blackMap.put(friend_uid,uid);
    }

    public static void removeBlack(String uid,String friend_uid){
        blackMap.remove(uid,friend_uid);
        blackMap.remove(friend_uid,uid);
    }


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        sun.misc.SignalHandler handler = new sun.misc.SignalHandler() {
            @Override
            public void handle(sun.misc.Signal signal) {
                System.out.println("别ctrl+c了，建议'ps aux' and 'kill'");
            }
        };    // 设置INT信号(Ctrl+C中断执行)交给指定的信号处理器处理，废掉系统自带的功能
        sun.misc.Signal.handle(new sun.misc.Signal("INT"), handler);

        DbUtil dbUtil = DbUtil.loginMysql().start();
        new ChatServer().server();
        dbUtil.close();
    }
}
