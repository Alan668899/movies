package com.xing.mita.movie.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.adapter.HeadIconAdapter;
import com.xing.mita.movie.dao.option.UserOption;
import com.xing.mita.movie.entity.Head;
import com.xing.mita.movie.entity.User;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2019/1/9
 * @Description 选择头像
 */
public class HeadIconActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.rv_head)
    RecyclerView recyclerView;
    @BindView(R.id.tv_confirm)
    TextView mTvConfirm;

    private HeadIconAdapter adapter;

    private List<Head> list = new ArrayList<>();

    private int lastPos = -1;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_head_icon;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.choose_head);
        mTvConfirm.setClickable(false);

        initRv();
        showLoading();
    }

    @OnClick({R.id.ifv_back, R.id.tv_confirm})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.tv_confirm:
                String url = adapter.getData().get(lastPos).getUrl();
                User user = UserOption.getUser();
                if (user == null) {
                    return;
                }
                user.setHeadIcon(url);
                //更新数据库
                UserOption.update(user);
                //返回数据
                Intent intent = new Intent();
                intent.putExtra("url", url);
                setResult(0, intent);
                finish();
                break;

            default:
                break;
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<Head> newData = (List<Head>) msg.obj;
                    adapter.setNewData(newData);
                    break;

                case 10:
                    View convertView = EmptyViewUtils.setEmptyView(HeadIconActivity.this,
                            adapter,
                            R.layout.empty_request_fail);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showLoading();
                        }
                    });
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 显示加载动画
     */
    private void showLoading() {
        EmptyViewUtils.setEmptyView(this, adapter, R.layout.empty_request_loading);
        getHtml();
    }

    private void getHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Head> list = new ArrayList<>();
                    String url = Constant.SOURCE_HEAD + "1.html";
                    Log.i(TAG, "请求url：" + url);
                    //获取请求连接
                    Connection con = Jsoup.connect(url);
                    //解析请求结果
                    Document doc = con.get();
                    Elements elements = doc.select("dl.egeli_pic_dl");
                    for (Element e : elements) {
                        Element imgElement = e.selectFirst("img");
                        if (imgElement == null) {
                            continue;
                        }
                        String image = imgElement.attr("src");
                        if (TextUtils.isEmpty(image) || !image.contains(".jpg")) {
                            continue;
                        }
                        Head head = new Head(image);
                        list.add(head);
                    }
                    if (list.size() == 0) {
                        throw new Exception("have no data");
                    }
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = list;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.w(TAG, "run 网页解析失败: " + e.getMessage());
                    handler.sendEmptyMessage(10);
                }
            }
        });
    }

    private void initRv() {
        adapter = new HeadIconAdapter(R.layout.adapter_head_icon, list);
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        //重复执行
        adapter.isFirstOnly(false);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (lastPos == position) {
            return;
        }
        List<Head> list = adapter.getData();
        Head head = list.get(position);
        head.setSelect(!head.isSelect());
        adapter.notifyItemChanged(position);
        if (lastPos >= 0) {
            list.get(lastPos).setSelect(false);
            adapter.notifyItemChanged(lastPos);
        }
        lastPos = position;
        canConfirm();
    }

    private void canConfirm() {
        mTvConfirm.setClickable(true);
        mTvConfirm.setTextColor(ContextCompat.getColor(this, R.color.color_blue));
    }

}
