package com.angrytest.model;

/**
 * @className Response
 * @Description TODO
 * @Author AngryTester
 * @Date 2018-11-23
 **/
public class Response {

    /**
     * 状态码
     */
    private String statusCode;

    /**
     * 响应内容
     */
    private String content;

    /**
     * 响应类型
     */
    private String contentType;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
