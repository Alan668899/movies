package com.xing.mita.movie.fragment.aaqq;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqVideoPlayActivity;
import com.xing.mita.movie.adapter.CategoryAdapter;
import com.xing.mita.movie.adapter.VideoAdapter;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.entity.Category;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.fragment.common.BaseFragment;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.SpUtils;
import com.xing.mita.movie.view.CustomLoadMoreView;

import org.jsoup.Connection;
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
 * @date 2018/10/12
 * @Description
 */
public class AaqqMovieFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.RequestLoadMoreListener,
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.sl_movie)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.rv_movie)
    RecyclerView mRvMovie;
    @BindView(R.id.rv_category)
    RecyclerView mRvCategory;

    private VideoAdapter adapter;
    private CategoryAdapter categoryAdapter;

    private List<Video> list = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private int id;
    private int page = 1, totalPage;
    private int lastCategoryPosition;
    private boolean isLoadMore;
    private boolean updateCategory;

    private String currentCategory;

    @Override
    protected int setContentView() {
        return R.layout.fragment_movie;
    }

    @Override
    protected void init() {
        initSRL();
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected void lazyLoad() {
        id = Objects.requireNonNull(getArguments()).getInt("id");
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
        adapter = new VideoAdapter(R.layout.adapter_video, R.layout.adapter_home_title, list);
        adapter.setOnItemClickListener(this);
        adapter.setOnLoadMoreListener(this, mRvMovie);
        adapter.setLoadMoreView(new CustomLoadMoreView());
        mRvMovie.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        //重复执行
        adapter.isFirstOnly(false);
        mRvMovie.setAdapter(adapter);

        categoryAdapter = new CategoryAdapter(R.layout.adapter_category, categoryList);
        categoryAdapter.setOnItemClickListener(this);
        mRvCategory.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        mRvCategory.setAdapter(categoryAdapter);
    }

    private void getHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String order = SpUtils.getVideoOrder(Objects.requireNonNull(getActivity()));
                    if (TextUtils.isEmpty(order)) {
                        order = "time";
                    }
                    String baseUrl = SysApplication.BASE_URL;
                    if (TextUtils.isEmpty(baseUrl)) {
                        baseUrl = Constant.SOURCE_AAQQY_MOBILE;
                    }
                    String url = baseUrl + "/vod-list-id-" + id
                            + "-pg-" + page
                            + "-order--by-" + order
                            + "-class-" + (TextUtils.isEmpty(currentCategory) ? "" : currentCategory)
                            + "-year-0-letter--area--lang-.html";
                    Log.i("xing", "请求url：" + url);
                    //获取请求连接
                    Connection con = Jsoup.connect(url);
                    //解析请求结果
                    Document doc = con.get();

                    //分类
                    updateCategory = categoryList.size() == 0;
                    if (updateCategory) {
                        Elements classifyList = doc.select("div.con").select("a");
                        for (Element element : classifyList) {
                            String name = element.text();
                            String href = element.select("a").first().attr("href");
                            String categoryId = "0";
                            if (!TextUtils.isEmpty(href) && href.contains("class-")
                                    && href.contains("-year")) {
                                categoryId = href.substring(href.indexOf("class-") + 6,
                                        href.indexOf("-year"));
                            }
                            Category category = new Category(name, categoryId);
                            categoryList.add(category);
                        }
                    }

                    //获取最大页码
                    Elements pageElement = doc.select("a.pagelink_b");
                    Element element = pageElement.last();
                    if (null == element) {
                        totalPage = 1;
                    } else {
                        String maxPage = element.text();
                        totalPage = Integer.parseInt(maxPage);
                    }

                    List<Video> newData = new ArrayList<>();
                    //获取影片信息
                    Elements videoList = doc.select("ul.list_tab_img").select("li");
                    for (Element videoElement : videoList) {
                        String name = videoElement.select("a").first().attr("title");
                        String link = videoElement.select("a").first().attr("href");
                        String pic = videoElement.select("img").attr("data-original");
                        if (pic.length() > 5) {
                            String head = pic.substring(0, 5);
                            if (!head.contains("http")) {
                                pic = "http:" + pic;
                            }
                        }
                        String score = videoElement.select("label.score").text();
                        String title = videoElement.select("label.title").text();
                        String actor = videoElement.select("p").text();
                        Video video = new Video(false, "");
                        video.setName(name).setLink(link).setPicture(pic).setScore(score).setTitle(title).setActor(actor);
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
        if (mSwipeRefreshLayout == null) {
            return;
        }
        if (mSwipeRefreshLayout.isEnabled()) {
            if (isLoadMore) {
                if (page >= totalPage) {
                    adapter.loadMoreEnd();
                    isLoadMore = false;
                    return;
                }
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
        if (adapter instanceof VideoAdapter) {
            Video video = (Video) adapter.getData().get(position);
            Intent intent = new Intent();
            intent.putExtra("url", video.getLink());
            intent.putExtra("imgUrl", video.getPicture());
            startWithIntent(AaqqVideoPlayActivity.class, intent);
        } else if (adapter instanceof CategoryAdapter) {
            if (lastCategoryPosition == position) {
                return;
            }
            categoryList.get(lastCategoryPosition).setSelect(false);
            categoryAdapter.notifyItemChanged(lastCategoryPosition);
            categoryList.get(position).setSelect(true);
            categoryAdapter.notifyItemChanged(position);
            currentCategory = categoryList.get(position).getId();
            onRefresh();
            lastCategoryPosition = position;
        }
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
                    if (updateCategory && categoryList.size() > 0) {
                        categoryList.get(0).setSelect(true);
                        categoryAdapter.replaceData(categoryList);
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
