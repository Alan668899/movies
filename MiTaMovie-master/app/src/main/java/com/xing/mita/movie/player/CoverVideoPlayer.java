package com.xing.mita.movie.player;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.xing.mita.movie.R;
import com.xing.mita.movie.utils.TimeUtils;

/**
 * @author Mita
 * @date 2018/10/17
 * @Description 视频播放器
 */
public class CoverVideoPlayer extends StandardGSYVideoPlayer implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    /**
     * 封面
     */
    private ImageView mCoverImage;
    private TextView mMoreScale;
    /**
     * 音量显示
     */
    private TextView mTvVolume;
    private TextView mTvTime;
    private TextView mTvSpeed;

    private Context context;

    /**
     * 切换数据源类型
     */
    private int mType = 0;

    private String mCoverOriginUrl;

    public CoverVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
        this.context = context;
    }

    public CoverVideoPlayer(Context context) {
        super(context);
        this.context = context;
    }

    public CoverVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public int getLayoutId() {
        if (mIfCurrentIsFullscreen) {
            return R.layout.video_layout_cover_land;
        }
        return R.layout.video_layout_cover;
    }

    @Override
    protected void init(final Context context) {
        super.init(context);
        mCoverImage = findViewById(R.id.thumbImage);
        mMoreScale = findViewById(R.id.moreScale);
        mTvTime = findViewById(R.id.tv_time);
        mTvSpeed = findViewById(R.id.tv_speed);
        ImageView mIvNext = findViewById(R.id.iv_next);

        if (mThumbImageViewLayout != null &&
                (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL
                        || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }

        findViewById(R.id.fullscreen).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        if (mMoreScale != null) {
            mMoreScale.setOnClickListener(this);
        }
        if (mIvNext != null) {
            mIvNext.setOnClickListener(this);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (getCurrentState() == CURRENT_STATE_AUTO_COMPLETE && mHadPlay && getGSYVideoManager().getPlayer() != null) {
            try {
                int currentPosition = seekBar.getProgress() * getDuration() / 100;
                setSeekOnStart(currentPosition);
                startPlayLogic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            super.onStopTrackingTouch(seekBar);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        if (fromUser && mHadPlay && getGSYVideoManager().getPlayer() != null) {
            try {
                int currentPosition = seekBar.getProgress() * getDuration() / 100;
                mCurrentTimeTextView.setText(TimeUtils.formatPlayTime(currentPosition));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fullscreen:
                startWindowFullscreen(context, false, true);
                break;

            case R.id.moreScale:
                if (!mHadPlay) {
                    return;
                }
                if (mType == 0) {
                    mType = 1;
                } else if (mType == 1) {
                    mType = 2;
                } else if (mType == 2) {
                    mType = 3;
                } else if (mType == 3) {
                    mType = 4;
                } else if (mType == 4) {
                    mType = 0;
                }
                resolveTypeUI();
                break;

            case R.id.iv_next:
                onAutoCompletion();
                break;

            case R.id.start:
                switch (mCurrentState) {
                    case CURRENT_STATE_PLAYING:
                        onVideoPause();
                        break;

                    case CURRENT_STATE_PAUSE:
                        onVideoResume();
                        break;

                    case CURRENT_STATE_AUTO_COMPLETE:
                    case CURRENT_STATE_ERROR:
                        onVideoReset();
                        break;

                    default:
                        break;
                }
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
                    if (mTvSpeed != null) {
                        mTvSpeed.setText(getNetSpeedText());
                        handler.removeMessages(0);
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public void loadCoverImage(String url) {
        mCoverOriginUrl = url;
        Glide.with(getContext().getApplicationContext())
                .load(url)
                .into(mCoverImage);
    }

    /**
     * 下方两个重载方法，在播放开始前不屏蔽封面
     */
    @Override
    public void onSurfaceUpdated(Surface surface) {
        super.onSurfaceUpdated(surface);
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.getVisibility() == VISIBLE) {
            mThumbImageViewLayout.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void setViewShowState(View view, int visibility) {
        if (view == mThumbImageViewLayout && visibility != VISIBLE) {
            return;
        }
        super.setViewShowState(view, visibility);
    }


    /**
     * 下方两个重载方法，在播放开始不显示底部进度
     */
    @Override
    protected void changeUiToPreparingShow() {
        Log.w(TAG, "changeUiToPreparingShow: ");

        super.changeUiToPreparingShow();
        setViewShowState(mBottomContainer, INVISIBLE);
        showTime();
    }

    private void showTime() {
        if (mTvTime != null) {
            mTvTime.setText(TimeUtils.nowTime());
        }
    }

    @Override
    public void startAfterPrepared() {
        Log.w(TAG, "startAfterPrepared: ");

        super.startAfterPrepared();
        setViewShowState(mBottomContainer, INVISIBLE);
    }

    @Override
    protected void changeUiToCompleteShow() {
        Log.w(TAG, "changeUiToCompleteShow: ");

        super.changeUiToCompleteShow();
        setViewShowState(mBottomContainer, INVISIBLE);
        showTime();
    }

    @Override
    public void changeUiToPlayingBufferingShow() {
        Log.w(TAG, "changeUiToPlayingBufferingShow: ");
        super.changeUiToPlayingBufferingShow();
        setViewShowState(mStartButton, VISIBLE);
        showTime();
        handler.sendEmptyMessage(0);
        if (isLock()) {
            setViewShowState(mTopContainer, INVISIBLE);
            setViewShowState(mBottomContainer, INVISIBLE);
        }
    }

    @Override
    public void onCompletion() {
        Log.w(TAG, "onCompletion: ");

        super.onCompletion();
        setViewShowState(mCoverImage, VISIBLE);
    }

    @Override
    public void onAutoCompletion() {
        Log.w(TAG, "onAutoCompletion: ");

        getCurrentPlayer().onVideoPause();
        changeUiToPlayingBufferingShow();
        mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    protected void changeUiToNormal() {
        Log.w(TAG, "changeUiToNormal: ");

        super.changeUiToNormal();
        showTime();
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        showTime();
        handler.removeMessages(0);
    }


    @Override
    protected void changeUiToPlayingClear() {
        Log.w(TAG, "changeUiToPlayingClear: ");
        super.changeUiToPlayingClear();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        Log.w(TAG, "changeUiToPauseClear: ");
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPlayingBufferingClear() {
        Log.w(TAG, "changeUiToPlayingBufferingClear: ");
        super.changeUiToPlayingBufferingClear();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPauseShow() {
        Log.w(TAG, "changeUiToPauseShow: ");
        super.changeUiToPauseShow();
        showTime();
    }

    @Override
    public void changeUiToCompleteClear() {
        Log.w(TAG, "changeUiToCompleteClear: ");
        super.changeUiToCompleteClear();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private void resolveTypeUI() {
        if (!mHadPlay) {
            return;
        }
        if (mType == 1) {
            mMoreScale.setText("16:9");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
        } else if (mType == 2) {
            mMoreScale.setText("4:3");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
        } else if (mType == 3) {
            mMoreScale.setText("全屏");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        } else if (mType == 4) {
            mMoreScale.setText("拉伸全屏");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        } else if (mType == 0) {
            mMoreScale.setText("默认比例");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        }
        changeTextureViewShowType();
        if (mTextureView != null) {
            mTextureView.requestLayout();
        }
    }

    /**
     * 重写音量布局
     */
    @Override
    protected int getVolumeLayoutId() {
        return R.layout.dialog_player_volume;
    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(getVolumeLayoutId(), null);
            mTvVolume = localView.findViewById(R.id.tv_volume);
            mVolumeDialog = new Dialog(getActivityContext(), com.shuyu.gsyvideoplayer.R.style.video_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            Window window = mVolumeDialog.getWindow();
            if (window != null) {
                window.addFlags(8);
                window.addFlags(32);
                window.addFlags(16);
                window.setLayout(-2, -2);
                WindowManager.LayoutParams localLayoutParams = window.getAttributes();
                localLayoutParams.gravity = Gravity.TOP | Gravity.START;
                localLayoutParams.width = getWidth();
                localLayoutParams.height = getHeight();
                int[] location = new int[2];
                getLocationOnScreen(location);
                localLayoutParams.x = location[0];
                localLayoutParams.y = location[1];
                window.setAttributes(localLayoutParams);
            }
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }
        if (mTvVolume == null) {
            return;
        }
        if (volumePercent < 0) {
            volumePercent = 0;
        } else if (volumePercent > 100) {
            volumePercent = 100;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(volumePercent);
        sb.append("%");
        mTvVolume.setText(sb);
    }

    /**
     * 重写亮度布局
     */
    @Override
    protected int getBrightnessLayoutId() {
        return R.layout.dialog_player_brightness;
    }

    /**
     * 重写触摸进度布局
     */
    @Override
    protected int getProgressDialogLayoutId() {
        return R.layout.dialog_player_duration_progress;
    }

    /**
     * 屏幕是否锁定
     *
     * @return boolean
     */
    public boolean isLock() {
        return mLockCurScreen;
    }

    @Override
    protected void onClickUiToggle() {
        super.onClickUiToggle();
        if (isLock()) {
            if (mBottomProgressBar != null) {
                mBottomProgressBar.setVisibility(View.VISIBLE);
            }
            setViewShowState(mTopContainer, INVISIBLE);
            setViewShowState(mBottomContainer, INVISIBLE);
        }
    }
}
