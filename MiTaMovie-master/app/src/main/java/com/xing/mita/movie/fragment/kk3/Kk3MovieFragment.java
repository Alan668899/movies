package com.xing.mita.movie.fragment.kk3;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.kk3.Kk3VideoPlayActivity;
import com.xing.mita.movie.adapter.kk3.Kk3VideoAdapter;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.fragment.common.BaseFragment;
import com.xing.mita.movie.http.HttpUtils;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.LogUtils;
import com.xing.mita.movie.view.CustomLoadMoreView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * @author Mita
 * @date 2018/12/3
 * @Description Kk3电影页面
 */
public class Kk3MovieFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.RequestLoadMoreListener,
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.sl_movie)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.rv_movie)
    RecyclerView mRvMovie;

    private Kk3VideoAdapter adapter;

    private List<Video> list = new ArrayList<>();

    private String link;
    private int page = 1;
    private boolean isLoadMore;

    @Override
    protected int setContentView() {
        return R.layout.fragment_kk3_movie;
    }

    @Override
    protected void init() {
        initSRL();
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected void lazyLoad() {
        link = Objects.requireNonNull(getArguments()).getString("link");
        initRv();
        showLoading();
    }

    /**
     * 显示加载动画
     */
    private void showLoading() {
        mSwipeRefreshLayout.setEnabled(false);
        EmptyViewUtils.setEmptyView(getActivity(), adapter,
                R.layout.empty_request_loading);
        getHtml();
    }

    private void initRv() {
        adapter = new Kk3VideoAdapter(R.layout.adapter_kk3_video, R.layout.adapter_search_result_title, list);
        adapter.setOnItemClickListener(this);
        adapter.setOnLoadMoreListener(this, mRvMovie);
        adapter.setLoadMoreView(new CustomLoadMoreView());
        mRvMovie.setLayoutManager(new LinearLayoutManager(getActivity()));
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        //重复执行
        adapter.isFirstOnly(false);
        mRvMovie.setAdapter(adapter);
    }

    private void getHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String realLink = link;
                    if (page > 1 && realLink.contains(".")) {
                        String str = realLink.substring(0, realLink.lastIndexOf("."));
                        realLink = str + "_" + page + ".html";
                    }
                    String url = Constant.SOURCE_KK3 + realLink;
                    logI("请求url：" + url);
                    String htmlStr = HttpUtils.getKk3HTML(url);
                    if (TextUtils.isEmpty(htmlStr)) {
                        handler.sendEmptyMessage(10);
                        return;
                    }
                    LogUtils.i(TAG, htmlStr);
                    // 从一个URL加载一个Document对象。
                    Document doc = Jsoup.parse(htmlStr);
                    Element ulElement = doc.selectFirst("ul.ui-list");
                    Elements listElement = ulElement.select("a");
                    List<Video> newData = new ArrayList<>();
                    for (Element e : listElement) {
                        String name = e.text();
                        String link = e.attr("href");
                        String date = e.selectFirst("span").text();
                        if (name.length() > date.length()) {
                            name = name.substring(date.length());
                        }
                        logI(name);
                        Video video = new Video(false, "");
                        video.setName(name)
                                .setLink(link)
                                .setScore(date);
                        newData.add(video);
                    }
                    if (newData.size() == 0) {
                        throw new Exception("have no data");
                    }
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = newData;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e(TAG, "run: 加载网页错误:" + e.getMessage());
                    handler.sendEmptyMessage(10);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mSwipeRefreshLayout.isEnabled()) {
            if (isLoadMore) {
                page++;
            } else {
                mSwipeRefreshLayout.setRefreshing(true);
                page = 1;
            }
            getHtml();
        } else {
            showLoading();
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Video video = (Video) adapter.getData().get(position);
        Intent intent = new Intent();
        intent.putExtra("url", video.getLink());
        startWithIntent(Kk3VideoPlayActivity.class, intent);
    }

    @Override
    public void onLoadMoreRequested() {
        isLoadMore = true;
        onRefresh();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    List<Video> newData = (List<Video>) message.obj;
                    if (isLoadMore) {
                        adapter.addData(newData);
                        adapter.loadMoreComplete();
                    } else {
                        adapter.setNewData(newData);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    isLoadMore = false;
                    mSwipeRefreshLayout.setEnabled(true);
                    break;

                case 10:
                    mSwipeRefreshLayout.setRefreshing(false);
                    View convertView = EmptyViewUtils.setEmptyView(getActivity(), adapter,
                            R.layout.empty_request_fail);
                    if (convertView == null) {
                        break;
                    }
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

    private void initSRL() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorAccent);
    }
}
