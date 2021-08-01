package com.xing.mita.movie.activity.kk3;

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
import com.xing.mita.movie.adapter.kk3.Kk3VideoAdapter;
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
public class Kk3SearchActivity extends BaseActivity implements TagFlowLayout.OnTagClickListener,
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

    private Kk3VideoAdapter searchResultAdapter;

    private List<String> keywordList;

    private int page = 1, totalPage;
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
                EditTextUtil.searchPoint(Kk3SearchActivity.this, mEtSearch);
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
            EditTextUtil.losePoint(Kk3SearchActivity.this, mEtSearch);
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
                        EmptyViewUtils.setEmptyView(Kk3SearchActivity.this,
                                searchResultAdapter, R.layout.empty_content);
                        break;
                    }
                    if (newList == null) {
                        searchResultAdapter.loadMoreFail();
                        break;
                    }
                    if (searchResultAdapter == null) {
                        break;
                    }
                    if (page == 1) {
                        Video video = new Video(true, "KK3");
                        video.setTitle("KK3")
                                .setTitleImg(R.mipmap.icon_logo_kk3)
                                .setSource(Constant.SOURCE_KK3);
                        newList.add(0, video);
                    }
                    searchResultAdapter.addData(newList);
                    searchResultAdapter.loadMoreComplete();
                    searchResultAdapter.setOnLoadMoreListener(
                            Kk3SearchActivity.this, mRecyclerView);

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
                    Video v = new Video(true, "");
                    v.setTitle("高清资源网")
                            .setLink(Constant.SEARCH_GQZY + keyword)
                            .setTitleImg(R.mipmap.icon_logo_gqzy)
                            .setSource(Constant.SOURCE_GQZY);
                    videoList.add(0, v);
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
        EmptyViewUtils.setEmptyView(this, searchResultAdapter,
                R.layout.empty_request_loading);
        //高清资源网
        requestGqzy();
        //kk3
        requestKk3();
    }

    private void requestGqzy() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                requestGqzyHtml();
            }
        });
    }

    private void requestKk3() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                requestKk3Html();
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
                video.setName(name).setLink(link).setScore(date).setSource(Constant.SOURCE_GQZY);
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

    private void requestKk3Html() {
        try {
            String url = Constant.SOURCE_KK3 + "search.asp?page=" + page +
                    "&searchword=" + keyword;
            url = HttpUtils.encodeUrl(url);
            log("请求的url：" + url);
            String htmlStr = HttpUtils.getKk3HTML(url);
            if (TextUtils.isEmpty(htmlStr)) {
                handler.sendEmptyMessage(10);
                return;
            }
            //从一个URL加载一个Document对象。
            Document doc = Jsoup.parse(htmlStr);
            Elements elements = doc.select("ul.ui-list").select("li");
            List<Video> videoList = new ArrayList<>();
            for (Element element : elements) {
                Element titleElement = element.selectFirst("a");
                if (titleElement == null) {
                    continue;
                }
                String name = titleElement.text();
                String link = titleElement.attr("href");
                String date = titleElement.selectFirst("span").text();
                if (name.length() > date.length()) {
                    name = name.substring(date.length());
                }

                Video video = new Video(false, "");
                video.setName(name).setLink(link).setScore(date);
                videoList.add(video);
            }

            //获取最大页码
            Element pageDiv = doc.selectFirst("div.ui-pages");
            Elements pageElement = pageDiv.select("a");
            for (Element element : pageElement) {
                if (null == element) {
                    totalPage = 1;
                } else {
                    try {
                        String maxPage = element.text();
                        totalPage = Integer.parseInt(maxPage);
                    } catch (NumberFormatException e) {
                        logE("页码格式错误：" + e.getMessage());
                    }
                }
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
        List<HotList> list = HotListOption.getHotList(Constant.SOURCE_KK3);
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
        mTvNewest.setVisibility(View.VISIBLE);
        mTvNewest.setText("最近更新");
    }

    /**
     * 初始化搜索结果Rv
     */
    private void initResultRv() {
        List<Video> videoList = new ArrayList<>();
        searchResultAdapter = new Kk3VideoAdapter(R.layout.adapter_kk3_video,
                R.layout.adapter_search_result_title, videoList);
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
                TextView tv = (TextView) LayoutInflater.from(Kk3SearchActivity.this)
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
        requestKk3();
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
        startWithIntent(Kk3VideoPlayActivity.class, intent);
    }
}
