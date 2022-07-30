package Server;

import config.DbUtil;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
                            ch.pipeline().addFirst(new FrameDecoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginMessage>(){
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) throws Exception {
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
                                            }else {
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
                                        //log.info(ctx.channel() + "错误登录");
                                    } else if(str.startsWith("start")){
                                        LoadMessage lm = LoadSystem.loadMessage(strings[1],0);
                                        log.info("start!");
                                        ctx.channel().writeAndFlush(lm);
                                    }else if(str.startsWith("flush")){
                                        LoadMessage lm = LoadSystem.loadMessage(strings[1],3);
//                                        log.info("flush!");
                                        ctx.channel().writeAndFlush(lm);
                                    }
                                    else if(str.startsWith("singleLoad")){
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
                                    channelUidMap.remove(channel,uid);
                                    uidChannelMap.remove(uid,channel);
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
                                    UserMessage um = LoadSystem.friendMaterial(uid);
//                                    log.debug("uid:"+uid+" "+um.getBuild_time());
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
                                    ctx.writeAndFlush(new ReviseMessage(msg.getUid(),end));
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<StringMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, StringMessage msg) throws Exception {
                                    Channel channel = uidChannelMap.get(msg.getFriend().getUid());
                                    if(channel != null) {
                                        channel.writeAndFlush(msg);
                                    }//在线
//                                    log.debug(msg.getMessage());
                                    Storage.storageSingleMessage(msg);
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

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<RequestMessage>() {

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
                                    if(msg.isClearMsg()){
                                        ReviseMaterial.reviseRequest(msg);
                                    }else {
                                        if (Verify.verifyIsFriend(msg)) {
                                            ctx.channel().writeAndFlush(new RequestMessage(true));
                                        } else {
                                            //向对象发送确认消息
                                            if (!msg.isConfirm()) {
                                                if (!msg.isFriend()) {//拒绝
                                                    String str = msg.getRecipientPerson().getName() + "拒绝了你的好友请求";
                                                    StringMessage sm = new StringMessage(msg.getRecipientPerson(), msg.getRequestPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                                                    ReviseMaterial.reviseAddFriendMsg(msg);
                                                    Storage.storageRequestMessage(sm, true, true);//addFriend 表示添加好友过程结束
                                                    ctx.channel().writeAndFlush(new RequestMessage(false));//阻塞用
                                                } else {//isFriend以false开始保存添加信息到数据库
                                                    log.debug(msg.isFriend()+" "+msg.isConfirm());
                                                    String str = msg.getRequestPerson().getName() + "发起了好友申请";
                                                    StringMessage sm = new StringMessage(msg.getRequestPerson(), msg.getRecipientPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                                                    Storage.storageRequestMessage(sm, false, true);
                                                }
                                            } else {//对方确认添加后添加消息到数据库
                                                String str = msg.getRecipientPerson().getName() + "同意了你的好友请求";
                                                StringMessage sm = new StringMessage(msg.getRecipientPerson(), msg.getRequestPerson(), str, Timestamp.valueOf(LocalDateTime.now()).toString());
                                                ReviseMaterial.reviseAddFriendMsg(msg);//修改状态为已读
                                                Storage.storageRequestMessage(sm, true, true);
                                                Storage.storageBuildFriends(msg);//建立联系

                                                sm.setMessage("我们已经是好友了！");
                                                Storage.storageSingleMessage(sm);
                                                ctx.channel().writeAndFlush(new RequestMessage(false));//阻塞用
                                            }
                                        }
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
