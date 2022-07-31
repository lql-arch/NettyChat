package config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ObjectDeserializer;
import com.alibaba.fastjson2.JSON;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Json {
    //谷歌GsonBuilder构造器
    static GsonBuilder gb = new GsonBuilder();
    static {
        //不需要html escape
        gb.disableHtmlEscaping();
    }

    //序列化:使用Gson将 POJO 转成字符串
    public static String pojoToJson(java.lang.Object obj) {
        return gb.create().toJson(obj);
    }

    //反序列化:使用Fastjson将字符串转成 POJO对象
    public static <T> T jsonToPojo(String json, Class<T>tClass)
    {
        T t = JSONObject.parseObject(json, tClass);
        return t;
    }

    public static final class CustomerListSerializer implements ObjectDeserializer {

        @Override
        public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
            T t = null;
           return t;
        }

    }
}