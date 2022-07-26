package Server;

import NettyChat.*;
import Server.processLogin.LoadSystem;
import Server.processLogin.processLogin;
import config.Decode;
import config.Encode;
import config.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final Logger log = LogManager.getLogger(ChatServer.class);

    private static List<Channel> channelList = new ArrayList<>();

    public void server() {
        ServerBootstrap boot = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try{
            ChannelFuture future = boot.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Decode()).addLast(new Encode());
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginMessage>(){
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) throws Exception {
                                    if(!processLogin.login(msg)){
                                        ctx.channel().writeAndFlush(new LoginStringMessage("password error!"));
                                    }else {
                                        if(msg.getIsLogin()) {
                                            Channel channel = ctx.channel();
                                            channelList.add(channel);
                                            log.info(channel + "已登录");
                                            ctx.channel().writeAndFlush(new LoginStringMessage("login success!"));
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
                                        log.info(ctx.channel()+":Goodbye!");
                                        log.info(ctx.channel() + "已退出！");
                                    }
                                    if(str.equals("err!")){
                                        log.info(ctx.channel() + "错误登录");
                                    }
                                    if(str.startsWith("start")){
//                                        log.debug("start!");
                                        ctx.channel().writeAndFlush(LoadSystem.loadMessage(strings[1]));
                                    }
                                    if(str.equals("活着没?")){
                                    }
                                }
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception
                                {
                                    Channel channel = ctx.channel();
                                    //当有客户端断开连接的时候,就移除对应的通道
                                    channelList.remove(channel);
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<UserMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, UserMessage msg) throws Exception {
                                    String uid = msg.getUid();
                                    ctx.writeAndFlush(LoadSystem.friendMaterial(uid));
                                }
                            });
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


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DbUtil.loginMysql().start();//.close();
        new ChatServer().server();
    }
}
