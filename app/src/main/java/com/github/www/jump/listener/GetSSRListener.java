package com.github.www.jump.listener;

/**
 * 获取ssr回调接口
 * Created by iswgr on 2017/8/16.
 */

public interface GetSSRListener {
    void error(Exception e);

    void success(String data);
}
