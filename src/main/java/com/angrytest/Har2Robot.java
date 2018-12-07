package com.angrytest;

import com.angrytest.constant.ROBOT;
import com.angrytest.model.API;
import com.angrytest.model.Har;
import com.angrytest.model.Request;
import com.angrytest.model.Response;
import com.angrytest.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @className Har2Robot
 * @Description TODO
 * @Author AngryTester
 * @Date 2018-11-23
 **/
public class Har2Robot {

    // 接口IP+端口
    static String HOST = "";

    public static void main(String[] args) throws Exception {
        // 接口序号
        int no = 0;
        Scanner s = new Scanner(System.in);
        System.out.println("请输入har文件绝对路径:");
        String file = s.next();
        s.close();
        if (!FileUtil.isFileExist(file)) {
            throw new Exception("请确认har文件是否存在");
        }
        String proName = new File(file).getName().replace(".har", "");

        if (new File(proName).exists()) {
            throw new Exception("工程已存在，请确认当前目录下不存在" + proName + "文件夹");
        }
        // 创建项目文件夹
        initProjectDir(proName);

        System.out.println("---开始文件转换对象---");

        // har文件转换成har对象
        Har har = Har2Obj.harFile2HarObj(file);

        System.out.println("---文件转换对象完成---");

        // 获取所有的请求响应
        List<API> apis = har.getApis();

        // 用于存储所有的接口名称，判重
        List<String> apiNames = new ArrayList<String>();

        // 循环处理所有的API
        for (int i = 0; i < apis.size(); i++) {
            API api = apis.get(i);
            Request request = api.getRequest();
            if(HOST.equals("")){
                HOST = request.getDomain();
            }
            Response response = api.getResponse();
            // 非Get和Post方法以及返回非200的不处理
            if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("GET") && response.getStatusCode().equals("200")) {
                // 以下先处理接口名称,直接用path去掉参数再替换/作为接口名称
                String path = request.getPath();
                String apiName = "";

                // 处理特殊字符/和?
                if (path.contains("?")) {
                    apiName = path.substring(1, path.indexOf("?")).replaceAll("/", "-");
                } else {
                    apiName = path.substring(1).replaceAll("/", "-");
                }
                String apiNameStr = request.getDomain() + "/" + apiName.substring(4);

                // 如果已存在的接口不再重复处理
                if (apiNames.contains(apiNameStr)) {
                    continue;
                } else {
                    apiNames.add(apiNameStr);
                    no++;
                }

                // 为了保持接口的先后顺序，自动在接口名称前补0
                apiName = String.format("%03d", no) + "." + apiName;

                // 默认导入公共资源
                String setting = ROBOT.SETTINGS
                        + ROBOT.RESOURCE + ROBOT.DIVISION + "公共资源.robot" + ROBOT.LINE_SEPARATOR;
                String testcases = ROBOT.TESTCASES
                        + "正例" + ROBOT.LINE_SEPARATOR
                        + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + "${HOST}" + ROBOT.LINE_SEPARATOR
                        + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + request.getPath() + ROBOT.LINE_SEPARATOR;

                if (request.getMethod().equalsIgnoreCase("POST")) {
                    String paramsStr = ROBOT.DIVISION + "${params}" + ROBOT.DIVISION + ROBOT.CREATEDICT;
                    if (!request.getParams().isEmpty()) {
                        for (Map.Entry<String, Object> entry : request.getParams().entrySet()) {
                            paramsStr += (ROBOT.DIVISION + entry.getKey() + "=" + entry.getValue().toString());
                        }
                        testcases += paramsStr + ROBOT.LINE_SEPARATOR;
                    } else {
                        testcases += (ROBOT.DIVISION + "${params}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + "NONE") + ROBOT.LINE_SEPARATOR;
                    }
                    if (!request.getData().equals("")) {
                        testcases += (ROBOT.DIVISION + "${data}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + request.getData()) + ROBOT.LINE_SEPARATOR;
                    } else {
                        testcases += (ROBOT.DIVISION + "${data}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + "NONE") + ROBOT.LINE_SEPARATOR;
                    }
                }

                if (!request.getHeaders().isEmpty()) {
                    String headersStr = ROBOT.DIVISION + "${headers}" + ROBOT.DIVISION + ROBOT.CREATEDICT;
                    for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                        headersStr += (ROBOT.DIVISION + entry.getKey() + "=" + entry.getValue().toString());
                    }
                    testcases += headersStr + ROBOT.LINE_SEPARATOR;
                } else {
                    testcases += (ROBOT.DIVISION + "${headers}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + "NONE") + ROBOT.LINE_SEPARATOR;
                }

                String content = response.getContent();
                // 处理响应体中的空格
                String regex = "\\s+";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(content);//进行匹配
                while (m.find()) {
                    int length = m.group().length();
                    String replacement = "";
                    if (length >= 2) {
                        for (int j = 0; j < length - 1; j++) {
                            replacement += "\\" + " ";
                        }
                    }
                    content = content.substring(0, content.indexOf(m.group())) + " " + replacement + content.substring(content.indexOf(m.group()) + length);
                }

                testcases += ROBOT.DIVISION + "${expect}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.DIVISION + content + ROBOT.DIVISION + "# 录制的HAR中的返回" + ROBOT.LINE_SEPARATOR;

                if (request.getMethod().equalsIgnoreCase("POST")) {
                    testcases += ROBOT.DIVISION + "${res}" + ROBOT.DIVISION + "POST请求" + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + "${data}" + ROBOT.DIVISION + "${params}" + ROBOT.DIVISION + "${headers}" + ROBOT.LINE_SEPARATOR;
                } else {
                    testcases += ROBOT.DIVISION + "${res}" + ROBOT.DIVISION + "GET请求" + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + "${headers}" + ROBOT.LINE_SEPARATOR;
                }

                FileUtil.newFile(proName + ROBOT.FILE_SEPARATOR + apiName + ".robot", setting + ROBOT.LINE_SEPARATOR + testcases);

                System.out.println("---接口：" + apiName + "创建完成---");
            }
        }

        // 创建公共资源
        initPublicResource(proName);

        System.out.println("---RF工程初始化完成，请使用RIDE导入当前目录下的-" + proName + "-文件夹---");
    }

    public static void initProjectDir(String proName) {
        FileUtil.newFolder(proName);
    }

    public static void initPublicResource(String proName) {
        String publicResource = ROBOT.SETTINGS
                + ROBOT.LIBRARY + ROBOT.DIVISION + ROBOT.REQUESTS_LIBRARY + ROBOT.LINE_SEPARATOR
                + ROBOT.LIBRARY + ROBOT.DIVISION + ROBOT.COLLECTIONS + ROBOT.LINE_SEPARATOR
                + ROBOT.LINE_SEPARATOR
                + ROBOT.VARIABLES + ROBOT.LINE_SEPARATOR
                + "${HOST}" + ROBOT.DIVISION + HOST + ROBOT.LINE_SEPARATOR
                + ROBOT.LINE_SEPARATOR
                + ROBOT.KEYWORDS
                + ROBOT.POST
                + ROBOT.DIVISION + ROBOT.ARGUMENTS + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + "${data}=None" + ROBOT.DIVISION + "${params}=None" + ROBOT.DIVISION + "${headers}=None" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${cookies}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${cookies_exist}" + ROBOT.DIVISION + ROBOT.RUNKEYWORDANDRETURNSTATUS + ROBOT.DIVISION + ROBOT.VARIABLESHOULDEXIST + ROBOT.DIVISION + "${global_cookies}" + ROBOT.DIVISION + "# 如果设置了全局COOKIES才加COOKIE" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + ROBOT.RUNKEYWORDIF + ROBOT.DIVISION + "${cookies_exist}" + ROBOT.DIVISION + ROBOT.SETSUITEVARIABLE + ROBOT.DIVISION + "${cookies}" + ROBOT.DIVISION + "${global_cookies}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "Create Session" + ROBOT.DIVISION + "api" + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + "cookies=${cookies}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${res}" + ROBOT.DIVISION + "Post Request" + ROBOT.DIVISION + "api" + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + "data=${data}" + ROBOT.DIVISION + "params=${params}" + ROBOT.DIVISION + "headers=${headers}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "log" + ROBOT.DIVISION + "${res.text}" + ROBOT.LINE_SEPARATOR
//                + ROBOT.DIVISION + "Should Be Equal" + ROBOT.DIVISION + "${res.text}" + ROBOT.DIVISION + "${expect}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${cookies_str}" + ROBOT.DIVISION + ROBOT.CONVERTTOSTRING + ROBOT.DIVISION + "${res.cookies}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${cookies_is_null}" + ROBOT.DIVISION + ROBOT.RUNKEYWORDANDRETURNSTATUS + ROBOT.DIVISION + ROBOT.SHOULDBEEQUAL + ROBOT.DIVISION + "${cookies_str}" + ROBOT.DIVISION + "<RequestsCookieJar[]>" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + ROBOT.RUNKEYWORDUNLESS + ROBOT.DIVISION + "${cookies_is_null}" + ROBOT.DIVISION + ROBOT.SETGLOBALVARIABLE + ROBOT.DIVISION + "${global_cookies}" + ROBOT.DIVISION + "${res.cookies}" + ROBOT.DIVISION + "# 返回COOKIES不为空时才重新设置COOKIE" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${res_json}" + ROBOT.DIVISION + "To JSON" + ROBOT.DIVISION + "${res.text}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${resulltCode}" + ROBOT.DIVISION + "Get From Dictionary" + ROBOT.DIVISION + "${res_json}" + ROBOT.DIVISION + "resultCode" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${code_list}" +ROBOT.DIVISION + "Create List" + ROBOT.DIVISION + "0000"+ ROBOT.DIVISION + "0" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${resulltMessage}" + ROBOT.DIVISION + "Get From Dictionary" + ROBOT.DIVISION + "${res_json}" + ROBOT.DIVISION + "resultMessage" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${msg_list}" +ROBOT.DIVISION + "Create List" + ROBOT.DIVISION + "success" + ROBOT.DIVISION + "SUCCESS" + ROBOT.DIVISION + "处理成功" + ROBOT.DIVISION + "操作成功" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# Should Contain" + ROBOT.DIVISION + "${code_list}" + ROBOT.DIVISION + "${resulltCode}" + ROBOT.DIVISION + "# 由于断言规则较复杂，暂时注释，可根据需要打开" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# Should Contain" + ROBOT.DIVISION + "${msg_list}" + ROBOT.DIVISION + "${resulltMessage}" + ROBOT.DIVISION + "# 由于断言规则较复杂，暂时注释，可根据需要打开" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "[Return]" + ROBOT.DIVISION + "${res}" + ROBOT.LINE_SEPARATOR
                + ROBOT.LINE_SEPARATOR
                + ROBOT.GET
                + ROBOT.DIVISION + ROBOT.ARGUMENTS + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + "${headers}=None" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${cookies}" + ROBOT.DIVISION + ROBOT.SETVARIABLE + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${cookies_exist}" + ROBOT.DIVISION + ROBOT.RUNKEYWORDANDRETURNSTATUS + ROBOT.DIVISION + ROBOT.VARIABLESHOULDEXIST + ROBOT.DIVISION + "${global_cookies}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + ROBOT.RUNKEYWORDIF + ROBOT.DIVISION + "${cookies_exist}" + ROBOT.DIVISION + ROBOT.SETSUITEVARIABLE + ROBOT.DIVISION + "${cookies}" + ROBOT.DIVISION + "${global_cookies}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "Create Session" + ROBOT.DIVISION + "api" + ROBOT.DIVISION + "${domain}" + ROBOT.DIVISION + "cookies=${cookies}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "${res}" + ROBOT.DIVISION + "Get Request" + ROBOT.DIVISION + "api" + ROBOT.DIVISION + "${path}" + ROBOT.DIVISION + "headers=${headers}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "log" + ROBOT.DIVISION + "${res.text}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${res_json}" + ROBOT.DIVISION + "To JSON" + ROBOT.DIVISION + "${res.text}" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${resulltCode}" + ROBOT.DIVISION + "Get From Dictionary" + ROBOT.DIVISION + "${res_json}" + ROBOT.DIVISION + "resultCode" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${code_list}" +ROBOT.DIVISION + "Create List" + ROBOT.DIVISION + "0000" + ROBOT.DIVISION + "0" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${resulltMessage}" + ROBOT.DIVISION + "Get From Dictionary" + ROBOT.DIVISION + "${res_json}" + ROBOT.DIVISION + "resultMessage" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# ${msg_list}" +ROBOT.DIVISION + "Create List" + ROBOT.DIVISION + "success" + ROBOT.DIVISION + "SUCCESS" + ROBOT.DIVISION + "处理成功" + ROBOT.DIVISION + "操作成功" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# Should Contain" + ROBOT.DIVISION + "${code_list}" + ROBOT.DIVISION + "${resulltCode}" + ROBOT.DIVISION + "# 由于断言规则较复杂，暂时注释，可根据需要打开" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + "# Should Contain" + ROBOT.DIVISION + "${msg_list}" + ROBOT.DIVISION + "${resulltMessage}" + ROBOT.DIVISION + "# 由于断言规则较复杂，暂时注释，可根据需要打开" + ROBOT.LINE_SEPARATOR
                + ROBOT.DIVISION + ROBOT.RETURN + ROBOT.DIVISION + "${res}" + ROBOT.LINE_SEPARATOR;
        FileUtil.newFile(proName + ROBOT.FILE_SEPARATOR + "公共资源.robot", publicResource);
        String init = ROBOT.SETTINGS
                + ROBOT.RESOURCE + ROBOT.DIVISION + "公共资源.robot";
        FileUtil.newFile(proName + ROBOT.FILE_SEPARATOR + "__init__.robot", init);
    }

}
