package com.xing.mita.movie.activity.common;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mob.moblink.Scene;
import com.mob.moblink.SceneRestorable;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqVideoPlayActivity;
import com.xing.mita.movie.activity.gqzy.GqzyVideoPlayActivity;
import com.xing.mita.movie.activity.kk3.Kk3VideoPlayActivity;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.dao.option.UpdateOption;
import com.xing.mita.movie.entity.Update;
import com.xing.mita.movie.fragment.aaqq.AaqqHomeFragment;
import com.xing.mita.movie.fragment.common.HotChannelFragment;
import com.xing.mita.movie.fragment.common.MeFragment;
import com.xing.mita.movie.fragment.kk3.Kk3HomeFragment;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.SpUtils;
import com.xing.mita.movie.utils.Utils;
import com.xing.mita.movie.view.IconFontView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * @author Mita
 * @date 2019/1/21
 * @Description 首页
 */
public class HomeActivity extends BaseActivity implements PopupWindow.OnDismissListener,
        SceneRestorable {

    @BindView(R.id.fl_content)
    FrameLayout mFlContent;
    @BindView(R.id.tv_tab_movie)
    TextView mTvMovie;
    @BindView(R.id.ifv_movie)
    IconFontView mIfvMovie;
    @BindView(R.id.tv_tab_live)
    TextView mTvLive;
    @BindView(R.id.ifv_live)
    TextView mIfvLive;
    @BindView(R.id.tv_tab_me)
    TextView mTvMe;
    @BindView(R.id.ifv_me)
    IconFontView mIfvMe;

    private AaqqHomeFragment aaqqHomeFragment;
    private Kk3HomeFragment kk3HomeFragment;
    private HotChannelFragment channelFragment;
    private MeFragment meFragment;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private View convertView;
    private PopupWindow popupWindow;

    private long firstTime;
    private boolean nextUpdate;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_home;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        getSaveFragment(savedInstanceState);
        showFirstFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUpdate();
    }

    @Override
    public void onBackPressed() {
        //双击退出
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > SysApplication.BACK_AGAIN_TIME) {
            showToast(R.string.back_again_exit);
            firstTime = secondTime;
        } else {
            super.onBackPressed();
        }
    }

    @OnClick({R.id.tv_tab_movie, R.id.tv_tab_live, R.id.tv_tab_me})
    public void click(View v) {
        initStatus();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.tv_tab_movie:
                int index = SpUtils.getMovieHome(HomeActivity.this);
                if (index == 1) {
                    if (aaqqHomeFragment == null) {
                        aaqqHomeFragment = new AaqqHomeFragment();
                        fragmentTransaction.add(R.id.fl_content, aaqqHomeFragment,
                                "aaqqHomeFragment");
                    } else {
                        fragmentTransaction.show(aaqqHomeFragment);
                    }
                    hideFragment(fragmentTransaction, kk3HomeFragment);
                } else {
                    if (kk3HomeFragment == null) {
                        kk3HomeFragment = new Kk3HomeFragment();
                        fragmentTransaction.add(R.id.fl_content, kk3HomeFragment,
                                "kk3HomeFragment");
                    } else {
                        fragmentTransaction.show(kk3HomeFragment);
                    }
                    hideFragment(fragmentTransaction, aaqqHomeFragment);
                }
                hideFragment(fragmentTransaction, channelFragment);
                hideFragment(fragmentTransaction, meFragment);
                setColor(mTvMovie, true);
                setColor(mIfvMovie, true);
                break;

            case R.id.tv_tab_live:
                if (channelFragment == null) {
                    channelFragment = new HotChannelFragment();
                    fragmentTransaction.add(R.id.fl_content, channelFragment, "channelFragment");
                } else {
                    fragmentTransaction.show(channelFragment);
                }
                hideFragment(fragmentTransaction, aaqqHomeFragment);
                hideFragment(fragmentTransaction, kk3HomeFragment);
                hideFragment(fragmentTransaction, meFragment);
                setColor(mTvLive, true);
                setColor(mIfvLive, true);
                break;

            case R.id.tv_tab_me:
                if (meFragment == null) {
                    meFragment = new MeFragment();
                    fragmentTransaction.add(R.id.fl_content, meFragment, "meFragment");
                } else {
                    fragmentTransaction.show(meFragment);
                }
                hideFragment(fragmentTransaction, aaqqHomeFragment);
                hideFragment(fragmentTransaction, kk3HomeFragment);
                hideFragment(fragmentTransaction, channelFragment);
                setColor(mTvMe, true);
                setColor(mIfvMe, true);

                //显示缓存大小
                meFragment.getCacheSize();
                break;

            default:
                break;
        }
        fragmentTransaction.commit();
    }

    @OnLongClick(R.id.tv_tab_movie)
    public boolean longClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tab_movie:
                initListPopupIfNeed();
                mListPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                mListPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
                mListPopup.show(v);
                break;

            default:
                break;
        }
        return false;
    }

    private void getSaveFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fragmentManager = getSupportFragmentManager();
            aaqqHomeFragment = (AaqqHomeFragment) fragmentManager.findFragmentByTag("aaqqHomeFragment");
            kk3HomeFragment = (Kk3HomeFragment) fragmentManager.findFragmentByTag("kk3HomeFragment");
            channelFragment = (HotChannelFragment) fragmentManager.findFragmentByTag("channelFragment");
            meFragment = (MeFragment) fragmentManager.findFragmentByTag("meFragment");
        }
    }

    /**
     * 显示初始fragment
     */
    private void showFirstFragment() {
        initStatus();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        int index = SpUtils.getMovieHome(this);
        if (index == 1) {
            if (aaqqHomeFragment == null) {
                aaqqHomeFragment = new AaqqHomeFragment();
                fragmentTransaction.add(R.id.fl_content, aaqqHomeFragment, "aaqqHomeFragment");
            } else {
                fragmentTransaction.show(aaqqHomeFragment);
            }
            hideFragment(fragmentTransaction, kk3HomeFragment);
            mTvMovie.setText(R.string.tab_aaqq);
        } else {
            if (kk3HomeFragment == null) {
                kk3HomeFragment = new Kk3HomeFragment();
                fragmentTransaction.add(R.id.fl_content, kk3HomeFragment, "kk3HomeFragment");
            } else {
                fragmentTransaction.show(kk3HomeFragment);
            }
            hideFragment(fragmentTransaction, aaqqHomeFragment);
            mTvMovie.setText(R.string.tab_kk3);
        }
        hideFragment(fragmentTransaction, meFragment);
        hideFragment(fragmentTransaction, channelFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setColor(mTvMovie, true);
        setColor(mIfvMovie, true);
    }

    /**
     * 隐藏其他Fragment
     *
     * @param transaction FragmentTransaction
     * @param fragment    Fragment
     */
    private void hideFragment(FragmentTransaction transaction, Fragment fragment) {
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }

    private void initStatus() {
        setColor(mTvMovie, false);
        setColor(mIfvMovie, false);
        setColor(mTvLive, false);
        setColor(mIfvLive, false);
        setColor(mTvMe, false);
        setColor(mIfvMe, false);
    }

    private void setColor(TextView tv, boolean select) {
        tv.setTextColor(ContextCompat.getColor(this,
                select ? R.color.color_blue : R.color.color_99));
    }

    private QMUIListPopup mListPopup;

    private void initListPopupIfNeed() {
        if (mListPopup == null) {
            String[] listItems = new String[]{"吉吉", "首播"};
            List<String> data = new ArrayList<>();
            Collections.addAll(data, listItems);
            ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.pop_souce_choose, data);
            mListPopup = new QMUIListPopup(this, QMUIPopup.DIRECTION_NONE, adapter);
            mListPopup.create(QMUIDisplayHelper.dp2px(this, 150),
                    QMUIDisplayHelper.dp2px(this, 150),
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view,
                                                int index, long l) {
                            mListPopup.dismiss();
                            int saveIndex = SpUtils.getMovieHome(HomeActivity.this);
                            if (saveIndex == index) {
                                return;
                            }
                            SpUtils.saveMovieHome(HomeActivity.this, index);
                            showFirstFragment();
                        }
                    });
        }
    }

    /**
     * 检测应用更新
     */
    private void checkUpdate() {
        if (nextUpdate) {
            return;
        }
        mTvMovie.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (popupWindow != null && popupWindow.isShowing()) {
                    return;
                }
                Update update = UpdateOption.getUpdate();
                if (update == null || !update.isHasDownload()) {
                    return;
                }
                int updateCode = update.getVersionCode();
                int localCode = Utils.getLocalVersionCode(HomeActivity.this);
                if (updateCode > localCode) {
                    initUpdatePop();
                    showUpdatePop(update);
                }
            }
        }, 1000);
    }

    /**
     * 初始化升级弹窗
     */
    private void initUpdatePop() {
        convertView = LayoutInflater.from(this).inflate(R.layout.pop_update, null);
        popupWindow = new PopupWindow(convertView,
                Utils.dp2px(this, 300),
                Utils.dp2px(this, 320));
        popupWindow.setAnimationStyle(R.style.PopupStyle);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setOnDismissListener(this);
        //在popupwindow显示之后使fragment上其他控件失去焦点，不可点击
        popupWindow.setFocusable(true);
    }

    /**
     * 显示升级弹窗
     *
     * @param update Update
     */
    private void showUpdatePop(Update update) {
        if (convertView == null) {
            return;
        }
        TextView mTvVersion = convertView.findViewById(R.id.tv_version_name);
        mTvVersion.setText(update.getVersionName());
        TextView mTvContent = convertView.findViewById(R.id.tv_update_content);
        String content = update.getContent();
        if (!TextUtils.isEmpty(content)) {
            content = content.replace("\\n", "\n");
        }
        mTvContent.setText(content);
        convertView.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                nextUpdate = true;
            }
        });
        convertView.findViewById(R.id.tv_install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //安装应用
                Utils.installNormal(HomeActivity.this,
                        Constant.DIR_APK + Constant.FILE_APK_NAME);
            }
        });
        mTvMovie.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(mTvMovie, Gravity.CENTER, 0, 0);
            }
        });
        ScreenUtils.changeWindowAlpha(this, true);
    }

    @Override
    public void onDismiss() {
        ScreenUtils.changeWindowAlpha(this, false);
    }

    /**
     * Bmob分享返回传递数据
     *
     * @param scene Scene
     */
    @Override
    public void onReturnSceneData(Scene scene) {
        Map<String, Object> map = scene.getParams();
        if (map == null) {
            return;
        }
        String originUrl = (String) map.get("url");
        String imgUrl = (String) map.get("imgUrl");
        String source = (String) map.get("source");
        if (TextUtils.isEmpty(source)) {
            return;
        }
        Class clazz = AaqqVideoPlayActivity.class;
        if (TextUtils.equals(source, Constant.SOURCE_KK3)) {
            clazz = Kk3VideoPlayActivity.class;
        } else if (TextUtils.equals(source, Constant.SOURCE_GQZY)) {
            clazz = GqzyVideoPlayActivity.class;
        }
        Intent intent = new Intent();
        intent.putExtra("url", originUrl);
        intent.putExtra("imgUrl", imgUrl);
        startWithIntent(clazz, intent);
    }
}
