package com.xing.mita.movie.app;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.mob.MobSDK;
import com.mob.moblink.MobLink;
import com.mob.moblink.RestoreSceneListener;
import com.mob.moblink.Scene;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.xing.mita.movie.service.UmengNotificationService;

/**
 * @author Mita
 * @date 2018/10/11
 * @Description Application
 */
public class SysApplication extends MultiDexApplication {

    private final String TAG = getClass().getSimpleName();

    /**
     * 双击退出应用时间间隔
     */
    public static final int BACK_AGAIN_TIME = 1500;

    private static Context mContext;
    public static String BASE_URL;

    public static String DOWNLOAD_VIDEO_URL;
    public static String DOWNLOAD_VIDEO_CACHE_NAME;
    public static String DOWNLOAD_VIDEO_NAME;
    public static String DOWNLOAD_VIDEO_CACHE_SPEED;
    public static long DOWNLOAD_VIDEO_SIZE;
    public static int DOWNLOAD_VIDEO_CACHE_PROGRESS;
    public static int DOWNLOAD_TASK_ID;

    public static boolean HAS_DOWN_TASK;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        UMConfigure.init(this, "5bf3c1b4f1f55694e900042e", "Mitaer",
                UMConfigure.DEVICE_TYPE_PHONE, "84bc27e84e630292c72339d15fef5d3d");
        /**
         * 设置组件化的Log开关
         * 参数: boolean 默认为false，如需查看LOG设置为true
         */
        UMConfigure.setLogEnabled(true);
        initUpush();
        //Mob
        MobSDK.init(this);
        MobLink.setRestoreSceneListener(new SceneListener());

        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

    private void initUpush() {
        PushAgent mPushAgent = PushAgent.getInstance(this);

        //sdk开启通知声音
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);

        //注册推送服务 每次调用register都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.i(TAG, "device token: " + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.i(TAG, "register failed: " + s + " " + s1);
            }
        });

        //使用完全自定义处理
        mPushAgent.setPushIntentServiceClass(UmengNotificationService.class);
    }

    private class SceneListener extends Object implements RestoreSceneListener {

        @Override
        public Class<? extends Activity> willRestoreScene(Scene scene) {
            return null;
        }

        @Override
        public void completeRestore(Scene scene) {

        }

        @Override
        public void notFoundScene(Scene scene) {

        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    public static Context getContext() {
        return mContext;
    }
}
