package com.github.www.jump.bean;

/**
 * SSR数据bean
 * Created by iswgr on 2017/8/17.
 */

public class DataBean {
    //服务器地址
    private String serviceAddr;
    //服务器ip
    private String serviceIp;
    //端口号
    private String post;
    //加密方式
    private String method;
    //捐献者名字
    private String name;
    //ssr链接
    private String ssLink;
    //国旗
    private int country;
    //标识位
    //0:中国
    //1:美国
    //2.日本
    //3:加拿大
    //4:新加坡
    //-1:其他
    private int code;

    public DataBean() {

    }

    public DataBean(String serviceAddr, String serviceIp, String post, String method, String name, String ssLink, int country, int code) {
        this.serviceAddr = serviceAddr;
        this.serviceIp = serviceIp;
        this.post = post;
        this.method = method;
        this.name = name;
        this.ssLink = ssLink;
        this.country = country;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public String getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public String getServiceIp() {
        return serviceIp;
    }

    public void setServiceIp(String serviceIp) {
        this.serviceIp = serviceIp;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSsLink() {
        return ssLink;
    }

    public void setSsLink(String ssLink) {
        this.ssLink = ssLink;
    }
}
