package com.xing.mita.movie.utils;

import android.os.Environment;

/**
 * @author Mita
 * @date 2018/10/13
 * @Description
 */
public class Constant {

    /**
     * 吐槽(反馈)url
     */
    public static final String TUCAO_URL = "https://support.qq.com/product/51890";

    /**
     * AAQQY网址
     */
    public static final String SOURCE_AAQQY = "http://aaqqy.com/";

    /**
     * AAQQY网址(移动端)
     */
    public static final String SOURCE_AAQQY_MOBILE = "http://m.aaqqy.com";

    /**
     * KK3网址
     */
    public static final String SOURCE_KK3 = "http://m.kk3.tv/";

    /**
     * KK3最近更新
     */
    public static final String SOURCE_KK3_RECOMMEND = "http://m.kk3.tv/new.html";

    /**
     * 高清资源网
     */
    public static final String SOURCE_GQZY = "http://gaoqingzy.com";

    /**
     * 高清资源网搜索
     */
    public static final String SEARCH_GQZY = SOURCE_GQZY + "/index.php?m=vod-search&wd=";

    /**
     * 头像网
     */
    public static final String SOURCE_HEAD = "https://www.enterdesk.com/qqtx/kt/";

    /**
     * 视频排序：最新、人气、评分
     */
    public static final String ORDER_BY_TIME = "time";
    public static final String ORDER_BY_HITS = "hits";
    public static final String ORDER_BY_SCORE = "score";

    /**
     * 最高下载权限
     */
    public static final int DOWNLOAD_FIRST_PRIORITY = 77;
    /**
     * 最低下载权限
     */
    public static final int DOWNLOAD_LAST_PRIORITY = -1;

    /**
     * 文件存储根目录
     */
    private static final String DIR_ROOT = Environment.getExternalStorageDirectory() + "/XingMi";

    /**
     * 图片存储目录
     */
    public static final String DIR_IMAGE = DIR_ROOT + "/images/";

    /**
     * 升级包存储目录
     */
    public static final String DIR_APK = DIR_ROOT + "/apk/";

    /**
     * 直播源存储目录
     */
    public static final String DIR_LIVE = DIR_ROOT + "/live/";

    /**
     * 直播源存储目录
     */
    public static final String DIR_VIDEO = DIR_ROOT + "/video/";

    /**
     * 直播源文件名
     */
    public static final String FILE_LIVE_NAME = "liveSource.txt";

    /**
     * 升级包名称
     */
    public static final String FILE_APK_NAME = "XingmiMovie.apk";

    /**
     * 缓存视频前缀
     */
    public static final String FILE_VIDEO_PREFIX = "Xingmi";

    /**
     * Bmob apk短链接
     */
    public static final String APK_URL = "http://4t.t4m.cn/";

    /**
     * apk下载路径
     */
    public static final String APK_DOWNLOAD_URL =
            "https://raw.githubusercontent.com/Mitaxing/XingMiMovie/master/xingmi.apk";

    /**
     * 直播源下载路径
     */
    public static final String LIVE_DOWNLOAD_URL =
            "https://raw.githubusercontent.com/Mitaxing/XingMiMovie/master/LiveSource.txt";

    //友盟推送

    /**
     * 友盟推送————更新apk
     */
    public static final String UMENG_MSG_UPDATE = "update";

    /**
     * 友盟推送————更新直播源
     */
    public static final String UMENG_MSG_UPDATE_LIVE = "updateLive";

    //广播

    /**
     * 广播————通知AAQQ加载搜素推荐内容
     */
    public static final String BROADCAST_AAQQ_LOAD_SEARCH_RECOMMEND = "aaqqLoadSearchRecommend";

    /**
     * 广播————通知KK3加载搜素推荐内容
     */
    public static final String BROADCAST_KK3_LOAD_SEARCH_RECOMMEND = "kk3LoadSearchRecommend";

    /**
     * 广播————通知显示对应页面
     */
    public static final String BROADCAST_SHOW_FRAGMENT_INDEX = "showFragmentIndex";

    /**
     * 广播————通知直播源更新
     */
    public static final String BROADCAST_LIVE_SOURCE_UPDATE = "liveSourceHasUpdate";

    /**
     * 广播————通知直播源下载失败
     */
    public static final String BROADCAST_LIVE_SOURCE_DOWNLOAD_FAIL = "liveSourceDownloadFail";

    /**
     * 广播————通知视频下载开始
     */
    public static final String BROADCAST_VIDEO_DOWNLOAD_START = "videoDownloadStart";

    /**
     * 广播————通知视频下载暂停
     */
    public static final String BROADCAST_VIDEO_DOWNLOAD_PAUSE = "videoDownloadPause";

    /**
     * 广播————通知视频下载成功
     */
    public static final String BROADCAST_VIDEO_DOWNLOAD_SUCCESS = "videoDownloadSuccess";

    /**
     * 广播————通知视频下载失败
     */
    public static final String BROADCAST_VIDEO_DOWNLOAD_FAIL = "videoDownloadFail";
}
