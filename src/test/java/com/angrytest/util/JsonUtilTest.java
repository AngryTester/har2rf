package com.angrytest.util;

import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @className JsonUtilTest
 * @Description TODO
 * @Author AngryTester
 * @Date 2018-11-23
 **/
public class JsonUtilTest {

    @Test
    public void testStr2JsonObj(){
        String jsonStr = "{\"id\":\"12345678\",\"name\":\"xxx\",\"age\":18}";
        JSONObject jsonObj = JsonUtil.str2JsonObj(jsonStr);
        Assert.assertEquals(jsonObj.getString("id"),"12345678");
    }
}
