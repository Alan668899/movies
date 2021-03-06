package com.xing.mita.movie.fragment.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import butterknife.ButterKnife;

/**
 * @author mita
 */
public abstract class BaseFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName();

    protected View rootView;
    private boolean isInitView = false;
    public boolean isVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(setContentView(), container, false);
        ButterKnife.bind(this, rootView);
        init();
        isInitView = true;
        isCanLoadData();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isVisible = true;
            isCanLoadData();
        } else {
            isVisible = false;
        }
    }

    private void isCanLoadData() {
        //所以条件是view初始化完成并且对用户可见
        if (isInitView && isVisible) {
            lazyLoad();

            //防止重复加载数据
            isInitView = false;
            isVisible = false;
        }
    }

    /**
     * 加载页面布局文件
     *
     * @return 布局ID
     */
    protected abstract int setContentView();

    /**
     * 让布局中的view与fragment中的变量建立起映射
     */
    protected abstract void init();

    /**
     * 加载要显示的数据
     */
    protected abstract void lazyLoad();

    /**
     * 页面跳转
     *
     * @param clazz 目标页面
     */
    protected void startNoIntent(Class clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }

    /**
     * 页面跳转(带Intent)
     *
     * @param clazz 目标页面
     */
    protected void startWithIntent(Class clazz, Intent intent) {
        intent.setClass(Objects.requireNonNull(getActivity()), clazz);
        startActivity(intent);
    }

    /**
     * 打印
     *
     * @param msg String
     */
    protected void logI(String msg) {
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
}
