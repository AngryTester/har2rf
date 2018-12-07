package com.angrytest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.angrytest.model.API;
import com.angrytest.model.Har;
import com.angrytest.model.Request;
import com.angrytest.model.Response;
import com.angrytest.util.FileUtil;
import com.angrytest.util.JsonUtil;

import java.util.*;

/**
 * @className Har2Obj
 * @Description TODO
 * @Author AngryTester
 * @Date 2018-11-23
 **/
public class Har2Obj {

    /**
     * 不用处理的header
     */
    public static final String[] IGNORE_REQUEST_HEADERS = {
            "host",
            "accept",
            "content-length",
            "connection",
            "accept-encoding",
            "accept-language",
            "origin",
            "referer",
            "cache-control",
            "pragma",
            "cookie",
            "upgrade-insecure-requests",
            ":authority",
            ":method",
            ":scheme",
            ":path"};

    /**
     * 将har文件转换成har对象
     *
     * @param harFile har文件绝对路径
     * @return
     */
    public static Har harFile2HarObj(String harFile) {
        Har har = new Har();
        List<API> apis = new ArrayList<API>();
        String harStr = FileUtil.readFile(harFile);
        JSONObject harJsonObj = JsonUtil.str2JsonObj(harStr);
        JSONArray entries = harJsonObj.getJSONObject("log").getJSONArray("entries");
        for (int i = 0; i < entries.size(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("application/javascript")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("image/png")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("image/jpeg")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("image/x-icon")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("text/css")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("font-woff2")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("image/gif")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("x-font-ttf")
                    || entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("image/svg+xml")){
                continue;
            }
            if(entry.getJSONObject("response").getJSONObject("content").getString("mimeType").contains("text/html")){
                if(entry.getJSONObject("response").getJSONObject("content").containsKey("text")){
                    if(entry.getJSONObject("response").getJSONObject("content").getString("text").contains("</")){
                        continue;
                    }
                }
            }
            API api = entry2Api(entry);
            apis.add(api);
        }
        har.setApis(apis);
        return har;
    }

    /**
     * 将entry对象转成API对象
     *
     * @param entry entry json对象
     * @return
     */
    public static API entry2Api(JSONObject entry) {
        API api = new API();
        // 先处理request
        Request request = new Request();
        String url = entry.getJSONObject("request").getString("url");
        request.setUrl(url);
        request.setDomain(url.substring(0, url.indexOf("/", 8)));
        request.setPath(url.substring(url.indexOf("/", 8)));
        Map<String, String> headers = new HashMap<String, String>();
        JSONArray headersObj = entry.getJSONObject("request").getJSONArray("headers");
        for (int i = 0; i < headersObj.size(); i++) {
            JSONObject headerObj = headersObj.getJSONObject(i);
            if (Arrays.asList(IGNORE_REQUEST_HEADERS).contains(headerObj.getString("name").toLowerCase())) {
                continue;
            } else {
                headers.put(headerObj.getString("name"), headerObj.getString("value"));
            }
        }
        request.setHeaders(headers);
        request.setMethod(entry.getJSONObject("request").getString("method"));
        Map<String, Object> params = new HashMap<String, Object>();
        if (entry.getJSONObject("request").getJSONObject("postData").containsKey("params")) {
            JSONArray paramsObj = entry.getJSONObject("request").getJSONObject("postData").getJSONArray("params");
            for (int j = 0; j < paramsObj.size(); j++) {
                JSONObject paramObj = paramsObj.getJSONObject(j);
                params.put(paramObj.getString("name"), paramObj.get("value"));
            }
        }
        request.setParams(params);
        if (entry.getJSONObject("request").getJSONObject("postData").containsKey("text")) {
            if (!entry.getJSONObject("request").getJSONObject("postData").getString("text").equals("")) {
                request.setData(entry.getJSONObject("request").getJSONObject("postData").getString("text")
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
//                        .replace(" ", "&nbsp;")
//                        .replace("'", "&#39;")
                        .replace("\\\n", "<br/>")
                        .replace("\\\"","&quot;")
                );
            } else {
                request.setData("");
            }
        } else {
            request.setData("");
        }

        api.setRequest(request);

        // 再处理response
        Response response = new Response();
        response.setStatusCode(entry.getJSONObject("response").getString("status"));
        response.setContentType(entry.getJSONObject("response").getJSONObject("content").getString("mimeType"));
        if (entry.getJSONObject("response").getJSONObject("content").containsKey("text")) {
            String content = entry.getJSONObject("response").getJSONObject("content").getString("text");
            response.setContent(content);
        } else {
            response.setContent("");
        }
        api.setResponse(response);

        return api;
    }


}
