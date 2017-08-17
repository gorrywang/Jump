package com.github.www.jump.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.www.jump.R;
import com.zxing.support.library.qrcode.QRCodeEncode;

public class ShowActivity extends AppCompatActivity {

    private TextView mTextIp, mTextPost, mTextMethod;
    private ImageView mImgQr, mImgCountry;
    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private String mSSRLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        initView();
        setBar();
        getData();
    }

    /**
     * 获取数据
     */
    private void getData() {
        Intent intent = getIntent();
        mTextIp.setText(intent.getStringExtra("serviceIp"));
        mTextPost.setText(intent.getStringExtra("post"));
        mTextMethod.setText(intent.getStringExtra("method"));
        mActionBar.setTitle(intent.getStringExtra("serviceAddr"));
        mImgCountry.setImageResource(intent.getIntExtra("country", R.mipmap.ic_launcher));
        mSSRLink = intent.getStringExtra("ssLink");
        //设置二维码
        mImgQr.setImageBitmap(getBitmap());
    }

    private Bitmap getBitmap() {
        QRCodeEncode.Builder builder = new QRCodeEncode.Builder();
        builder.setBackgroundColor(0xffffff)
                .setOutputBitmapHeight(800)
                .setOutputBitmapWidth(800)
                .setOutputBitmapPadding(10);
        Bitmap qrCodeBitmap = builder.build().encode(mSSRLink);
        return qrCodeBitmap;
    }

    private void setBar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * 初始化参数
     */
    private void initView() {
        mTextIp = (TextView) findViewById(R.id.ac_show_textView_ip);
        mTextPost = (TextView) findViewById(R.id.ac_show_textView_post);
        mTextMethod = (TextView) findViewById(R.id.ac_show_textView_jm);
        mImgQr = (ImageView) findViewById(R.id.ac_show_img_qr);
        mImgCountry = (ImageView) findViewById(R.id.ac_show_img_country);
        mToolbar = (Toolbar) findViewById(R.id.ac_show_bar_title);
    }

    public static void jumpActivity(Context context, String serviceAddr, String serviceIp, String post, String method, int country, String ssLink) {
        Intent intent = new Intent(context, ShowActivity.class);
        intent.putExtra("serviceAddr", serviceAddr);
        intent.putExtra("serviceIp", serviceIp);
        intent.putExtra("post", post);
        intent.putExtra("method", method);
        intent.putExtra("country", country);
        intent.putExtra("ssLink", ssLink);
        context.startActivity(intent);
    }


    /**
     * 复制二维码
     *
     * @param view
     */
    public void copySS(View view) {
        if (mSSRLink == null) {
            //加载完在数据在复制
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(mSSRLink);
        Toast.makeText(ShowActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 导入
     * <action android:name="android.intent.action.VIEW"/>
     * <category android:name="android.intent.category.DEFAULT"/>
     * <category android:name="android.intent.category.BROWSABLE"/>
     *
     * @param view
     */
    public void impotSS(View view) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.setData(Uri.parse(mSSRLink));
            startActivity(intent);
            Toast.makeText(ShowActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ShowActivity.this, "请点击主页的小飞机安装SSR", Toast.LENGTH_SHORT).show();
        }
    }


}
