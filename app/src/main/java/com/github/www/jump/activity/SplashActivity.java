package com.github.www.jump.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.www.jump.R;
import com.github.www.jump.gson.VersionGson;
import com.github.www.jump.service.DownloadService;
import com.github.www.jump.utils.BasicUtils;
import com.github.www.jump.utils.HttpUtils;
import com.github.www.jump.utils.PrivateUtils;
import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

import java.io.File;

import cz.msebera.android.httpclient.Header;

import static com.github.www.jump.utils.BasicUtils.CAST_INSTALL;
import static com.github.www.jump.utils.BasicUtils.CAST_RESET;

public class SplashActivity extends AppCompatActivity {
    private TextView mTextJump;
    //动画停留时间，最少3秒
    private long mStartTime, mEndTime;
    //获取当前app版本
    private int mNowVersion;
    //广播
    private MyCast mMyCast;

    private DownloadService.MyBinder mBinder;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = (DownloadService.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //绑定id
        initView();
        //开始时间
        mStartTime = System.currentTimeMillis();
        //获取当前app版本
        mNowVersion = getNowVersion();
        BasicUtils.printLog("当前程序的版本号:" + mNowVersion);
        //申请权限
        questPermissions();
    }

    /**
     * 绑定id
     */
    private void initView() {
        mTextJump = (TextView) findViewById(R.id.ac_splash_txt_jump);
    }

    //联网获取版本
    private void getOnlineVersion() {
        HttpUtils.sendQuestGetBackResponse(PrivateUtils.URL_VERSION, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                BasicUtils.printLog("未获取到version文件");
//                TSnackbar.make(mTextJump, "网络未连接,禁止登陆", TSnackbar.LENGTH_LONG, TSnackbar.APPEAR_FROM_TOP_TO_DOWN).setPromptThemBackground(Prompt.WARNING).show();
                getOnlineVersion();
                return;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                BasicUtils.printLog("version文件获取成功");
                VersionGson versionGson = new Gson().fromJson(responseString, VersionGson.class);
                compare(versionGson);
            }
        });
    }

    /**
     * 申请权限
     */
    private void questPermissions() {
        //申请权限
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //无权限
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }
        //有权限
        //联网获取version
        getOnlineVersion();
    }

    //权限返回
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                //判断
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //联网获取version
                    getOnlineVersion();
                } else {
                    //不同意
                    TSnackbar.make(mTextJump, "请允许权限", TSnackbar.LENGTH_SHORT, TSnackbar.APPEAR_FROM_TOP_TO_DOWN).setPromptThemBackground(Prompt.WARNING).show();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(1800);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    questPermissions();
                                }
                            });
                        }
                    }.start();
                    return;
                }
                break;
        }
    }

    /**
     * 启动下载服务
     */
    private void startService() {
        Intent intent = new Intent(SplashActivity.this, DownloadService.class);
        startService(intent);
        //绑定服务
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    /**
     * 比较版本数据
     *
     * @param versionGson version实体类
     */
    private void compare(VersionGson versionGson) {
        //获取服务器开关
        if (versionGson.getOpen() != 1) {
            BasicUtils.printLog("服务器已暂停服务");
            TSnackbar.make(mTextJump, "服务器已暂停服务", TSnackbar.LENGTH_LONG, TSnackbar.APPEAR_FROM_TOP_TO_DOWN).setPromptThemBackground(Prompt.ERROR).show();
            return;
        }
        //版本是否过于老旧
        if (versionGson.getOldversion() >= mNowVersion) {
            BasicUtils.printLog("版本过老,暂停服务");
            TSnackbar.make(mTextJump, "版本过老,暂停服务", TSnackbar.LENGTH_LONG, TSnackbar.APPEAR_FROM_TOP_TO_DOWN).setPromptThemBackground(Prompt.ERROR).show();
            return;
        }
        //判断版本
        if (versionGson.getVersion() > mNowVersion) {
            BasicUtils.printLog("需要更新");
            //开启广播
            startCast();
            //启动服务
            startService();
            //对话框提示,需要更新
            showDialog(versionGson);
        } else {
            //不需要更新,直接进入首页
            BasicUtils.printLog("不需要更新");
            jumpActivity();
        }
    }

    /**
     * 开启广播
     */
    private void startCast() {
        mMyCast = new MyCast();
        IntentFilter filter = new IntentFilter(CAST_INSTALL);
        filter.addAction(CAST_RESET);
        registerReceiver(mMyCast, filter);
    }

    /**
     * 跳转到新界面
     */
    private void jumpActivity() {
        //最后的时间
        mEndTime = System.currentTimeMillis();
        final long time = mEndTime - mStartTime;
        BasicUtils.printLog("前后时间：" + time);
        if (time < 3000) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(3000 - time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //跳转
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            if (BasicUtils.firstRun(SplashActivity.this)) {
                                //第一次运行
                                intent.setClass(SplashActivity.this, GuideActivity.class);
                            } else {
                                intent.setClass(SplashActivity.this, MainActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }.start();
        } else {
            Intent intent = new Intent();
            if (BasicUtils.firstRun(SplashActivity.this)) {
                //第一次运行
                intent.setClass(SplashActivity.this, GuideActivity.class);
            } else {
                intent.setClass(SplashActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消广播
        if (mMyCast != null) {
            unregisterReceiver(mMyCast);
        }
        //解除绑定
        if (mBinder != null) {
            unbindService(mConnection);
            mConnection = null;
            mBinder = null;
        }
    }

    /**
     * 显示下载对话框
     *
     * @param versionGson 实体类
     */
    private void showDialog(final VersionGson versionGson) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setTitle("更新提示").setMessage(versionGson.getDesc()).setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BasicUtils.printLog("开启更新服务");
                        //更新服务
                        mBinder.startDownload(versionGson.getUrl());
                    }
                }).setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BasicUtils.printLog("不更新，跳转到主界面");
                //跳转到新界面
                jumpActivity();
            }
        }).show();
    }

    /**
     * 获取app版本
     *
     * @return 版本号
     */
    private int getNowVersion() {
        PackageManager manager = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = manager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionCode;
    }

    /**
     * 广播
     */
    private class MyCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CAST_INSTALL)) {
                BasicUtils.printLog("收到更新广播");
                //安装广播
                install(intent.getStringExtra("url"), context);
            } else if (action.equals(CAST_RESET)) {
                //下载失败，重新下载
                //更新服务
                TSnackbar.make(mTextJump, "网络原因下载失败，正在重新下载", TSnackbar.LENGTH_LONG, TSnackbar.APPEAR_FROM_TOP_TO_DOWN).setPromptThemBackground(Prompt.ERROR).show();
                mBinder.startDownload(intent.getStringExtra("url"));
            }
        }

        /**
         * 安装方法
         *
         * @param downloadUrl 下载链接
         * @param context     上下文
         */
        private void install(String downloadUrl, Context context) {
            //文件名
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            //路径
            String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            //创建File对象
            File file = new File(fileDir, fileName);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
                //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
                Uri apkUri =
                        FileProvider.getUriForFile(context, "com.github.www.jump.fileprovider", file);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
        }
    }
}
