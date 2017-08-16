package com.github.www.jump.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 基础配置
 * Created by iswgr on 2017/8/16.
 */

public class BasicUtils {
    /**
     * 打印开关
     */
    private static boolean mLogBool = true;

    /**
     * 安装广播
     */
    public static final String CAST_INSTALL = "com.github.www.jump.install";
    public static final String CAST_RESET = "com.github.www.jump.reset";

    /**
     * 打印日志
     *
     * @param data 打印内容
     */
    public static void printLog(String data) {
        if (mLogBool) {
            Log.e("print", data);
        }
    }

    /**
     * 程序是否第一次启动
     *
     * @param context 上下文
     * @return 是否第一次启动
     */
    public static boolean firstRun(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        boolean is = sharedPreferences.getBoolean("is", true);
        if (is) {
            //第一次启动
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("is", false);
            edit.commit();
        }
        return is;
    }
}
