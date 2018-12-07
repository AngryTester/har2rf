package com.angrytest.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @className JsonUtil
 * @Description TODO
 * @Author HUANGPENG541
 * @Date 2018-11-23
 **/
public class JsonUtil {
    /**
     * @param jsonStr 待转换的字符串
     * @return JSON对象
     */
    public static JSONObject str2JsonObj(String jsonStr){
        return JSON.parseObject(jsonStr);
    }
}
