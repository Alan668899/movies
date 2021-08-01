package com.xing.mita.movie.player;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.xing.mita.movie.R;
import com.xing.mita.movie.utils.TimeUtils;

/**
 * @author Mita
 * @date 2018/10/17
 * @Description 本地视频播放器
 */
public class LocalVideoPlayer extends StandardGSYVideoPlayer implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private TextView mMoreScale;
    /**
     * 音量显示
     */
    private TextView mTvVolume;
    private TextView mTvTime;

    private Context context;

    /**
     * 切换数据源类型
     */
    private int mType = 0;

    public LocalVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
        this.context = context;
    }

    public LocalVideoPlayer(Context context) {
        super(context);
        this.context = context;
    }

    public LocalVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_local_layout_cover_land;
    }

    @Override
    protected void init(final Context context) {
        super.init(context);
        mMoreScale = findViewById(R.id.moreScale);
        mTvTime = findViewById(R.id.tv_time);

        findViewById(R.id.fullscreen).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        if (mMoreScale != null) {
            mMoreScale.setOnClickListener(this);
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

    /**
     * 下方两个重载方法，在播放开始不显示底部进度
     */
    @Override
    protected void changeUiToPreparingShow() {
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
        super.startAfterPrepared();
        setViewShowState(mBottomContainer, INVISIBLE);
    }

    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        setViewShowState(mBottomContainer, INVISIBLE);
        showTime();
    }

    @Override
    public void changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow();
        setViewShowState(mStartButton, VISIBLE);
        showTime();
    }

    @Override
    public void onAutoCompletion() {
        getCurrentPlayer().onVideoPause();
        changeUiToPlayingBufferingShow();
        mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        showTime();
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        showTime();
    }


    @Override
    protected void changeUiToPlayingClear() {
        super.changeUiToPlayingClear();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPlayingBufferingClear() {
        super.changeUiToPlayingBufferingClear();
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        showTime();
    }

    @Override
    public void changeUiToCompleteClear() {
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
        if (isLock() && mBottomProgressBar != null) {
            mBottomProgressBar.setVisibility(View.VISIBLE);
        }
    }
}
