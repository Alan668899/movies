package com.xing.mita.movie.activity.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.umeng.analytics.MobclickAgent;
import com.xing.mita.movie.R;
import com.xing.mita.movie.receiver.NetworkConnectChangedReceiver;

import butterknife.ButterKnife;
import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/9
 * @Description
 */
@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();

    protected ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //去除主题windowBackground
        getWindow().getDecorView().setBackground(null);
        //设置布局
        setContentView(getContentViewResId());
        //绑定ButterKnife
        ButterKnife.bind(this);
        //初始化状态栏
        initWhiteBar();
        //初始化
        init(savedInstanceState);
        // 设置为U-APP场景
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        //注册网络状态监听广播
        regNetworkReceiver();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        AutoSize.autoConvertDensityOfGlobal(this);
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销状态栏设置
        if (mImmersionBar != null) {
            ImmersionBar.with(this).destroy();
        }
        //注销网络状态广播
        if (netWorkChangReceiver != null) {
            unregisterReceiver(netWorkChangReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //友盟统计
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //友盟统计
        MobclickAgent.onPause(this);
    }

    /**
     * 获取布局
     *
     * @return layoutId
     */
    public abstract int getContentViewResId();

    /**
     * 初始化
     *
     * @param savedInstanceState Bundle
     */
    public abstract void init(Bundle savedInstanceState);

    /**
     * 初始化状态栏（颜色White）
     */
    protected void initWhiteBar() {
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColor(R.color.main_color)
                .fitsSystemWindows(true)
                .flymeOSStatusBarFontColor(R.color.black)
                .statusBarDarkFont(true)
                .init();
    }

    /**
     * 初始化状态栏（颜色White）
     */
    protected void initBlackBar() {
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColor(R.color.black)
                .fitsSystemWindows(true)
                .flymeOSStatusBarFontColor(R.color.white)
                .statusBarDarkFont(false)
                .init();
    }

    /**
     * 初始化状态栏
     *
     * @param isTranslucent 是否透明
     */
    protected void setTranslucentBar(boolean isTranslucent) {
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColor(isTranslucent ? R.color.translucent : R.color.main_color)
                .fitsSystemWindows(!isTranslucent)
                .flymeOSStatusBarFontColor(R.color.black)
                .statusBarDarkFont(true)
                .init();
    }

    /**
     * 页面跳转
     *
     * @param clazz 目标页面
     */
    protected void startNoIntent(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    /**
     * 页面跳转(带Intent)
     *
     * @param clazz 目标页面
     */
    protected void startWithIntent(Class clazz, Intent intent) {
        intent.setClass(this, clazz);
        startActivity(intent);
    }

    /**
     * 页面跳转(带返回)
     *
     * @param clazz 目标页面
     */
    protected void startForResult(Class clazz, Intent intent, int requestCode) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.setClass(this, clazz);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 打印
     *
     * @param msg String
     */
    protected void log(String msg) {
        Log.i(TAG, msg);
    }

    /**
     * 打印
     *
     * @param msg String
     */
    protected void logE(String msg) {
        Log.e(TAG, msg);
    }

    /**
     * toast
     *
     * @param msg String
     */
    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * toast
     *
     * @param msgRes int
     */
    protected void showToast(int msgRes) {
        Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show();
    }

    private NetworkConnectChangedReceiver netWorkChangReceiver;

    /**
     * 注册网络状态监听广播
     */
    private void regNetworkReceiver() {
        netWorkChangReceiver = new NetworkConnectChangedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
    }
}
