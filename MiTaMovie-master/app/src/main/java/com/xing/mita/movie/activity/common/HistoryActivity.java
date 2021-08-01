package com.xing.mita.movie.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqVideoPlayActivity;
import com.xing.mita.movie.activity.gqzy.GqzyVideoPlayActivity;
import com.xing.mita.movie.activity.kk3.Kk3VideoPlayActivity;
import com.xing.mita.movie.adapter.MovieHistoryAdapter;
import com.xing.mita.movie.dao.option.MovieHistoryOption;
import com.xing.mita.movie.entity.MovieHistory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.DateUtils;
import com.xing.mita.movie.utils.EmptyViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2018/10/30
 * @Description 观看记录
 */
public class HistoryActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.rv_history)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_edit)
    TextView mTvEdit;
    @BindView(R.id.tv_all)
    TextView mTvAll;
    @BindView(R.id.tv_delete)
    TextView mTvDelete;

    private MovieHistoryAdapter adapter;

    /**
     * 编辑状态
     */
    private boolean isEdit;

    /**
     * 全选
     */
    private boolean isChooseAll;

    /**
     * 选择的个数
     */
    private int checkedCount;

    /**
     * 总的个数
     */
    private int historyCount;

    /**
     * 标题个数
     */
    private int headerCount;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_history;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.historic_record);
        initRv();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }, 1000);
    }

    @OnClick({R.id.ifv_back, R.id.tv_edit, R.id.tv_all, R.id.tv_delete})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.tv_edit:
                isEdit = !isEdit;
                mTvEdit.setText(isEdit ? "取消" : "编辑");
                mTvAll.setVisibility(isEdit ? View.VISIBLE : View.GONE);
                mTvDelete.setVisibility(isEdit ? View.VISIBLE : View.GONE);

                List<MovieHistory> list = adapter.getData();
                for (MovieHistory history : list) {
                    if (history.getType() != 1) {
                        history.setType(isEdit ? 2 : 0);
                    }
                }
                adapter.notifyDataSetChanged();
                if (isEdit) {
                    checkedCount = 0;
                    initDeleteText();
                }
                break;

            case R.id.tv_all:
                isChooseAll = !isChooseAll;
                mTvAll.setText(isChooseAll ? R.string.cancel_choose_all : R.string.choose_all);
                list = adapter.getData();
                if (isChooseAll) {
                    checkedCount = 0;
                }
                for (MovieHistory history : list) {
                    if (history.getType() != 1) {
                        history.setType(isChooseAll ? 3 : 2);
                    }
                }
                adapter.notifyDataSetChanged();
                checkedCount = isChooseAll ? historyCount : 0;
                initDeleteText();
                break;

            case R.id.tv_delete:
                if (checkedCount == 0) {
                    break;
                }
                List<Integer> posList = new ArrayList<>();
                list = adapter.getData();
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    if (list.get(i).getType() == 3) {
                        posList.add(i);
                        if (posList.size() == checkedCount) {
                            break;
                        }
                    }
                }
                //删除本地记录
                List<Long> ids = new ArrayList<>();
                for (MovieHistory history : list) {
                    if (history.getType() == 3) {
                        ids.add(history.getId());
                    }
                }
                MovieHistoryOption.deleteOneHistory(ids);
                //移除item
                Collections.reverse(posList);
                for (int pos : posList) {
                    adapter.remove(pos);
                }
                historyCount -= checkedCount;
                checkedCount = 0;
                initDeleteText();
                //没有历史记录显示空布局
                if (historyCount == 0) {
                    for (int i = 0; i < headerCount; i++) {
                        adapter.remove(i);
                    }
                    mTvEdit.setVisibility(View.GONE);
                    mTvAll.setVisibility(View.GONE);
                    mTvDelete.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 更新删除文字提示
     */
    private void initDeleteText() {
        mTvDelete.setText(checkedCount > 0 ? getString(R.string.history_delete, checkedCount) :
                getString(R.string.delete));
    }

    /**
     * 初始化RecyclerView
     */
    private void initRv() {
        List<MovieHistory> list = new ArrayList<>();
        adapter = new MovieHistoryAdapter(list);
        adapter.setOnItemClickListener(this);
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        //重复执行
        adapter.isFirstOnly(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 显示空布局
     */
    private void showEmptyLayout() {
        EmptyViewUtils.setEmptyView(this, adapter, R.layout.empty_content);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        List<MovieHistory> list = new ArrayList<>();
        headerCount = 0;

        //今天
        long today = DateUtils.formatToDate(new Date());
        List<MovieHistory> todayList = MovieHistoryOption.getOneDayHistory(today);
        if (todayList != null && todayList.size() > 0) {
            historyCount += todayList.size();
            headerCount++;
            MovieHistory history = new MovieHistory();
            history.setType(1).setName("今天");
            todayList.add(history);
            Collections.reverse(todayList);
            list.addAll(todayList);
        }

        //昨天
        long yesterday = DateUtils.formatYesterday();
        List<MovieHistory> yesterdayList = MovieHistoryOption.getOneDayHistory(yesterday);
        if (yesterdayList != null && yesterdayList.size() > 0) {
            historyCount += yesterdayList.size();
            headerCount++;
            MovieHistory history = new MovieHistory();
            history.setType(1).setName("昨天");
            yesterdayList.add(history);
            Collections.reverse(yesterdayList);
            list.addAll(yesterdayList);
        }

        //更早
        List<MovieHistory> earlierList = MovieHistoryOption.getDaysAgoHistory(yesterday);
        if (earlierList != null && earlierList.size() > 0) {
            historyCount += earlierList.size();
            headerCount++;
            MovieHistory history = new MovieHistory();
            history.setType(1).setName("更早");
            earlierList.add(history);
            Collections.reverse(earlierList);
            list.addAll(earlierList);
        }

        if (list.size() > 0) {
            mTvEdit.setVisibility(View.VISIBLE);
            adapter.setNewData(list);
        } else {
            showEmptyLayout();
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        List list = adapter.getData();
        MovieHistory history = (MovieHistory) list.get(position);
        if (history.getType() == 1) {
            return;
        }
        if (isEdit) {
            int type = history.getType();
            type = type == 2 ? 3 : 2;
            history.setType(type);
            adapter.notifyItemChanged(position);
            if (type == 2) {
                checkedCount--;
            } else {
                checkedCount++;
            }
            initDeleteText();
            isChooseAll = checkedCount == historyCount;
            mTvAll.setText(isChooseAll ? R.string.cancel_choose_all : R.string.choose_all);
        } else {
            Intent intent = new Intent();
            intent.putExtra("url", history.getLink());
            intent.putExtra("imgUrl", history.getImage());
            String webSite = history.getWebSite();
            Class clazz = AaqqVideoPlayActivity.class;
            if (TextUtils.equals(webSite, Constant.SOURCE_GQZY)) {
                clazz = GqzyVideoPlayActivity.class;
            } else if (TextUtils.equals(webSite, Constant.SOURCE_KK3)) {
                clazz = Kk3VideoPlayActivity.class;
            }
            startWithIntent(clazz, intent);
        }
    }
}
