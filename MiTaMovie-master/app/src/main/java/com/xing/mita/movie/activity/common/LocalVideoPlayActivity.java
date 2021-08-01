package com.xing.mita.movie.activity.common;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.umeng.analytics.MobclickAgent;
import com.xing.mita.movie.R;
import com.xing.mita.movie.player.LocalVideoPlayer;
import com.xing.mita.movie.player.PlayerUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalVideoPlayActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.local_video_player)
    LocalVideoPlayer videoPlayer;

    private OrientationUtils orientationUtils;

    private String url;
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置布局
        setContentView(R.layout.activity_local_video_play);
        //绑定ButterKnife
        ButterKnife.bind(this);
        // 设置为U-APP场景
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        initPlayer();
        url = getIntent().getStringExtra("url");
        name = getIntent().getStringExtra("name");
        //获取图片真正的宽高
        Glide.with(this)
                .load(url)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        int width = resource.getIntrinsicWidth();
                        int height = resource.getIntrinsicHeight();
                        if (width > height) {
                            handler.sendEmptyMessage(0);
                        }
                        play(url, name);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        play(url, name);
                    }
                });
    }

    @Override
    protected void onResume() {
        //友盟统计
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //友盟统计
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放所有
        videoPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //设置为横屏
                    if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        try {
            //初始化播放器
            PlayerUtils.initPlayer(videoPlayer, playStateListener);
            //设置旋转
            orientationUtils = new OrientationUtils(this, videoPlayer);
            orientationUtils.setEnable(false);

            //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
            videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orientationUtils.resolveByClick();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "initPlayer 初始化播放器出错：" + e.getMessage());
        }
    }

    /**
     * 播放
     *
     * @param url String
     */
    private void play(String url, String name) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        //标题
        if (!TextUtils.isEmpty(name) && name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        //设置竖屏url
        videoPlayer.setUp(url, true, name);
        videoPlayer.getCurrentPlayer().startPlayLogic();
    }

    private VideoAllCallBack playStateListener = new GSYSampleCallBack() {

        @Override
        public void onPrepared(String url, Object... objects) {
            super.onPrepared(url, objects);
        }

        @Override
        public void onPlayError(String url, Object... objects) {
            super.onPlayError(url, objects);
        }

        @Override
        public void onAutoComplete(String url, Object... objects) {
            super.onAutoComplete(url, objects);
            finish();
        }

    };
}
