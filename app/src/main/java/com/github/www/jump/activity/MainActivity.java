package com.github.www.jump.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.www.jump.R;
import com.github.www.jump.bean.DataBean;
import com.github.www.jump.listener.GetSSRListener;
import com.github.www.jump.utils.BasicUtils;
import com.github.www.jump.utils.HttpUtils;
import com.github.www.jump.utils.PrivateUtils;
import com.loopj.android.http.TextHttpResponseHandler;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private List<DataBean> mBack = new ArrayList<>();
    private List<DataBean> mBeen = new ArrayList<>();
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private ActionBar mActionBar;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //更新数据完毕，显示数据
                    mBeen = new ArrayList<>(mBack);
                    initAdapter();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initView
        initView();
        //设置title
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        //获取key
        getKey();
    }

    private void initAdapter() {
        if (mAdapter == null) {
            //第一次
            mAdapter = new MyAdapter();
            GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 2);
            mRecyclerView.setLayoutManager(manager);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            //刷新数据
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取key
     */
    private void getKey() {
        //请求数据
        HttpUtils.sendQuestGetBackResponse(PrivateUtils.URL_SSR, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //获取密码失败，暂时不可用
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                //id:problem
                Document document = Jsoup.parse(responseString);
                Element problem = document.getElementById("problem");
                String text = problem.text();
                showDialog(text);
            }
        });
    }

    /**
     * 提问
     *
     * @param text 问题
     */
    private void showDialog(String text) {
        final EditText editText = new EditText(MainActivity.this);
        editText.setSingleLine();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("验证:  " + text).setView(editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String data = editText.getText().toString();
                PrivateUtils.PASS_VALUE = data;
                //下载数据
                getData();
            }
        }).setCancelable(false).show();
    }

    /**
     * 获取数据
     */
    private void getData() {
        Map<String, String> map = new HashMap<>();
        map.put(PrivateUtils.PASS_KEY, PrivateUtils.PASS_VALUE);
        HttpUtils.sendQuestPostBackResponse(PrivateUtils.URL_SSR, map, MainActivity.this, new GetSSRListener() {
            @Override
            public void error(Exception e) {
                //未获取到数据
                Toast.makeText(MainActivity.this, "未获取到数据", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void success(String data) {
                if (data.equals("-1")) {
                    Toast.makeText(MainActivity.this, "未获取到数据", Toast.LENGTH_SHORT).show();
                    return;
                }
                //获取到数据
                //解析
                document(data);
            }
        });
    }

    /**
     * 解析数据
     *
     * @param data HTML文档
     */
    private void document(String data) {
        Document document = Jsoup.parse(data);
        //获取table，获取不到就是密码输入错误
        try {
            Element table = document.select("table").get(0);
            //获取所有的行,第一行不存在
            Elements trs = table.select("tr");
            for (Element tr : trs) {
                //获取所有的td
                Elements tds = tr.select("td");
                if (tds.size() != 0) {
                    //开始获取数据
                    resolution(tds);
                }
            }
            //更新完毕★★★
            mHandler.sendEmptyMessage(1);

        } catch (Exception e) {
            TSnackbar.make(mToolbar, "验证错误,重新输入", TSnackbar.LENGTH_LONG, TSnackbar.APPEAR_FROM_TOP_TO_DOWN).setPromptThemBackground(Prompt.ERROR).show();
            //重新请求key
            getKey();
            return;
        }

    }

    /**
     * 解析每行的所有单元格
     *
     * @param tds 所有的单元格
     */
    private void resolution(Elements tds) {
        DataBean bean = new DataBean();
        //便利循环
        for (int i = 0; i < tds.size(); i++) {
            //获取当前td
            Element td = tds.get(i);
            if (i == 0) {
                //服务器地址
                bean.setServiceAddr(td.text());
                //打印
                BasicUtils.printLog(td.text());
                setCountry(bean, td);
            } else if (i == 1) {
                //服务器ip
                String html = td.html();
                if (html.contains("del")) {
                    bean.setServiceIp("无流量,请更换");
                } else {
                    bean.setServiceIp(td.text());
                }
            } else if (i == 2) {
                //服务器端口号
                bean.setPost(td.text());
            } else if (i == 4) {
                //加密方式
                bean.setMethod(td.text());
            } else if (i == 5) {
                //捐献者名字
                bean.setName(td.text());
            } else if (i == 6) {
                //链接
                Element aa = null;
                Elements as = td.select("a");
                if (as.size() == 2) {
                    //选最后一个
                    aa = as.get(1);
                } else {
                    aa = as.get(0);
                }
                //获取链接
                String link = aa.attr("href");
                bean.setSsLink(link.replace("http://doub.pw/qr/qr.php?text=", ""));
                //提交到list
                mBack.add(bean);
            }

        }
    }

    /**
     * 设置当前的国家和标识位
     *
     * @param bean 实体类
     * @param td   国家
     */
    private void setCountry(DataBean bean, Element td) {
        String country = td.text();
        if (country.contains("美国")) {
            //美国
            bean.setCountry(R.drawable.flag_usa);
            bean.setCode(1);

        } else if (country.contains("日本")) {
            //日本
            bean.setCountry(R.drawable.flag_japan);
            bean.setCode(2);

        } else if (country.contains("俄罗斯")) {
            //俄罗斯
            bean.setCountry(R.drawable.flag_russia);
            bean.setCode(-1);

        } else if (country.contains("香港") || country.contains("澳门") || country.contains("台湾") ||
                country.contains("中国") || country.contains("北京") || country.contains("上海") ||
                country.contains("青岛")) {
            //中国
            bean.setCountry(R.drawable.flag_china);
            bean.setCode(0);

        } else if (country.contains("加拿大")) {
            //加拿大
            bean.setCountry(R.drawable.flag_canada);
            bean.setCode(3);

        } else if (country.contains("韩国")) {
            //韩国
            bean.setCountry(R.drawable.defaulf);
            bean.setCode(-1);

        } else if (country.contains("新加坡")) {
            //新加坡
            bean.setCountry(R.drawable.flag_singapore);
            bean.setCode(4);

        } else if (country.contains("法国")) {
            //法国
            bean.setCountry(R.drawable.defaulf);
            bean.setCode(-1);

        } else if (country.contains("罗马尼亚")) {
            //罗马尼亚
            bean.setCountry(R.drawable.defaulf);
            bean.setCode(-1);

        } else if (country.contains("澳大利亚")) {
            //澳大利亚
            bean.setCountry(R.drawable.defaulf);
            bean.setCode(-1);

        } else if ((country.contains("德国"))) {
            //德国
            bean.setCountry(R.drawable.defaulf);
            bean.setCode(-1);

        } else {
            bean.setCountry(R.drawable.defaulf);
            bean.setCode(-1);

        }
    }

    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //菜单点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_ssr:
                //安装ssr
                Toast.makeText(MainActivity.this, "安装SSR", Toast.LENGTH_SHORT).show();
                break;

            case R.id.main_menu_use:
                //使用说明
                break;

            case R.id.main_menu_wo:
                //关于我们
                break;

            case R.id.main_menu_all:
                //全部
                mBeen = new ArrayList<>(mBack);
                mActionBar.setTitle("Jump");
                initAdapter();
                break;

            case R.id.main_menu_cn:
                //中国
                getBeenData(0);
                mActionBar.setTitle("中国");
                initAdapter();
                break;

            case R.id.main_menu_usa:
                //美国
                getBeenData(1);
                mActionBar.setTitle("美国");
                initAdapter();
                break;

            case R.id.main_menu_jp:
                //日本
                getBeenData(2);
                mActionBar.setTitle("日本");
                initAdapter();
                break;

            case R.id.main_menu_can:
                //加拿大
                getBeenData(3);
                mActionBar.setTitle("加拿大");
                initAdapter();
                break;

            case R.id.main_menu_sing:
                //新加坡
                getBeenData(4);
                mActionBar.setTitle("新加坡");
                initAdapter();
                break;

        }
        return true;
    }

    /**
     * 获取每个地区的ss
     *
     * @param i 参数
     */
    private void getBeenData(int i) {
        mBeen.clear();
        for (int a = 0; a < mBack.size(); a++) {
            DataBean bean = mBack.get(a);
            int code = bean.getCode();
            if (code == i) {
                mBeen.add(bean);
            }
        }
    }


    /**
     * 绑定id
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.ac_main_bar_title);
        mRecyclerView = (RecyclerView) findViewById(R.id.ac_main_recycler_show);
    }

    /**
     * adapter
     */
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
            final ViewHolder viewHolder = new ViewHolder(inflate);
            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = viewHolder.getAdapterPosition();
                    DataBean bean = mBeen.get(position);
                    ShowActivity.jumpActivity(MainActivity.this, bean.getServiceAddr(), bean.getServiceIp(), bean.getPost(), bean.getMethod(),
                            bean.getCountry(), bean.getSsLink());
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DataBean bean = mBeen.get(position);
            holder.ip.setText(bean.getServiceIp());
            holder.name.setText(bean.getServiceAddr());
            holder.img.setImageResource(bean.getCountry());
        }

        @Override
        public int getItemCount() {
            return mBeen.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            //ip地址
            TextView ip;
            //服务器地址
            TextView name;
            //国家图片
            ImageView img;

            public ViewHolder(View itemView) {
                super(itemView);
                ip = itemView.findViewById(R.id.item_show_ip);
                name = itemView.findViewById(R.id.item_show_name);
                img = itemView.findViewById(R.id.item_show_img);
            }
        }
    }



}
