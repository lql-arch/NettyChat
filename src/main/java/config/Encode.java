package config;


import NettyChat.Json;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Encode extends MessageToMessageEncoder<Message> {//编码
    private static final Logger log = LogManager.getLogger();


    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        int length ;

        //有懒狗没写魔数

        buf.writeInt(1);//版本
        buf.writeInt(msg.getMessageType());//类型

        String str = Json.convertToJson(msg);

        byte[] b = str.getBytes();

        length = b.length;

        buf.writeInt(length);//长度

        buf.writeBytes(b);

        out.add(buf);

    }
}
