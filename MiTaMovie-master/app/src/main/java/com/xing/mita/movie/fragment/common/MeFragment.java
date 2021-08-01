package com.xing.mita.movie.fragment.common;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.common.AboutActivity;
import com.xing.mita.movie.activity.common.CollectionActivity;
import com.xing.mita.movie.activity.common.OfflineCacheActivity;
import com.xing.mita.movie.activity.common.FeedbackActivity;
import com.xing.mita.movie.activity.common.HistoryActivity;
import com.xing.mita.movie.activity.common.ShareAppActivity;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.CacheDataManager;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.Utils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2018/10/12
 * @Description 我的页面
 */
public class MeFragment extends BaseFragment {

    @BindView(R.id.tv_version)
    TextView mTvVersion;
    @BindView(R.id.tv_cache_size)
    TextView mTvCacheSize;

    @Override
    protected int setContentView() {
        return R.layout.fragment_me;
    }

    @Override
    protected void init() {
        mTvVersion.setText(getString(R.string.name_version,
                Utils.getLocalVersionName(Objects.requireNonNull(getActivity()))));
        getCacheSize();
    }

    @Override
    protected void lazyLoad() {
    }

    @OnClick({R.id.tv_history, R.id.tv_download, R.id.tv_collect, R.id.tv_share,
            R.id.tv_clear_cache, R.id.tv_feedback, R.id.tv_about})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.tv_history:
                startNoIntent(HistoryActivity.class);
                break;

            case R.id.tv_download:
                startNoIntent(OfflineCacheActivity.class);
                break;

            case R.id.tv_collect:
                Activity activity = getActivity();
                if (activity == null) {
                    break;
                }
                if (ScreenUtils.checkIsInMultiWindow(activity,
                        R.string.tip_multi_window_enter_collect)) {
                    break;
                }
                startNoIntent(CollectionActivity.class);
                break;

            case R.id.tv_share:
                activity = getActivity();
                if (activity == null) {
                    break;
                }
                if (ScreenUtils.checkIsInMultiWindow(activity,
                        R.string.tip_multi_window_enter_share)) {
                    break;
                }
                startNoIntent(ShareAppActivity.class);
                break;

            case R.id.tv_clear_cache:
                showClearCacheDialog();
                break;

            case R.id.tv_feedback:
                startNoIntent(FeedbackActivity.class);
                break;

            case R.id.tv_about:
                startNoIntent(AboutActivity.class);
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
                    String cacheSize = (String) msg.obj;
                    mTvCacheSize.setText(cacheSize);
                    break;

                default:
                    break;
            }
            return false;
        }
    });


    public void getCacheSize() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                calculateCacheSize();
            }
        });
    }

    /**
     * 计算缓存大小
     */
    private void calculateCacheSize() {
        try {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            String cacheSize = CacheDataManager.getTotalCacheSize(activity);
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = cacheSize;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                CacheDataManager.clearAllCache(activity);
                calculateCacheSize();
            }
        });
    }

    /**
     * 显示清除缓存弹窗
     */
    private void showClearCacheDialog() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.all_will_clear)
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
                        clearCache();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }
}
