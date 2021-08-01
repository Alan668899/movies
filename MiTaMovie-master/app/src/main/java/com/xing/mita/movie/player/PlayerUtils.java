package com.xing.mita.movie.player;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author Mita
 * @date 2018/11/26
 * @Description
 */
public class PlayerUtils {

    /**
     * 初始化播放器
     */
    public static void initPlayer(StandardGSYVideoPlayer videoPlayer,
                                  VideoAllCallBack playStateListener) {
        List<VideoOptionModel> list = new ArrayList<>();
        VideoOptionModel videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 30);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        list.add(videoOptionModel);
//        videoOptionModel = new VideoOptionModel(
//                IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
//        list.add(videoOptionModel);
        //  关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START
//        videoOptionModel = new VideoOptionModel(
//                IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
//        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);

        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        //打印日志等级
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_ERROR);
        //ijk模式
        PlayerFactory.setPlayManager(IjkPlayerManager.class);
        //代理缓存模式，支持所有模式，不支持m3u8等
        CacheFactory.setCacheManager(ProxyCacheManager.class);
        //设置监听
        videoPlayer.setVideoAllCallBack(playStateListener);
        //开启自动旋转
        videoPlayer.setRotateViewAuto(true);
        //跟随系统旋转
        videoPlayer.setRotateWithSystem(true);
        //全屏锁定屏幕
        videoPlayer.setNeedLockFull(true);
        //使用全屏动画效果
        videoPlayer.setShowFullAnimation(false);
        //一全屏就锁屏横屏，默认false竖屏
        videoPlayer.setLockLand(true);
        //调整触摸滑动快进的比例,默认1。数值越大，滑动的产生的seek越小
        videoPlayer.setSeekRatio(50);
    }

    /**
     * Exo模式或者IJK模式
     *
     * @param toExo boolean
     */
    public static void changeExoMode(boolean toExo) {
        if (toExo) {
            //exo模式
            PlayerFactory.setPlayManager(Exo2PlayerManager.class);
            //exo缓存模式，支持m3u8，只支持exo
            CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
        } else {
            //ijk模式
            PlayerFactory.setPlayManager(IjkPlayerManager.class);
            //代理缓存模式，支持所有模式，不支持m3u8等
            CacheFactory.setCacheManager(ProxyCacheManager.class);
        }
    }
}
