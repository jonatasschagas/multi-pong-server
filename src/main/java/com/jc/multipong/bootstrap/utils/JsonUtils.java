package com.jc.multipong.bootstrap.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by jchagas on 22/02/2017.
 */
public class JsonUtils {

    public static String toJson(Object object) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, classOfT);
    }

}
