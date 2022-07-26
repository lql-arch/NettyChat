package config;

import NettyChat.Json;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Decode extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger log = LogManager.getLogger();
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int version = buf.readInt();
        int type = buf.readInt();
        int length = buf.readInt();

//        log.debug(type +" "+version+" "+length);

        byte[] b = new byte[length];

        buf.readBytes(b,0,length);

        String str = new String(b,0,length, CharsetUtil.UTF_8);

        Class<? extends Message> cl = Message.getMessageClass(type);

        Message end = (Message) Json.parseFromJson(str,cl);

        out.add(end);
    }
}
