package Server.SimpleChannelHandler;

import Server.processLogin.ReviseMaterial;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.ReviseMessage;

public class ReviseHandler extends SimpleChannelInboundHandler<ReviseMessage> {
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
}
