package com.xing.mita.movie.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.shuyu.gsyvideoplayer.utils.NetworkUtils;
import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.service.DownLoadVideoService;

/**
 * @author Mita
 * @date 2019/2/21
 * @Description
 */
public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            //判断当前网络时候可用
            boolean isConnected = NetworkUtils.isConnected(context);
            //网络类型
            int networkType = NetworkUtils.getNetWorkType(context);
            Log.w(TAG, "onReceive isConnected: " + isConnected);
            Log.w(TAG, "onReceive networkType: " + networkType);
            if (isConnected && networkType == NetworkUtils.NETWORK_WIFI
                    && !SysApplication.HAS_DOWN_TASK) {
                Log.w(TAG, "onReceive 启动下载--------------: ");
                DownLoadVideoService.startDownload(context);
            }
        }
    }
}