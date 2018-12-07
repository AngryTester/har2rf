package com.angrytest.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @className FileUtilTest
 * @Description TODO
 * @Author AngryTester
 * @Date 2018-11-23
 **/
public class FileUtilTest {

    @Test
    public void testReadFile(){
        String file = "src/test/resouces/1.har";
        String fileContent = FileUtil.readFile(file);
        Assert.assertTrue(fileContent.contains("headers"));
    }
}
