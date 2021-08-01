package com.xing.mita.movie.fragment.kk3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.common.CollectionActivity;
import com.xing.mita.movie.activity.common.HistoryActivity;
import com.xing.mita.movie.activity.common.OfflineCacheActivity;
import com.xing.mita.movie.activity.kk3.Kk3SearchActivity;
import com.xing.mita.movie.adapter.PagerFragmentAdapter;
import com.xing.mita.movie.dao.option.HotListOption;
import com.xing.mita.movie.entity.Category;
import com.xing.mita.movie.entity.HotList;
import com.xing.mita.movie.fragment.common.BaseFragment;
import com.xing.mita.movie.http.HttpUtils;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.AnimUtils;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.LogUtils;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.TextSwitcherAnimation;
import com.xing.mita.movie.utils.Utils;
import com.xing.mita.movie.view.IconFontView;

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
 * @date 2019/1/21
 * @Description Kk3电影首页
 */
public class Kk3HomeFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,
        ViewSwitcher.ViewFactory {

    @BindView(R.id.tl_home)
    TabLayout mTabLayout;
    @BindView(R.id.vp_home)
    ViewPager mViewPager;
    @BindView(R.id.ifv_collect)
    IconFontView mIfvCollect;
    @BindView(R.id.ifv_history)
    IconFontView mIfvHistory;
    @BindView(R.id.ifv_download)
    IconFontView mIfvDownload;
    @BindView(R.id.ts_search)
    TextSwitcher mTsSearch;
    @BindView(R.id.ifv_search)
    TextView mIfvSearch;

    private List<Category> categoryList = new ArrayList<>();
    private List<String> searchList = new ArrayList<>();

    private List<Fragment> fragmentList = new ArrayList<>();

    private TextSwitcherAnimation textSwitcherAnimation;

    @Override
    protected int setContentView() {
        return R.layout.activity_kk3_home;
    }

    @Override
    protected void init() {
        requestHtml();
        registerBroadcast();
        mTsSearch.setFactory(this);
    }

    @Override
    protected void lazyLoad() {
    }

    @Override
    public void onResume() {
        super.onResume();
        startSearchRecommend();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (textSwitcherAnimation != null) {
            textSwitcherAnimation.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播接收器
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
    }

    @OnClick({R.id.ts_search, R.id.ifv_history, R.id.ifv_collect, R.id.ifv_download})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ts_search:
                Intent intent = new Intent();
                if (textSwitcherAnimation != null) {
                    int marker = textSwitcherAnimation.getMarker();
                    intent.putExtra("searchName", searchList.get(marker));
                }
                startWithIntent(Kk3SearchActivity.class, intent);
                break;

            case R.id.ifv_collect:
                Activity activity = getActivity();
                if (activity == null) {
                    break;
                }
                if (ScreenUtils.checkIsInMultiWindow(activity,
                        R.string.tip_multi_window_enter_collect)) {
                    break;
                }
                AnimUtils.loadClickAnim(mIfvCollect);
                startNoIntent(CollectionActivity.class);
                break;

            case R.id.ifv_history:
                AnimUtils.loadClickAnim(mIfvHistory);
                startNoIntent(HistoryActivity.class);
                break;

            case R.id.ifv_download:
                AnimUtils.loadClickAnim(mIfvDownload);
                startNoIntent(OfflineCacheActivity.class);
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
                    initFragments();
                    initTabLayout();
                    break;

                case 1:
                    textSwitcherAnimation = new TextSwitcherAnimation(mTsSearch, searchList);
                    textSwitcherAnimation.create();
                    break;

                case 10:
                    requestHtml();
                    break;

                case 11:
                    initTabLayout();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    private void requestHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                String htmlStr = HttpUtils.getKk3HTML(Constant.SOURCE_KK3);
                if (TextUtils.isEmpty(htmlStr)) {
                    handler.sendEmptyMessageDelayed(10, 1000);
                    return;
                }
                try {
                    // 从一个URL加载一个Document对象。
                    Document doc = Jsoup.parse(htmlStr);
                    Elements navElements = doc.select("div#nav").select("a");
                    for (Element element : navElements) {
                        String link = element.attr("href");
                        String title = element.text();
                        if (title.contains("排行") || title.contains("更新")) {
                            continue;
                        }
                        Category category = new Category();
                        category.setName(title).setId(link);
                        categoryList.add(category);
                    }
                    Category category = new Category();
                    category.setName("推荐");
                    categoryList.add(0, category);
                    handler.sendEmptyMessage(0);
                } catch (Exception e) {
                    Log.e(TAG, "run: " + e.toString());
                    handler.sendEmptyMessageDelayed(10, 1000);
                }
            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        //设置选中状态下tab字体显示样式
        View view = tab.getCustomView();
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(22);
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(15);
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            ((TextView) view).setTextColor(ContextCompat.getColor(activity, R.color.color_66));
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * 初始化TabLayout
     */
    private void initTabLayout() {
        if (!isAdded()) {
            handler.sendEmptyMessageDelayed(11, 1000);
            return;
        }
        mTabLayout.addOnTabSelectedListener(this);
        PagerFragmentAdapter pagerAdapter = new PagerFragmentAdapter(getChildFragmentManager(), fragmentList);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(fragmentList.size());
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        int size = categoryList.size();
        for (int i = 0; i < size; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(getTabView(i)), i, i == 0);
        }
    }

    /**
     * 自定义Tab的View
     *
     * @param currentPosition int
     * @return View
     */
    private View getTabView(int currentPosition) {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_tab, null);
        TextView textView = view.findViewById(R.id.tab_item_textview);
        textView.setText(categoryList.get(currentPosition).getName());
        return view;
    }

    /**
     * 添加Fragments
     */
    private void initFragments() {
        fragmentList.clear();
        fragmentList.add(new Kk3RecommendFragment());
        int size = categoryList.size();
        for (int i = 1; i < size; i++) {
            Kk3MovieFragment movieFragment = new Kk3MovieFragment();
            Bundle bundle = new Bundle();
            bundle.putString("link", categoryList.get(i).getId());
            movieFragment.setArguments(bundle);
            fragmentList.add(movieFragment);
        }
    }

    /**
     * 开启热搜推荐
     */
    private void startSearchRecommend() {
        if (textSwitcherAnimation != null) {
            textSwitcherAnimation.start();
        }
    }

    @Override
    public View makeView() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        TextView tv = new TextView(activity);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        tv.setTextColor(ContextCompat.getColor(activity, R.color.color_99));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL
        );
        params.leftMargin = Utils.dp2px(activity, 16);
        tv.setLayoutParams(params);
        return tv;
    }

    /**
     * Fragment通知加载搜索推荐
     */
    public void loadSearchRecommend() {
        List<HotList> hotLists = HotListOption.getHotList(Constant.SOURCE_KK3);
        if (hotLists == null || hotLists.size() == 0) {
            return;
        }
        searchList.clear();
        for (HotList hot : hotLists) {
            searchList.add(hot.getName());
        }
        handler.sendEmptyMessage(1);
    }

    private LocalBroadcastManager localBroadcastManager;

    private void registerBroadcast() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BROADCAST_KK3_LOAD_SEARCH_RECOMMEND);
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Constant.BROADCAST_KK3_LOAD_SEARCH_RECOMMEND:
                    loadSearchRecommend();
                    break;

                default:
                    break;
            }
        }
    };
}
