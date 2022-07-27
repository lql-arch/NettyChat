package Server;

import NettyChat.DbUtil;
import Server.processLogin.*;
import config.Decode;
import config.Encode;
import config.FrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import message.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private static final Logger log = LogManager.getLogger(ChatServer.class);

    private static Map<String,Channel> uidChannelMap = new HashMap<>();
    private static Map<Channel,String> channelUidMap = new HashMap<>();

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
                            ch.pipeline().addLast(new FrameDecoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginMessage>(){
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) throws Exception {
                                    if(!processLogin.login(msg)){
                                        ctx.channel().writeAndFlush(new LoginStringMessage("password error!"));
                                    }else {
                                        if(msg.getIsLogin()) {
                                            Channel channel = ctx.channel();
                                            channelUidMap.put(channel, msg.getUid());
                                            uidChannelMap.put(msg.getUid(),channel);
                                            log.info(msg.getUid() + "已登录");
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
                                        log.info(channelUidMap.get(ctx.channel())+":Goodbye!");
                                        log.info(channelUidMap.get(ctx.channel()) + "已退出！");
                                    } else if(str.equals("err!")){
                                        //log.info(ctx.channel() + "错误登录");
                                    } else if(str.startsWith("start")){
                                        log.info("start!");
                                        ctx.channel().writeAndFlush(LoadSystem.loadMessage(strings[1]));
                                    }else if(str.startsWith("singleLoad")){
                                        log.info("读取单聊信息："+strings[1]+"和"+strings[2]);
                                        ctx.channel().writeAndFlush(LoadSystem.SingleChat(strings[1],strings[2]));
                                    }
                                }
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception
                                {
                                    Channel channel = ctx.channel();
                                    String uid =  channelUidMap.get(channel);
                                    //当有客户端断开连接的时候,就移除对应的通道
//                                    channelUidMap.remove(uid,channel);
//                                    uidChannelMap.remove(channel,uid);
                                    super.channelInactive(ctx);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    Channel channel = ctx.channel();
                                    String uid =  channelUidMap.get(channel);
                                    //当有客户端断开连接的时候,就移除对应的通道
//                                    channelUidMap.remove(uid,channel);
//                                    uidChannelMap.remove(channel,uid);
                                    super.exceptionCaught(ctx, cause);
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<UserMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, UserMessage msg) throws Exception {
                                    String uid = msg.getUid();
                                    ctx.writeAndFlush(LoadSystem.friendMaterial(uid));
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
                                    boolean result = false;
                                    if(msg.getGander() != null){
                                        result = ReviseMaterial.reviseGander(msg);
                                    } else if(msg.getPassword() != null){
                                        result = ReviseMaterial.revisePassword(msg);
                                    } else if(msg.getName() != null){
                                        result = ReviseMaterial.reviseName(msg);
                                    } else if(msg.getAge() != -1){
                                        result = ReviseMaterial.reviseAge(msg);
                                    }
                                    ctx.writeAndFlush(new ReviseMessage(msg.getUid(),result));
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<StringMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, StringMessage msg) throws Exception {
                                    Storage.storageSingleMessage(msg);
                                    Channel channel = uidChannelMap.get(msg.getFriend().getUid());
                                    if(channel != null)
                                        channel.writeAndFlush(msg);
                                    else{
                                        //不在线
                                    }
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ReviseMsgStatusMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ReviseMsgStatusMessage msg) throws Exception {
                                    if(!ReviseMaterial.reviseMessageStatus(msg)){
                                        log.warn("reviseMessageStatus failed:"+msg.getTime());
                                    }
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
