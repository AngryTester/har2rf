package com.angrytest.util;

import java.io.*;

/**
 * @className FileUtil
 * @Description TODO
 * @Author HUANGPENG541
 * @Date 2018-11-23
 **/
public class FileUtil {
    /**
     * 读取文件，返回字符串
     *
     * @param fileName
     * @return
     */
    public static String readFile(String fileName) {
//        String returnStr = "";
//        File file = new File(fileName);
//        Reader reader = null;
//        try {
//            // 一次读一个字符
//            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
//            int tempchar;
//            while ((tempchar = reader.read()) != -1) {
//                if (((char) tempchar) != '\r') {
//                    returnStr += (char) tempchar;
//                }
//            }
//            reader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        StringBuffer buffer = new StringBuffer();
        File file = new File(fileName);
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader in = new BufferedReader(isr);
            String line = "";
            while ((line = in.readLine()) != null){
                buffer.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 创建目录
     * @param folderPath
     */
    public static void newFolder(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * 创建文件
     * @param filePath
     * @param fileContent
     */
    public static void newFile(String filePath,String fileContent){
        try {
            BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (filePath,true),"UTF-8"));
            bw.write(fileContent);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath){
        return new File(filePath).exists();
    }
}
