package com.xing.mita.movie.activity.gqzy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.common.BaseActivity;
import com.xing.mita.movie.adapter.kk3.Kk3VideoAdapter;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.http.HttpUtils;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.view.CustomLoadMoreView;

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
 * @date 2019/1/5
 * @Description 高清资源网搜索结果
 */
public class GqzySearchActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.rv_search)
    RecyclerView mRvSearch;

    private Kk3VideoAdapter adapter;

    private List<Video> list = new ArrayList<>();

    @Override
    public int getContentViewResId() {
        return R.layout.activity_gqzy_search_result;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText("高清资源网搜索结果");

        initRv();
        showLoading();
    }

    @OnClick(R.id.ifv_back)
    public void click(View v) {
        finish();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Video video = (Video) adapter.getData().get(position);
        Intent intent = new Intent();
        intent.putExtra("url", video.getLink());
        startWithIntent(GqzyVideoPlayActivity.class, intent);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<Video> newData = (List<Video>) msg.obj;
                    adapter.setNewData(newData);
                    break;

                case 10:
                    View convertView = EmptyViewUtils.setEmptyView(GqzySearchActivity.this,
                            adapter, R.layout.empty_request_fail);
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
     * 发起请求并显示加载动画
     */
    private void showLoading() {
        EmptyViewUtils.setEmptyView(this, adapter, R.layout.empty_request_loading);
        requestGqzyHtml();
    }

    private void initRv() {
        adapter = new Kk3VideoAdapter(R.layout.adapter_kk3_video,
                R.layout.adapter_search_result_title, list);
        adapter.setOnItemClickListener(this);
        adapter.setLoadMoreView(new CustomLoadMoreView());
        mRvSearch.setLayoutManager(new LinearLayoutManager(this));
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        //重复执行
        adapter.isFirstOnly(false);
        mRvSearch.setAdapter(adapter);
    }

    private void requestGqzyHtml() {
        final String url = getIntent().getStringExtra("url");
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    log("请求的url：" + url);
                    String htmlStr = HttpUtils.getHTML(url, false);
                    if (TextUtils.isEmpty(htmlStr)) {
                        handler.sendEmptyMessage(10);
                        return;
                    }
                    //从一个URL加载一个Document对象。
                    Document doc = Jsoup.parse(htmlStr);
                    Elements elements = doc.select("ul.nr");
                    List<Video> videoList = new ArrayList<>();
                    for (Element element : elements) {
                        Element titleElement = element.selectFirst("a.name");
                        if (titleElement == null) {
                            continue;
                        }
                        String name = titleElement.text();
                        String link = titleElement.attr("href");
                        String date = element.selectFirst("span.hours").text();

                        Video video = new Video(false, "");
                        video.setName(name).setLink(link).setScore(date).setSource(Constant.SOURCE_GQZY);
                        videoList.add(video);
                    }
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = videoList;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
