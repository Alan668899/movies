package com.xing.mita.movie.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.adapter.LocalVideoAdapter;
import com.xing.mita.movie.entity.LocalVideo;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2019/2/16
 * @Description 本地视频
 */
public class LocalVideoActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

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

    private LocalVideoAdapter adapter;

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

    @Override
    public int getContentViewResId() {
        return R.layout.activity_history;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.local_video);
        initRv();
        getVideo();
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
        }else {
            String url = video.getPath();
            Intent intent = new Intent();
            intent.putExtra("url", url);
            intent.putExtra("name", video.getName());
            startWithIntent(LocalVideoPlayActivity.class, intent);
        }
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

                List<LocalVideo> list = adapter.getData();
                for (LocalVideo video : list) {
                    video.setStatus(isEdit ? 1 : 0);
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
                    //更新媒体库
                    FileUtils.updateMedia(LocalVideoActivity.this, path);
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
                }
                break;

            default:
                break;
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (adapter == null) {
                        break;
                    }
                    List<LocalVideo> list = (List<LocalVideo>) message.obj;
                    adapter.setNewData(list);
                    historyCount = list.size();
                    //显示编辑按钮
                    mTvEdit.setVisibility(View.VISIBLE);
                    break;

                case 10:
                    EmptyViewUtils.setEmptyView(LocalVideoActivity.this,
                            adapter, R.layout.empty_content);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 更新删除文字提示
     */
    private void initDeleteText() {
        mTvDelete.setText(checkedCount > 0 ? getString(R.string.history_delete, checkedCount) :
                getString(R.string.delete));
    }

    /**
     * 获取本地视频
     */
    private void getVideo() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                List<LocalVideo> list = FileUtils.getLocalVideo(LocalVideoActivity.this);
                if (list == null || list.size() == 0) {
                    handler.sendEmptyMessage(10);
                } else {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = list;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRv() {
        List<LocalVideo> list = new ArrayList<>();
        adapter = new LocalVideoAdapter(R.layout.adapter_local_video, list);
        adapter.setOnItemClickListener(this);
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM);
        //重复执行
        adapter.isFirstOnly(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);
    }
}
