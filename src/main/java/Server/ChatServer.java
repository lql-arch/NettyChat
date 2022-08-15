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
    public static Map<String,String> blackMap = new HashMap<>();

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
                            ch.pipeline().addLast(new LoginHandler());
                            ch.pipeline().addLast(new LoginStringHandler());
                            ch.pipeline().addLast(new UserHandler());
                            ch.pipeline().addLast(new FindHandler());
                            ch.pipeline().addLast(new ReviseHandler());
                            ch.pipeline().addLast(new StringHandler());
                            ch.pipeline().addLast(new ReviseMsgStatusHandler());
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
//        dbUtil.close();
    }
}
