package client.SimpleChannelHandler;

import client.Start;
import client.System.GroupSystem;
import client.normal.GroupChat_text;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.GroupStringMessage;

import java.util.ArrayList;
import java.util.List;

import static client.System.GroupSystem.groupChat_texts;

public class GroupStringHandler extends SimpleChannelInboundHandler<GroupStringMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupStringMessage msg) throws Exception {
        if(msg.isBanned()){
            System.out.println("禁言设置完成");
            Start.semaphore.release();
            return;
        }

        if(GroupSystem.groupChat.get()){
            System.out.println(msg.getText().getMyName()+": "+msg.getText().getText());
        }

        synchronized (GroupSystem.class) {
            List<GroupChat_text> t;
            if ((t = groupChat_texts.get(msg.getText().getGid())) != null) {
                t.add(msg.getText());
            } else {
                t = new ArrayList<>();
                t.add(msg.getText());
                groupChat_texts.put(msg.getText().getGid(), t);
            }
        }
    }
}
