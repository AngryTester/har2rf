package com.angrytest.model;

import java.util.Map;

/**
 * @className Request
 * @Description TODO
 * @Author AngryTester
 * @Date 2018-11-23
 **/
public class Request {

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求主机和端口
     */
    private String domain;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 请求头
     */
    private Map<String,String> headers;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求参数
     */
    private Map<String,Object> params;

    /**
     * 请求内容
     */
    private String data;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
