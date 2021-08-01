package com.xing.mita.movie.fragment.common;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.common.LiveActivity;
import com.xing.mita.movie.adapter.HotChannelAdapter;
import com.xing.mita.movie.adapter.IndexAdapter;
import com.xing.mita.movie.dao.option.ChanHisOption;
import com.xing.mita.movie.dao.option.ProgramOption;
import com.xing.mita.movie.entity.Channel;
import com.xing.mita.movie.entity.ChannelHistory;
import com.xing.mita.movie.entity.Program;
import com.xing.mita.movie.service.DownLoadLiveService;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.PinYinUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

/**
 * @author Mita
 * @date 2019/1/17
 * @Description 热门频道推荐
 */
public class HotChannelFragment extends BaseFragment implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.rv_hot_channel)
    RecyclerView mRvChannel;
    @BindView(R.id.rv_index)
    RecyclerView mRvIndex;

    private HotChannelAdapter adapter;

    private List<String> words = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    private int[] wordPos = new int[26];

    private int historyNum;

    @Override
    protected int setContentView() {
        return R.layout.fragment_hot_channel;
    }

    @Override
    protected void init() {
        initRv();
        initData();
        registerReceiver();
    }

    @Override
    public void lazyLoad() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && adapter.getData().size() > 0) {
            initData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销本地广播
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (adapter instanceof HotChannelAdapter) {
            List list = adapter.getData();
            if (list.size() == 0 || list.size() <= position) {
                return;
            }
            Channel channel = (Channel) list.get(position);
            if (channel == null || channel.getType() == 0) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("programId", channel.getId());
            startWithIntent(LiveActivity.class, intent);

            //保存点击记录
            ChannelHistory history = new ChannelHistory();
            history.setName(channel.getName())
                    .setChannelId(channel.getId())
                    .setUpdateTime(System.currentTimeMillis());
            ChanHisOption.save(history);
        } else {
            index = wordPos[position];
            index += historyNum;
            int first = glm.findFirstVisibleItemPosition();
            int end = glm.findLastVisibleItemPosition();
            if (index <= first) {
                mRvChannel.scrollToPosition(index);
            } else if (index <= end) {
                int top = mRvChannel.getChildAt(index - first).getTop();
                mRvChannel.scrollBy(0, top);
            } else {
                mRvChannel.scrollToPosition(index);
                move = true;
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    EmptyViewUtils.setEmptyView(getActivity(), adapter,
                            R.layout.empty_request_loading);
                    break;

                case 2:
                    List<Channel> newData = (List<Channel>) msg.obj;
                    adapter.setNewData(newData);
                    break;

                case 10:
                    View convertView = EmptyViewUtils.setEmptyView(getActivity(), adapter,
                            R.layout.empty_request_fail);
                    if (convertView == null) {
                        break;
                    }
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initData();
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
     * 初始化数据
     */
    private void initData() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                //获取本地直播源数据
                List<Program> list = ProgramOption.getAll();
                if (list == null || list.size() == 0) {
                    handler.sendEmptyMessage(0);
                    Activity activity = getActivity();
                    if (activity != null) {
                        //启动直播源下载服务
                        DownLoadLiveService.startDownload(activity);
                    }
                    return;
                }
                List<Channel> newData = new ArrayList<>();
                for (Program program : list) {
                    Channel channel = new Channel();
                    String name = program.getName();
                    channel.setId(program.getId())
                            .setName(name)
                            .setType(2)
                            .setPinYin(PinYinUtils.getPinyin(name));
                    newData.add(channel);
                }
                if (newData.size() == 0) {
                    handler.sendEmptyMessage(10);
                }
                for (String word : words) {
                    Channel channel = new Channel();
                    channel.setType(0)
                            .setTitle(word)
                            .setPinYin(PinYinUtils.getPinyin(word));
                    newData.add(channel);
                }
                //对集合排序
                Collections.sort(newData, new Comparator<Channel>() {
                    @Override
                    public int compare(Channel lhs, Channel rhs) {
                        //根据拼音进行排序
                        return lhs.getPinYin().compareTo(rhs.getPinYin());
                    }
                });

                int size = newData.size();
                int len = words.size();
                int pos = 0;
                //遍历首字母index
                for (int i = 0; i < size; i++) {
                    String name = newData.get(i).getTitle();
                    if (TextUtils.isEmpty(name)) {
                        continue;
                    }
                    if (TextUtils.equals(name, words.get(pos))) {
                        wordPos[pos] = i;
                        pos++;
                        if (pos == len) {
                            break;
                        }
                    }
                }

                //加载历史记录
                loadHistory(newData);

                Message msg = Message.obtain();
                msg.what = 2;
                msg.obj = newData;
                handler.sendMessage(msg);
            }
        });
    }


    /**
     * 加载历史记录
     *
     * @param list List<Channel>
     */
    private void loadHistory(List<Channel> list) {
        List<ChannelHistory> historyList = ChanHisOption.loadAll();
        if (historyList != null) {
            List<Channel> channelList = new ArrayList<>();
            Channel channel = new Channel();
            channel.setTitle("观看记录");
            channelList.add(channel);

            for (ChannelHistory his : historyList) {
                Channel chan = new Channel();
                chan.setName(his.getName())
                        .setType(1)
                        .setId(his.getChannelId());
                channelList.add(chan);
            }
            historyNum = channelList.size();
            list.addAll(0, channelList);
        } else {
            historyNum = 0;
        }
    }

    private GridLayoutManager glm;

    private void initRv() {
        List<Channel> list = new ArrayList<>();
        adapter = new HotChannelAdapter(list);
        adapter.setOnItemClickListener(this);
        glm = new GridLayoutManager(getActivity(), 3);
        mRvChannel.setLayoutManager(glm);
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        //重复执行
        adapter.isFirstOnly(false);
        mRvChannel.setAdapter(adapter);
        mRvChannel.addOnScrollListener(onScrollListener);
        adapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                List<Channel> list = adapter.getData();
                if (list.size() == 0) {
                    return 3;
                }
                int spanSize = 3;
                int type = list.get(position).getType();
                if (type == 1) {
                    spanSize = 1;
                }
                return spanSize;
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRvIndex.setLayoutManager(llm);
        IndexAdapter indexAdapter = new IndexAdapter(R.layout.adapter_channel_index, words);
        indexAdapter.setOnItemClickListener(this);
        mRvIndex.setAdapter(indexAdapter);
    }

    private boolean move;
    private int index;

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (move) {
                int n = index - glm.findFirstVisibleItemPosition();
                if (n >= 0 && n < mRvChannel.getChildCount()) {
                    mRvChannel.scrollBy(0, mRvChannel.getChildAt(n).getTop());
                }
                move = false;
            }
        }
    };

    private LocalBroadcastManager localBroadcastManager;

    /**
     * 注册本地广播
     */
    private void registerReceiver() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_LIVE_SOURCE_UPDATE);
        filter.addAction(Constant.BROADCAST_LIVE_SOURCE_DOWNLOAD_FAIL);
        localBroadcastManager.registerReceiver(localReceiver, filter);
    }

    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Constant.BROADCAST_LIVE_SOURCE_UPDATE:
                    initData();
                    break;

                case Constant.BROADCAST_LIVE_SOURCE_DOWNLOAD_FAIL:
                    handler.sendEmptyMessage(10);
                    break;

                default:
                    break;
            }
        }
    };
}
