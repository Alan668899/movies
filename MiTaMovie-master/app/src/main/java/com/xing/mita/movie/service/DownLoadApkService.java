package com.xing.mita.movie.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;
import com.xing.mita.movie.dao.option.UpdateOption;
import com.xing.mita.movie.entity.Update;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.FileUtils;
import com.xing.mita.movie.utils.NetworkUtils;
import com.xing.mita.movie.utils.Utils;

import java.io.File;
import java.text.DecimalFormat;

/**
 * @author Mita
 * @date 2018/11/23
 * @Description APK下载服务
 */
public class DownLoadApkService extends IntentService {

    private final String TAG = getClass().getSimpleName();

    public DownLoadApkService() {
        super("DownLoadApkService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean connectWifi = NetworkUtils.isWifiConnected(this);
        if (intent == null || !connectWifi) {
            return;
        }
        int versionCode = Utils.getLocalVersionCode(this);
        Update update = UpdateOption.getUpdate();
        if (update == null) {
            return;
        }
        if (update.getVersionCode() <= versionCode) {
            //清除升级信息
            UpdateOption.clear();
            //删除升级包
            String path = Constant.DIR_APK + Constant.FILE_APK_NAME;
            FileUtils.deleteFile(path);
            return;
        }
        if (update.isHasDownload()) {
            return;
        }
        File file = new File(Constant.DIR_APK);
        if (!file.exists()) {
            file.mkdirs();
        }
        DownloadTask task = new DownloadTask.Builder(update.getUrl(), file)
                .setFilename(Constant.FILE_APK_NAME)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(30)
                // do re-download even if the task has already been completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
        task.setTag(Constant.UMENG_MSG_UPDATE);
        task.enqueue(listener);
    }

    DownloadListener1 listener = new DownloadListener1() {
        @Override
        public void taskStart(@NonNull DownloadTask task,
                              @NonNull Listener1Assist.Listener1Model model) {
            Log.i(TAG, "taskStart: 开始下载··········");
        }

        @Override
        public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

        }

        @Override
        public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset,
                              long totalLength) {

        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
            double progress = (double) currentOffset / totalLength;
            String pro = new DecimalFormat("#.##%").format(progress);
            Log.i(TAG, "progress: 下载进度：" + pro);
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                            @Nullable Exception realCause,
                            @NonNull Listener1Assist.Listener1Model model) {
            if (EndCause.COMPLETED == cause) {
                Log.i(TAG, "taskEnd: download success");
                UpdateOption.updateHasDownload();
                //删除下载记录
                OkDownload.with().breakpointStore().remove(task.getId());
            } else if (EndCause.ERROR == cause) {
                Log.i(TAG, "taskEnd: download fail：" + cause);
            }
        }
    };

    public static void startDownload(Context context) {
        Intent intent = new Intent(context, DownLoadApkService.class);
        context.startService(intent);
    }
}