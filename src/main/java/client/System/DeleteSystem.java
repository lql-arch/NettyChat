package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.RequestMessage;
import message.UserMessage;

public class DeleteSystem {
    public static void deleteFriend(ChannelHandlerContext ctx, UserMessage me, UserMessage friend) throws InterruptedException {
        RequestMessage rm = new RequestMessage().setRequestPerson(me).setRecipientPerson(friend).setAddOrDelete(false).setFriend(true);
        ctx.channel().writeAndFlush(rm);
        Start.semaphore.acquire();
    }
}
