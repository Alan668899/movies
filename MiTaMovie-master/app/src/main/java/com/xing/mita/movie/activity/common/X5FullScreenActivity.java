package com.xing.mita.movie.activity.common;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.xing.mita.movie.R;
import com.xing.mita.movie.x5.X5WebView;

import butterknife.BindView;

/**
 * @author Mita
 * @date 2018/12/15
 * @Description X5视频播放
 */
public class X5FullScreenActivity extends BaseActivity {

    @BindView(R.id.web_filechooser)
    X5WebView webView;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_x5;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        //开启X5全屏播放模式
        enableX5FullscreenFunc();
        String url = getIntent().getStringExtra("url");
        webView.loadUrl(url);
        webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        setTranslucentBar(true);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            finish();
        }
    }

    /**
     * 确保注销配置能够被释放
     */
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    /**
     * 开启X5全屏播放模式
     */
    private void enableX5FullscreenFunc() {
        if (webView.getX5WebViewExtension() != null) {
            Bundle data = new Bundle();
            // true表示标准全屏，false表示X5全屏；不设置默认false，
            data.putBoolean("standardFullScreen", false);
            // false：关闭小窗；true：开启小窗；不设置默认true，
            data.putBoolean("supportLiteWnd", false);
            // 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
            data.putInt("DefaultVideoScreen", 2);
            webView.getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);
        }
    }

}
