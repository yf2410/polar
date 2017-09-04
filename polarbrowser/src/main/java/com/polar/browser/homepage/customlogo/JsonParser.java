package com.polar.browser.homepage.customlogo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class JsonParser {


    private static final Gson gson = new Gson();

    /**
     * 对象解析为json string
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * json string解析为类型为type的对象
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * json string解析为类型为t的对象
     *
     * @param json
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, Class<T> t) {
        return gson.fromJson(json, t);
    }

    /**
     * 转成list
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> List<T> gsonToList(String gsonString, Class<T> cls) {
        if (gson != null) {
            return gson.fromJson(gsonString, new TypeToken<List<T>>() {
            }.getType());
        } else {
            return null;
        }
    }
}
