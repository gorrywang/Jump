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

    /**
     * 获取数据是否存在
     *
     * @param context 上下文
     * @param key     问题
     * @return 密码
     */
    public static String getValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getString(key, "jump");
    }

    /**
     * 保存密码
     *
     * @param context 上下文
     * @param key     问题
     * @param value   密码
     */
    public static void setValue(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String data = sp.getString(key, "jump");
        if (data.equals(value)) {
            //不用保存
        } else {
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(key, value);
            edit.commit();
        }
    }
}
