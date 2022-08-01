package client.System;

import client.Start;
import io.netty.channel.ChannelHandlerContext;
import message.RequestMessage;
import message.ReviseMessage;
import message.UserMessage;

import java.util.Scanner;

import static client.Start.semaphore;

public class DeleteSystem {
    public static boolean deleteFriend(ChannelHandlerContext ctx, UserMessage me, UserMessage friend) throws InterruptedException {
        System.out.println("你确认要删除好友吗？(yes/no)");
        String str = new Scanner(System.in).nextLine();
        if(str.compareToIgnoreCase("yes") == 0 || str.compareToIgnoreCase("y") == 0) {
            RequestMessage rm = new RequestMessage().setRequestPerson(me).setRecipientPerson(friend).setAddOrDelete(false).setFriend(true);
            ctx.channel().writeAndFlush(rm);
            Start.semaphore.acquire();
            return true;
        }else{
            return false;
        }
    }

    public static boolean removeBlackFriend(ChannelHandlerContext ctx, UserMessage me, UserMessage friend) throws InterruptedException {
        System.out.println("你确认要将好友移入黑名单？(yes/no)");
        String str = new Scanner(System.in).nextLine();
        if(str.compareToIgnoreCase("yes") == 0 || str.compareToIgnoreCase("y") == 0) {
            ReviseMessage rvm = new ReviseMessage();
            rvm.setBlack(2);
            rvm.setUid(Start.uid);
            rvm.setFriend_uid(friend.getUid());
            ctx.channel().writeAndFlush(rvm);
            semaphore.acquire();
            return true;
        }else{
            return false;
        }
    }
}
