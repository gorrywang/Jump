package com.github.www.jump.utils;

import android.app.Activity;

import com.github.www.jump.listener.GetSSRListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

/**
 * 联网类
 * Created by iswgr on 2017/8/16.
 */

public class HttpUtils {
    /**
     * get请求获取数据
     *
     * @param url     链接
     * @param handler 回调接口
     */
    public static void sendQuestGetBackResponse(String url, TextHttpResponseHandler handler) {
        //客户端
        AsyncHttpClient client = new AsyncHttpClient();
        //get请求
        client.get(url, handler);
    }

    /**
     * post请求数据
     *
     * @param url      链接
     * @param maps     表单
     * @param activity 上下文
     * @param listener 回调接口
     */
    public static void sendQuestPostBackResponse(final String url, final Map<String, String> maps, final Activity activity, final GetSSRListener listener) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final Connection.Response response = Jsoup.connect(url)
                            .data(maps)
                            .method(Connection.Method.POST)
                            .timeout(20000)
                            .execute();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(response.body().toString());
                        }
                    });
                } catch (final IOException e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.error(e);
                        }
                    });
                }
            }
        }.start();
    }
}
