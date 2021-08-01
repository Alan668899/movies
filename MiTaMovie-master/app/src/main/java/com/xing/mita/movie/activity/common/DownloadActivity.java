package com.xing.mita.movie.activity.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.StatusUtil;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;
import com.xing.mita.movie.R;
import com.xing.mita.movie.adapter.DownloadAdapter;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.dao.option.DownloadOption;
import com.xing.mita.movie.entity.Download;
import com.xing.mita.movie.service.DownLoadVideoService;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2019/2/18
 * @Description 下载页面
 */
public class DownloadActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

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

    private DownloadAdapter adapter;

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

    private int currentDownPos = -1;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_history;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.caching);
        initRv();
        registerLocalReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        matchDown();
    }

    @Override
    protected void onStop() {
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(receiver);
        }
        super.onStop();
    }

    @OnClick({R.id.ifv_back, R.id.tv_edit, R.id.tv_all, R.id.tv_delete})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.tv_edit:
                isEdit = !isEdit;
                mTvEdit.setText(isEdit ? R.string.cancel : R.string.edit);
                mTvAll.setVisibility(isEdit ? View.VISIBLE : View.GONE);
                mTvDelete.setVisibility(isEdit ? View.VISIBLE : View.GONE);

                List<Download> list = adapter.getData();
                for (Download video : list) {
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
                for (Download video : list) {
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
                    if (pos == currentDownPos) {
                        handler.removeMessages(0);
                        //取消下载
                        DownLoadVideoService.cancelTask();
                        //删除下载记录
                        DownloadOption.delete(list.get(pos).getUrl());
                        OkDownload.with().breakpointStore().remove(SysApplication.DOWNLOAD_TASK_ID);
                        currentDownPos = -1;
                    }
                    //删除本地文件
                    String path = list.get(pos).getCacheName();
                    path = Constant.DIR_VIDEO + path;
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
                    showEmptyLayout();
                } else if (currentDownPos == -1) {
                    DownLoadVideoService.startDownload(DownloadActivity.this);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        List list = adapter.getData();
        Download video = (Download) list.get(position);
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
//            StatusUtil.Status status = StatusUtil.getStatus(video.getUrl(), Constant.DIR_VIDEO, null);
//            Log.w(TAG, "onItemClick: " + status);
//            if (position == currentDownPos) {
//                //取消下载
//                DownLoadVideoService.cancelTask();
//                //降低下载权限
//                video.setPriority(Constant.DOWNLOAD_LAST_PRIORITY);
//                DownloadOption.update(video);
//                //提高下一个下载任务的下载权限
//                int size = list.size();
//                if (size > 1) {
//                    int pos = currentDownPos++;
//                    if (pos >= size) {
//                        pos = 0;
//                    }
//                    Download down = DownloadOption.load(((Download) list.get(pos)).getUrl());
//                    if (down != null) {
//                        down.setPriority(Constant.DOWNLOAD_FIRST_PRIORITY);
//                        DownloadOption.update(down);
//                        //开启下载
//                        DownLoadVideoService.startDownload(DownloadActivity.this);
//                    }
//                }
//            } else {
//                //降低正在下载任务的下载权限
//                Download down = (Download) list.get(currentDownPos);
//                if (down != null) {
//                    down.setPriority(Constant.DOWNLOAD_LAST_PRIORITY);
//                    DownloadOption.update(down);
//                }
//                //提高选中任务的下载权限
//                video.setPriority(Constant.DOWNLOAD_FIRST_PRIORITY);
//                DownloadOption.update(video);
//                //开启下载
//                DownLoadVideoService.startDownload(DownloadActivity.this);
//            }
//            handler.removeMessages(0);
//            matchDown();

            if (!SysApplication.HAS_DOWN_TASK) {
                //判断当前网络时候可用
                boolean isConnected = NetworkUtils.isConnected(DownloadActivity.this);
                //网络类型
                int networkType = NetworkUtils.getNetWorkType(DownloadActivity.this);
                if (isConnected && networkType != NetworkUtils.NETWORK_WIFI) {
                    showFlowDialog();
                }
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        if (adapter == null || !SysApplication.HAS_DOWN_TASK) {
                            break;
                        }
                        List<Download> list = adapter.getData();
                        if (list.size() <= currentDownPos) {
                            break;
                        }
                        int progress = SysApplication.DOWNLOAD_VIDEO_CACHE_PROGRESS;
                        long videoSize = SysApplication.DOWNLOAD_VIDEO_SIZE;
                        long cacheSize = videoSize * progress / 100;
                        String cacheRatio =
                                Formatter.formatFileSize(DownloadActivity.this, cacheSize)
                                        + "/" + Formatter.formatFileSize(
                                        DownloadActivity.this, videoSize);
                        Download down = list.get(currentDownPos);
                        down.setProgress(progress)
                                .setSpeed(SysApplication.DOWNLOAD_VIDEO_CACHE_SPEED)
                                .setCacheSize(cacheRatio);
                        adapter.notifyItemChanged(currentDownPos);
                        handler.sendEmptyMessageDelayed(0, 1500);
                    } catch (Exception e) {
                        Log.e(TAG, "handleMessage: " + e.getMessage());
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 显示流量提醒弹窗
     */
    private void showFlowDialog() {
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle(R.string.flow_tip)
                .setMessage(R.string.flow_tip_message)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.confirm, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        DownLoadVideoService.startDownload(DownloadActivity.this);
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

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
        List<Download> list = DownloadOption.loadAll();
        if (list == null || list.size() == 0) {
            return;
        }
        adapter = new DownloadAdapter(R.layout.adapter_download, list);
        adapter.setOnItemClickListener(this);
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM);
        //重复执行
        adapter.isFirstOnly(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);

        //显示编辑
        mTvEdit.setVisibility(View.VISIBLE);
        historyCount = list.size();
    }

    private void matchDown() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (adapter == null) {
                    return;
                }
                List<Download> list = adapter.getData();
                int size = list.size();
                if (size == 0) {
                    showEmptyLayout();
                    return;
                }
                for (int i = 0; i < size; i++) {
                    Download down = list.get(i);
                    if (TextUtils.equals(down.getUrl(), SysApplication.DOWNLOAD_VIDEO_URL)) {
                        currentDownPos = i;
                        handler.sendEmptyMessage(0);
                        break;
                    }
                }
            }
        });
    }

    /**
     * 显示空布局
     */
    private void showEmptyLayout() {
        EmptyViewUtils.setEmptyView(DownloadActivity.this, adapter,
                R.layout.empty_content);
    }

    /**
     * 注册本地广播
     */
    private void registerLocalReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_VIDEO_DOWNLOAD_START);
        filter.addAction(Constant.BROADCAST_VIDEO_DOWNLOAD_PAUSE);
        filter.addAction(Constant.BROADCAST_VIDEO_DOWNLOAD_SUCCESS);
        filter.addAction(Constant.BROADCAST_VIDEO_DOWNLOAD_FAIL);
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
                    List<Download> list = DownloadOption.loadAll();
                    adapter.setNewData(list);
                    if (list == null || list.size() == 0) {
                        showEmptyLayout();
                        return;
                    }
                    break;

                case Constant.BROADCAST_VIDEO_DOWNLOAD_START:
                    matchDown();
                    break;

                case Constant.BROADCAST_VIDEO_DOWNLOAD_FAIL:

                    break;

                case Constant.BROADCAST_VIDEO_DOWNLOAD_PAUSE:

                    break;

                default:
                    break;
            }
        }
    };
}