package com.github.www.jump.gson;

/**
 * version实体类
 * Created by iswgr on 2017/8/16.
 */

public class VersionGson {

    private int open;
    private int oldversion;
    private int version;
    private String desc;
    private String url;

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getOldversion() {
        return oldversion;
    }

    public void setOldversion(int oldversion) {
        this.oldversion = oldversion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
