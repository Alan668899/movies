package com.xing.mita.movie.fragment.aaqq;

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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqSearchActivity;
import com.xing.mita.movie.activity.common.CollectionActivity;
import com.xing.mita.movie.activity.common.HistoryActivity;
import com.xing.mita.movie.activity.common.OfflineCacheActivity;
import com.xing.mita.movie.adapter.PagerFragmentAdapter;
import com.xing.mita.movie.dao.option.HotListOption;
import com.xing.mita.movie.entity.HotList;
import com.xing.mita.movie.fragment.common.BaseFragment;
import com.xing.mita.movie.utils.AnimUtils;
import com.xing.mita.movie.utils.ColorUtils;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.SpUtils;
import com.xing.mita.movie.utils.TextSwitcherAnimation;
import com.xing.mita.movie.utils.Utils;
import com.xing.mita.movie.view.IconFontView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnPageChange;

/**
 * @author Mita
 * @date 2019/1/21
 * @Description
 */
public class AaqqHomeFragment extends BaseFragment implements TabLayout.OnTabSelectedListener,
        ViewSwitcher.ViewFactory {

    @BindView(R.id.tl_home)
    TabLayout mTabLayout;
    @BindView(R.id.vp_home)
    ViewPager mViewPager;
    @BindView(R.id.tv_order_time)
    TextView mTvOrderTime;
    @BindView(R.id.tv_order_hits)
    TextView mTvOrderHits;
    @BindView(R.id.tv_order_score)
    TextView mTvOrderScore;
    @BindView(R.id.v_cover)
    View mCover;
    @BindView(R.id.ifv_collect)
    IconFontView mIfvCollect;
    @BindView(R.id.ifv_download)
    IconFontView mIfvDownload;
    @BindView(R.id.ifv_history)
    IconFontView mIfvHistory;
    @BindView(R.id.ts_search)
    TextSwitcher mTsSearch;
    @BindView(R.id.ifv_search)
    TextView mIfvSearch;

    private TextSwitcherAnimation textSwitcherAnimation;

    private int[] categoryList = {R.string.tab_recommend, R.string.tab_movie, R.string.tab_teleplay,
            R.string.tab_variety, R.string.tab_cartoon};
    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> searchList = new ArrayList<>();

    @Override
    protected int setContentView() {
        return R.layout.activity_aaqq_home;
    }

    @Override
    protected void init() {
        initFragments();
        initTabLayout();
        initOrder();
        registerBroadcast();
        mTsSearch.setFactory(this);
    }

    @Override
    protected void lazyLoad() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (textSwitcherAnimation != null) {
            textSwitcherAnimation.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startSearchRecommend();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播接收器
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
    }

    @OnClick({R.id.tv_order_time, R.id.tv_order_hits, R.id.tv_order_score, R.id.ts_search,
            R.id.ifv_history, R.id.ifv_collect, R.id.ifv_download})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.tv_order_time:
                updateOrder(Constant.ORDER_BY_TIME);
                break;

            case R.id.tv_order_hits:
                updateOrder(Constant.ORDER_BY_HITS);
                break;

            case R.id.tv_order_score:
                updateOrder(Constant.ORDER_BY_SCORE);
                break;

            case R.id.ts_search:
                Intent intent = new Intent();
                if (textSwitcherAnimation != null) {
                    int marker = textSwitcherAnimation.getMarker();
                    intent.putExtra("searchName", searchList.get(marker));
                }
                startWithIntent(AaqqSearchActivity.class, intent);
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

    @OnPageChange(value = R.id.vp_home)
    public void onPageSelected(int position) {
        boolean showHomeOption = position == 0;
        setViewVisibility(mIfvCollect, showHomeOption);
        setViewVisibility(mIfvHistory, showHomeOption);
        setViewVisibility(mIfvDownload, showHomeOption);
        setViewVisibility(mTvOrderHits, !showHomeOption);
        setViewVisibility(mTvOrderScore, !showHomeOption);
        setViewVisibility(mTvOrderTime, !showHomeOption);
        setViewVisibility(mCover, !showHomeOption);
    }

    private void setViewVisibility(View v, boolean show) {
        v.setVisibility(show ? View.VISIBLE : View.GONE);
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

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    textSwitcherAnimation = new TextSwitcherAnimation(mTsSearch, searchList);
                    textSwitcherAnimation.create();
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


    /**
     * 添加Fragments
     */
    private void initFragments() {
        fragmentList.clear();
        fragmentList.add(new AaqqRecommendFragment());
        for (int i = 1; i < 5; i++) {
            AaqqMovieFragment movieFragment = new AaqqMovieFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("id", i);
            movieFragment.setArguments(bundle);
            fragmentList.add(movieFragment);
        }
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
        int size = categoryList.length;
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
        textView.setText(getString(categoryList[currentPosition]));
        return view;
    }

    /**
     * 跳转显示fragment
     *
     * @param position int
     */
    public void showFragment(int position) {
        mViewPager.setCurrentItem(position);
    }

    /**
     * 更新视频排序
     *
     * @param order String
     */
    private void updateOrder(String order) {
        Activity activity = getActivity();
        if (activity != null) {
            SpUtils.saveVideoOrder(activity, order);
        }
        initOrder();
        AaqqMovieFragment fragment = (AaqqMovieFragment) fragmentList.get(mViewPager.getCurrentItem());
        fragment.onRefresh();
    }

    /**
     * 初始化视频排序
     */
    private void initOrder() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        String videoOrder = SpUtils.getVideoOrder(activity);
        if (TextUtils.equals(videoOrder, Constant.ORDER_BY_TIME)) {
            ColorUtils.setColor(activity, mTvOrderTime, R.color.white);
            ColorUtils.setColor(activity, mTvOrderHits, R.color.color_66);
            ColorUtils.setColor(activity, mTvOrderScore, R.color.color_66);

            mTvOrderTime.setBackgroundResource(R.drawable.shape_category_select);
            mTvOrderHits.setBackground(null);
            mTvOrderScore.setBackground(null);
        } else if (TextUtils.equals(videoOrder, Constant.ORDER_BY_HITS)) {
            ColorUtils.setColor(activity, mTvOrderHits, R.color.white);
            ColorUtils.setColor(activity, mTvOrderTime, R.color.color_66);
            ColorUtils.setColor(activity, mTvOrderScore, R.color.color_66);

            mTvOrderTime.setBackground(null);
            mTvOrderHits.setBackgroundResource(R.drawable.shape_category_select);
            mTvOrderScore.setBackground(null);
        } else {
            ColorUtils.setColor(activity, mTvOrderScore, R.color.white);
            ColorUtils.setColor(activity, mTvOrderHits, R.color.color_66);
            ColorUtils.setColor(activity, mTvOrderTime, R.color.color_66);

            mTvOrderTime.setBackground(null);
            mTvOrderHits.setBackground(null);
            mTvOrderScore.setBackgroundResource(R.drawable.shape_category_select);
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
        List<HotList> hotLists = HotListOption.getHotList(Constant.SOURCE_AAQQY);
        if (hotLists == null || hotLists.size() == 0) {
            return;
        }
        searchList.clear();
        for (HotList hot : hotLists) {
            searchList.add(hot.getName());
        }
        handler.sendEmptyMessage(0);
    }

    private LocalBroadcastManager localBroadcastManager;

    private void registerBroadcast() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BROADCAST_AAQQ_LOAD_SEARCH_RECOMMEND);
        intentFilter.addAction(Constant.BROADCAST_SHOW_FRAGMENT_INDEX);
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
                case Constant.BROADCAST_AAQQ_LOAD_SEARCH_RECOMMEND:
                    loadSearchRecommend();
                    break;

                case Constant.BROADCAST_SHOW_FRAGMENT_INDEX:
                    int index = intent.getIntExtra("index", 0);
                    showFragment(index);
                    break;

                default:
                    break;
            }
        }
    };
}
