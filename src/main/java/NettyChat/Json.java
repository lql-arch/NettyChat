package NettyChat;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Json {
    //谷歌GsonBuilder构造器
    static GsonBuilder gb = new GsonBuilder();
    static {
        //不需要html escape
        gb.disableHtmlEscaping();
    }

    //序列化:使用Gson将 POJO 转成字符串
    private static String pojoToJson(java.lang.Object obj) {
        String json = gb.create().toJson(obj);
        return json;
    }

    //反序列化:使用Fastjson将字符串转成 POJO对象
    private static <T> T jsonToPojo(String json, Class<T>tClass)
    {
        T t = JSONObject.parseObject(json, tClass);
        return t;
    }

    //序列化:调用通用方法,使用Gson转成字符串
    public static String convertToJson(Object o) {
        return Json.pojoToJson(o);
    }

    //反序列化:使用FastJson转成Java POJO对象
    public static Object parseFromJson(String json, Class<?> cl) {
        return Json.jsonToPojo(json, cl);
    }
}