package com.github.www.jump.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.www.jump.R;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {
    //集合
    private List<View> mList = new ArrayList<>();
    private ViewPager mPager;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();
        //获取list
        getList();
        mAdapter = new MyAdapter();
        mPager.setAdapter(mAdapter);
    }

    /**
     * 绑定
     */
    private void initView() {
        mPager = (ViewPager) findViewById(R.id.ac_guide_pager_load);
    }

    /**
     * 获取list
     */
    private void getList() {
        View v1 = LayoutInflater.from(this).inflate(R.layout.guide_1, null);
        View v2 = LayoutInflater.from(this).inflate(R.layout.guide_2, null);
        View v3 = LayoutInflater.from(this).inflate(R.layout.guide_3, null);
        Button btn = v3.findViewById(R.id.item_guide3_btn_jump);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mList.add(v1);
        mList.add(v2);
        mList.add(v3);
        v1 = null;
        v2 = null;
        v3 = null;
    }

    /**
     * 滑动适配器
     */
    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mList.get(position));
            return mList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
