package com.xing.mita.movie.activity.aaqq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.common.BaseActivity;
import com.xing.mita.movie.activity.gqzy.GqzySearchActivity;
import com.xing.mita.movie.activity.gqzy.GqzyVideoPlayActivity;
import com.xing.mita.movie.adapter.HotListAdapter;
import com.xing.mita.movie.adapter.SearchResultAdapter;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.dao.option.HotListOption;
import com.xing.mita.movie.dao.option.SearchHistoryOption;
import com.xing.mita.movie.entity.HotList;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.http.HttpUtils;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EditTextUtil;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.SoftHideKeyBoardUtil;
import com.xing.mita.movie.view.IconFontView;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * @author Mita
 * @date 2018/10/24
 * @Description 搜索页
 */
public class AaqqSearchActivity extends BaseActivity implements TagFlowLayout.OnTagClickListener,
        BaseQuickAdapter.RequestLoadMoreListener,
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.rv_hot)
    RecyclerView mRecyclerView;
    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.tv_search)
    TextView mTvSearch;
    @BindView(R.id.ifv_clear)
    IconFontView mIfvClear;
    @BindView(R.id.tv_search_history)
    TextView mTvSearchHistory;
    @BindView(R.id.ifv_delete)
    IconFontView mIfvDelete;
    @BindView(R.id.tv_newest)
    TextView mTvNewest;
    @BindView(R.id.flow_layout)
    TagFlowLayout mFlowLayout;

    private SearchResultAdapter searchResultAdapter;

    private List<String> keywordList;

    private int page, totalPage;
    private String keyword;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_search;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        SoftHideKeyBoardUtil.assistActivity(this);
        initHistory();
        initRv();

        String searchName = getIntent().getStringExtra("searchName");
        if (!TextUtils.isEmpty(searchName)) {
            mEtSearch.setHint(searchName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                EditTextUtil.searchPoint(AaqqSearchActivity.this, mEtSearch);
            }
        }, 500);
    }

    @OnClick({R.id.ifv_back, R.id.tv_search, R.id.et_search, R.id.ifv_delete, R.id.ifv_clear})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                EditTextUtil.losePoint(this, mEtSearch);
                finish();
                break;

            case R.id.et_search:
                EditTextUtil.searchPoint(this, mEtSearch);
                break;

            case R.id.tv_search:
                EditTextUtil.losePoint(this, mEtSearch);
                doSearch();
                break;

            case R.id.ifv_delete:
                EditTextUtil.losePoint(this, mEtSearch);
                SearchHistoryOption.clearHistory();
                showHideSearchHistory(false);
                break;

            case R.id.ifv_clear:
                mEtSearch.getText().clear();
                break;

            default:
                break;
        }
    }

    @OnEditorAction(R.id.et_search)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean hasKeyword = mEtSearch.getText().length() > 0 || mEtSearch.getHint().length() > 0;
        if (actionId == EditorInfo.IME_ACTION_SEARCH && hasKeyword) {
            doSearch();
            EditTextUtil.losePoint(AaqqSearchActivity.this, mEtSearch);
        }
        return true;
    }

    @OnTextChanged(value = R.id.et_search, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s) {
        mEtSearch.setHint("");
        int len = s.length();
        boolean hasKeyword = len > 0;
        //搜索状态变更
        mTvSearch.setTextColor(ContextCompat.getColor(this,
                hasKeyword ? R.color.black : R.color.color_99));
        //清除按钮显隐变更
        mIfvClear.setVisibility(hasKeyword ? View.VISIBLE : View.GONE);
        mEtSearch.setImeOptions(
                hasKeyword ? EditorInfo.IME_ACTION_SEARCH : EditorInfo.IME_ACTION_DONE);
        mEtSearch.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        if (len == 0) {
            initHistory();
            initRv();
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<Video> newList = (List<Video>) msg.obj;
                    if (page == 1 && (newList == null || newList.size() == 0)) {
                        EmptyViewUtils.setEmptyView(AaqqSearchActivity.this,
                                searchResultAdapter, R.layout.empty_content);
                        break;
                    }
                    if (newList == null) {
                        searchResultAdapter.loadMoreFail();
                        break;
                    }
                    if (page == 1) {
                        Video video = new Video(true, "");
                        video.setTitle("首播")
                                .setTitleImg(R.mipmap.icon_logo_aaqq)
                                .setSource(Constant.SOURCE_AAQQY)
                                .setType(0);
                        newList.add(0, video);
                    }
                    searchResultAdapter.addData(newList);
                    searchResultAdapter.loadMoreComplete();
                    searchResultAdapter.setOnLoadMoreListener(
                            AaqqSearchActivity.this, mRecyclerView);

                    if (page >= totalPage) {
                        searchResultAdapter.loadMoreEnd();
                    }
                    break;

                case 1:
                    newList = (List<Video>) msg.obj;
                    if (newList == null || newList.size() == 0) {
                        break;
                    }
                    List<Video> videoList = new ArrayList<>();
                    if (newList.size() > 5) {
                        videoList.addAll(newList.subList(0, 5));
                    } else {
                        videoList.addAll(newList);
                    }
                    Video video = new Video(true, "");
                    video.setTitle("高清资源网")
                            .setLink(Constant.SEARCH_GQZY + keyword)
                            .setTitleImg(R.mipmap.icon_logo_gqzy)
                            .setType(0)
                            .setSource(Constant.SOURCE_GQZY);
                    videoList.add(0, video);
                    if (searchResultAdapter != null) {
                        searchResultAdapter.addData(0, videoList);
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    private void doSearch() {
        mTvSearch.setClickable(true);
        keyword = mEtSearch.getText().toString();
        String hint = mEtSearch.getHint().toString();
        if (TextUtils.isEmpty(keyword) && !TextUtils.isEmpty(hint)) {
            keyword = hint;
            mEtSearch.setText(keyword);
            mEtSearch.setSelection(keyword.length());
        } else if (TextUtils.isEmpty(keyword) && TextUtils.isEmpty(hint)) {
            mTvSearch.setClickable(false);
            return;
        }
        page = 1;
        //保存搜索关键词
        SearchHistoryOption.saveKeyword(keyword);
        showHideSearchHistory(false);
        initResultRv();
        EmptyViewUtils.setEmptyView(this, searchResultAdapter, R.layout.empty_request_loading);
        request();
        //高清资源网
        requestGqzy();
    }

    private void requestGqzy() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                requestGqzyHtml();
            }
        });
    }

    private void request() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                requestHtml();
            }
        });
    }

    private void requestGqzyHtml() {
        try {
            String url = Constant.SEARCH_GQZY + keyword;
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
                video.setName(name).setLink(link).setScore(date).setSource(Constant.SOURCE_GQZY).setType(1);
                videoList.add(video);
            }
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = videoList;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestHtml() {
        try {
            String baseUrl = SysApplication.BASE_URL;
            if (TextUtils.isEmpty(baseUrl)) {
                baseUrl = Constant.SOURCE_AAQQY_MOBILE;
            }
            String url = baseUrl + "/vod-search-pg-" + page +
                    "-wd-" + keyword + ".html";
            log("请求的url：" + url);
            //获取请求连接
            Connection con = Jsoup.connect(url);
            //解析请求结果
            Document doc = con.get();
            Elements elements = doc.select("ul.new_tab_img").select("li");
            List<Video> videoList = new ArrayList<>();
            for (Element element : elements) {
                Element titleElement = element.selectFirst("a");
                if (titleElement == null) {
                    continue;
                }
                String name = titleElement.attr("title");
                String link = titleElement.attr("href");
                String img = titleElement.selectFirst("img").attr("data-original");
                if (img.length() > 5) {
                    String head = img.substring(0, 5);
                    if (!head.contains("http")) {
                        img = "http:" + img;
                    }
                }

                Element infoElement = element.select("div").last();
                if (infoElement == null) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                Elements detailElement = infoElement.select("p");
                for (Element detail : detailElement) {
                    String label = detail.text();
                    if (!TextUtils.isEmpty(label) && !label.contains("分类") && label.length() > 3) {
                        if (label.length() > 17) {
                            label = label.substring(0, 17);
                        }
                        sb.append(label);
                        if (!label.contains("时间")) {
                            sb.append("\n");
                        }
                    }
                }
                String label = sb.toString();
                Video video = new Video(false, "");
                video.setName(name).setLink(link).setPicture(img).setActor(label).setType(2);
                videoList.add(video);
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

            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = videoList;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRv() {
        List<HotList> list = HotListOption.getHotList(Constant.SOURCE_AAQQY);
        HotListAdapter adapter = new HotListAdapter(R.layout.adapter_hotlist, list);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRecyclerView.setLayoutManager(llm);

        searchResultAdapter = null;
        mTvNewest.setText("最近更新");
        mTvNewest.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化搜索结果Rv
     */
    private void initResultRv() {
        List<Video> videoList = new ArrayList<>();
        searchResultAdapter = new SearchResultAdapter(videoList);
        searchResultAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(searchResultAdapter);
        final LinearLayoutManager llm = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRecyclerView.setLayoutManager(llm);

        mTvNewest.setVisibility(View.GONE);
    }

    private void initHistory() {
        keywordList = SearchHistoryOption.getAllKeyword();
        if (keywordList == null || keywordList.size() == 0) {
            showHideSearchHistory(false);
            return;
        }
        showHideSearchHistory(true);
        Collections.reverse(keywordList);
        mFlowLayout.setAdapter(new TagAdapter<String>(keywordList) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) LayoutInflater.from(AaqqSearchActivity.this)
                        .inflate(R.layout.layout_search_hot_tag, mFlowLayout, false);
                tv.setText(s);
                return tv;
            }
        });
        mFlowLayout.setOnTagClickListener(this);
    }

    @Override
    public boolean onTagClick(View view, int position, FlowLayout parent) {
        EditTextUtil.losePoint(this, mEtSearch);
        keyword = keywordList.get(position);
        mEtSearch.setText(keyword);
        //将光标移至文字末尾
        mEtSearch.setSelection(keyword.length());
        doSearch();
        return false;
    }

    /**
     * 显示（隐藏）历史记录
     *
     * @param hasHistory boolean
     */
    private void showHideSearchHistory(boolean hasHistory) {
        int visibility = hasHistory ? View.VISIBLE : View.GONE;
        mTvSearchHistory.setVisibility(visibility);
        mIfvDelete.setVisibility(visibility);
        mFlowLayout.setVisibility(visibility);
    }

    @Override
    public void onLoadMoreRequested() {
        page++;
        request();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        String link, img = null;
        if (searchResultAdapter == null) {
            HotList hot = (HotList) adapter.getData().get(position);
            link = hot.getLink();
        } else {
            Video video = (Video) adapter.getData().get(position);
            link = video.getLink();
            img = video.getPicture();
            if (video.isHeader) {
                Intent intent = new Intent();
                intent.putExtra("url", link);
                startWithIntent(GqzySearchActivity.class, intent);
                return;
            } else if (TextUtils.equals(video.getSource(), Constant.SOURCE_GQZY)) {
                Intent intent = new Intent();
                intent.putExtra("url", link);
                startWithIntent(GqzyVideoPlayActivity.class, intent);
                return;
            }
        }
        Intent intent = new Intent();
        intent.putExtra("url", link);
        intent.putExtra("imgUrl", img);
        startWithIntent(AaqqVideoPlayActivity.class, intent);
    }
}
