package com.xing.mita.movie.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.dao.option.DownloadOption;
import com.xing.mita.movie.entity.Download;
import com.xing.mita.movie.utils.Constant;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Mita
 * @date 2018/11/23
 * @Description 视频下载服务
 */
public class DownLoadVideoService extends IntentService {

    private final String TAG = getClass().getSimpleName();

    private static DownloadTask task;

    private long totalLength;
    private String fileName;
    private Download down;

    public DownLoadVideoService() {
        super("DownLoadVideoService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        down = DownloadOption.loadFirstDown();
        if (down == null) {
            return;
        }
        File file = new File(Constant.DIR_VIDEO);
        if (!file.exists()) {
            file.mkdirs();
        }
        String url = down.getUrl();
        fileName = down.getCacheName();
        if (TextUtils.isEmpty(fileName)) {
            String suffix = ".mp4";
            if (url.contains(".mkv")) {
                suffix = ".mkv";
            } else if (url.contains(".avi")) {
                suffix = ".avi";
            } else if (url.contains(".mov")) {
                suffix = ".mov";
            } else if (url.contains(".rmvb")) {
                suffix = ".rmvb";
            }
            int index = 0;
            while (true) {
                fileName = Constant.FILE_VIDEO_PREFIX + index + suffix;
                File f = new File(Constant.DIR_VIDEO + fileName);
                if (!f.exists()) {
                    break;
                }
                index++;
            }
            down.setCacheName(fileName);
            //更新缓存名称
            DownloadOption.update(down);
        }
        task = new DownloadTask.Builder(url, file)
                .setFilename(fileName)
                .setMinIntervalMillisCallbackProcess(800)
                .setPassIfAlreadyCompleted(false)
                .build();
        task.setTag(url);
        listener.setAlwaysRecoverAssistModel(true);
        task.enqueue(listener);
        DownloadListener4WithSpeed listener4WithSpeed = (DownloadListener4WithSpeed) task.getListener();
        if (listener4WithSpeed != null) {
            task.replaceListener(listener);
        }

        SysApplication.DOWNLOAD_VIDEO_URL = url;
        SysApplication.DOWNLOAD_VIDEO_CACHE_NAME = fileName;
        SysApplication.DOWNLOAD_VIDEO_NAME = down.getName() + down.getEpisode();
    }

    DownloadListener4WithSpeed listener = new DownloadListener4WithSpeed() {

        @Override
        public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info,
                              boolean fromBreakpoint,
                              @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
            totalLength = info.getTotalLength();
            SysApplication.DOWNLOAD_VIDEO_SIZE = totalLength;
            //发送广播通知视频下载开始
            Intent intent = new Intent(Constant.BROADCAST_VIDEO_DOWNLOAD_START);
            LocalBroadcastManager.getInstance(DownLoadVideoService.this).sendBroadcast(intent);
            Log.w(TAG, "infoReady: ---------");
        }

        @Override
        public void progressBlock(@NonNull DownloadTask task, int blockIndex,
                                  long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
            Log.w(TAG, "progressBlock: ===========");
        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset,
                             @NonNull SpeedCalculator taskSpeed) {
            if (StatusUtil.getStatus(task) == StatusUtil.Status.RUNNING) {
                Log.w(TAG, "progress: " + taskSpeed.speed());
                Log.w(TAG, "progress: " + (int) (currentOffset * 100 / totalLength));
                //全局记录下载任务
                SysApplication.HAS_DOWN_TASK = true;
                SysApplication.DOWNLOAD_TASK_ID = task.getId();
                SysApplication.DOWNLOAD_VIDEO_CACHE_SPEED = taskSpeed.speed();
                SysApplication.DOWNLOAD_VIDEO_CACHE_PROGRESS = (int) (currentOffset * 100 / totalLength);
            }
        }

        @Override
        public void blockEnd(@NonNull DownloadTask task, int blockIndex,
                             BlockInfo info, @NonNull SpeedCalculator blockSpeed) {
            Log.w(TAG, "blockEnd: --------");
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                            @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
            Log.w(TAG, "taskEnd: " + cause);
            if (cause == EndCause.COMPLETED) {
                if (down == null) {
                    return;
                }
                //重命名
                String name = Constant.DIR_VIDEO + fileName;
                File oldFile = new File(name);
                String splitStr = ".";
                if (name.contains(splitStr)) {
                    name = name.substring(name.lastIndexOf(splitStr));
                }
                name = Constant.DIR_VIDEO + down.getName() + down.getEpisode() + name;
                File newFile = new File(name);
                oldFile.renameTo(newFile);
                //删除下载记录
                DownloadOption.delete(down.getUrl());
                OkDownload.with().breakpointStore().remove(task.getId());

                //发送广播通知视频下载完成
                Intent intent = new Intent(Constant.BROADCAST_VIDEO_DOWNLOAD_SUCCESS);
                LocalBroadcastManager.getInstance(DownLoadVideoService.this).sendBroadcast(intent);

                //继续下载
                startDownload(DownLoadVideoService.this);
            } else if (cause == EndCause.CANCELED) {
                //发送广播通知视频下载暂停
                Intent intent = new Intent(Constant.BROADCAST_VIDEO_DOWNLOAD_PAUSE);
                LocalBroadcastManager.getInstance(DownLoadVideoService.this).sendBroadcast(intent);
            } else {
                //发送广播通知视频下载失败
                Intent intent = new Intent(Constant.BROADCAST_VIDEO_DOWNLOAD_FAIL);
                LocalBroadcastManager.getInstance(DownLoadVideoService.this).sendBroadcast(intent);
            }
            //重置下载信息
            SysApplication.HAS_DOWN_TASK = false;
            SysApplication.DOWNLOAD_VIDEO_CACHE_PROGRESS = 0;
            SysApplication.DOWNLOAD_VIDEO_CACHE_SPEED = "";
        }

        @Override
        public void taskStart(@NonNull DownloadTask task) {
            //全局记录下载任务
            SysApplication.HAS_DOWN_TASK = true;
            Log.w(TAG, "taskStart: -----------==");
        }

        @Override
        public void connectStart(@NonNull DownloadTask task, int blockIndex,
                                 @NonNull Map<String, List<String>> requestHeaderFields) {
            Log.w(TAG, "connectStart: =======-------");
        }

        @Override
        public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
                               @NonNull Map<String, List<String>> responseHeaderFields) {
            Log.w(TAG, "connectEnd: ++++++++++");
        }
    };

    public static void startDownload(Context context) {
        Intent intent = new Intent(context, DownLoadVideoService.class);
        //TODO Not allowed to start service Intent app is in background
        context.startService(intent);
    }

    /**
     * 暂停下载任务
     */
    public static void cancelTask() {
        if (task != null) {
            task.cancel();
        }
    }

}