package com.xing.mita.movie.activity.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.adapter.CacheVideoAdapter;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.dao.option.DownloadOption;
import com.xing.mita.movie.entity.Download;
import com.xing.mita.movie.entity.LocalVideo;
import com.xing.mita.movie.player.LocalVideoPlayer;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.CacheDataManager;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2018/10/30
 * @Description 离线缓存页面
 */
public class OfflineCacheActivity extends BaseActivity implements
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_local_nun)
    TextView mTvLocalVideoNum;
    @BindView(R.id.pb_memsize)
    ProgressBar mPbMemSize;
    @BindView(R.id.tv_cache_size_tip)
    TextView mTvCacheSizeTip;
    @BindView(R.id.tv_cache_num_show)
    TextView mTvCacheNum;
    @BindView(R.id.rv_cache_video)
    RecyclerView mRecyclerView;
    @BindView(R.id.cl_download)
    ConstraintLayout mClDownload;
    @BindView(R.id.tv_cache_num)
    TextView mTvCachingNum;
    @BindView(R.id.tv_edit)
    TextView mTvEdit;
    @BindView(R.id.tv_all)
    TextView mTvAll;
    @BindView(R.id.tv_delete)
    TextView mTvDelete;
    @BindView(R.id.tv_cache_status)
    TextView mTvCacheStatus;
    @BindView(R.id.tv_cache_speed)
    TextView mTvCacheSpeed;
    @BindView(R.id.tv_cache_name)
    TextView mTvCacheName;
    @BindView(R.id.pb_cache)
    ProgressBar mPbCache;

    private CacheVideoAdapter adapter;

    private LocalBroadcastManager localBroadcastManager;

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
     * 是否有下载任务
     */
    private boolean hasDownTask;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_download;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.my_download);
        initRv();
        registerLocalReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        initEditStatus();
        handler.removeMessages(2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocalVideoNum();
        initSpace();
        getCacheVideo();
        initDownloadStatus();
        if (hasDownTask) {
            handler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onStop() {
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(receiver);
        }
        super.onStop();
    }

    @OnClick({R.id.ifv_back, R.id.cl_local, R.id.cl_download, R.id.tv_edit, R.id.tv_all,
            R.id.tv_delete})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.cl_local:
                startNoIntent(LocalVideoActivity.class);
                break;

            case R.id.cl_download:
                startNoIntent(DownloadActivity.class);
                break;

            case R.id.tv_edit:
                isEdit = !isEdit;
                mTvEdit.setText(isEdit ? R.string.cancel : R.string.edit);
                mTvAll.setVisibility(isEdit ? View.VISIBLE : View.GONE);
                mTvDelete.setVisibility(isEdit ? View.VISIBLE : View.GONE);

                List<LocalVideo> list = adapter.getData();
                for (LocalVideo video : list) {
                    video.setStatus(isEdit ? 1 : 0);
                }
                adapter.notifyDataSetChanged();
                if (isEdit) {
                    initEditStatus();
                }
                break;

            case R.id.tv_all:
                isChooseAll = !isChooseAll;
                mTvAll.setText(isChooseAll ? R.string.cancel_choose_all : R.string.choose_all);
                list = adapter.getData();
                if (isChooseAll) {
                    checkedCount = 0;
                }
                for (LocalVideo video : list) {
                    video.setStatus(isChooseAll ? 2 : 1);
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
                    if (list.get(i).getStatus() == 2) {
                        posList.add(i);
                        if (posList.size() == checkedCount) {
                            break;
                        }
                    }
                }

                //移除item
                Collections.reverse(posList);
                for (int pos : posList) {
                    //删除本地文件
                    String path = list.get(pos).getPath();
                    FileUtils.deleteFile(path);
                    //移除item
                    adapter.remove(pos);
                }
                historyCount -= checkedCount;
                checkedCount = 0;
                initDeleteText();
                //没有历史记录显示空布局
                if (historyCount == 0) {
                    mTvEdit.setVisibility(View.GONE);
                    mTvAll.setVisibility(View.GONE);
                    mTvDelete.setVisibility(View.GONE);
                    mTvCacheNum.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        List list = adapter.getData();
        LocalVideo video = (LocalVideo) list.get(position);
        if (isEdit) {
            int type = video.getStatus();
            type = type == 2 ? 1 : 2;
            video.setStatus(type);
            adapter.notifyItemChanged(position);
            if (type == 1) {
                checkedCount--;
            } else {
                checkedCount++;
            }
            initDeleteText();
            isChooseAll = checkedCount == historyCount;
            mTvAll.setText(isChooseAll ? R.string.cancel_choose_all : R.string.choose_all);
        } else {
            String url = video.getPath();
            Intent intent = new Intent();
            intent.putExtra("url", url);
            intent.putExtra("name", video.getName());
            startWithIntent(LocalVideoPlayActivity.class, intent);
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    int num = message.arg1;
                    mTvLocalVideoNum.setText(getString(R.string.local_video_num, num));
                    break;

                case 1:
                    List<LocalVideo> list = (List<LocalVideo>) message.obj;
                    adapter.setNewData(list);
                    int size = list.size();
                    mTvCacheNum.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
                    if (size > 0) {
                        mTvCacheNum.setText(getString(R.string.cache_video_num, size));
                        //显示编辑按钮
                        mTvEdit.setVisibility(View.VISIBLE);
                        historyCount = size;
                    }
                    break;

                case 2:
                    boolean isCaching = !TextUtils.isEmpty(SysApplication.DOWNLOAD_VIDEO_NAME);
                    if (isCaching) {
                        mTvCacheStatus.setText("正在缓存");
                        mTvCacheName.setText(SysApplication.DOWNLOAD_VIDEO_NAME);
                        mPbCache.setProgress(SysApplication.DOWNLOAD_VIDEO_CACHE_PROGRESS);
                        mTvCacheSpeed.setText(SysApplication.DOWNLOAD_VIDEO_CACHE_SPEED);
                        initSpace();
                    } else {
                        mTvCacheStatus.setText("等待缓存");
                        mTvCacheName.setText("XingMi");
                    }
                    handler.sendEmptyMessageDelayed(2, 1500);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化编辑状态
     */
    private void initEditStatus() {
        isChooseAll = false;
        mTvAll.setText(R.string.choose_all);
        checkedCount = 0;
        initDeleteText();
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
        List<LocalVideo> list = new ArrayList<>();
        adapter = new CacheVideoAdapter(R.layout.adapter_cache_video, list);
        adapter.setOnItemClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 获取本地视频个数
     */
    private void getLocalVideoNum() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                List<LocalVideo> list = FileUtils.getLocalVideo(OfflineCacheActivity.this);
                int size = 0;
                if (list != null) {
                    size = list.size();
                }
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = size;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 初始化缓存空间大小
     */
    private void initSpace() {
        //获取内存可用剩余空间
        long romFreeSpace = Environment.getDataDirectory().getFreeSpace();
        String freeSize = Formatter.formatFileSize(this, romFreeSpace);
        //获取内存总空间大小
        long romTotalSpace = Environment.getDataDirectory().getTotalSpace();
        //获取已缓存大小
        File file = new File(Constant.DIR_VIDEO);
        long cacheSpace = CacheDataManager.getFolderSize(file);
        String cacheSize = Formatter.formatFileSize(this, cacheSpace);
        int progress = (int) (cacheSpace * 100 / romTotalSpace);

        mPbMemSize.setProgress(progress);
        mTvCacheSizeTip.setText(getString(R.string.cache_size_tip, cacheSize, freeSize));
    }

    /**
     * 获取所有缓存的视频
     */
    private void getCacheVideo() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(Constant.DIR_VIDEO);
                if (!file.exists()) {
                    return;
                }
                File[] files = file.listFiles();
                if (files == null) {
                    return;
                }
                List<LocalVideo> list = new ArrayList<>();
                for (File f : files) {
                    String name = f.getName();
                    if (TextUtils.isEmpty(name) || name.contains(Constant.FILE_VIDEO_PREFIX)) {
                        continue;
                    }
                    LocalVideo video = new LocalVideo();
                    video.setName(name)
                            .setPath(f.getPath())
                            .setSize(Formatter.formatFileSize(
                                    OfflineCacheActivity.this, f.length()));
                    if (isEdit) {
                        video.setStatus(1);
                    }
                    list.add(video);
                }
                if (list.size() > 0) {
                    Message msg = Message.obtain();
                    msg.obj = list;
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 初始化当前下载状态
     */
    private void initDownloadStatus() {
        List<Download> list = DownloadOption.loadAll();
        if (list == null || list.size() == 0) {
            mClDownload.setVisibility(View.GONE);
            hasDownTask = false;
            return;
        }
        mClDownload.setVisibility(View.VISIBLE);
        mTvCachingNum.setText(String.valueOf(list.size()));
        handler.sendEmptyMessage(2);
        hasDownTask = true;
    }

    /**
     * 注册本地广播
     */
    private void registerLocalReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_VIDEO_DOWNLOAD_SUCCESS);
        localBroadcastManager.registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Constant.BROADCAST_VIDEO_DOWNLOAD_SUCCESS:
                    handler.removeMessages(2);
                    initDownloadStatus();
                    getCacheVideo();
                    break;

                default:
                    break;
            }
        }
    };
}
