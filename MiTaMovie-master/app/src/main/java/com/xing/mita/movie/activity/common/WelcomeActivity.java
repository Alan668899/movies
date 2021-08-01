package com.xing.mita.movie.activity.common;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.xing.mita.movie.R;
import com.xing.mita.movie.service.DownLoadApkService;
import com.xing.mita.movie.service.DownLoadVideoService;
import com.xing.mita.movie.utils.NetworkUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Setting;

import java.util.List;

/**
 * @author Mita
 * @date 2018/10/17
 * @Description 欢迎页
 */
public class WelcomeActivity extends BaseActivity {

    private String[] needPermissions = {Permission.READ_PHONE_STATE, Permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public int getContentViewResId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        requestPermission(needPermissions);
    }

    private void initData() {
        boolean connectWifi = NetworkUtils.isWifiConnected(this);
        if (connectWifi) {
            //开启下载服务
            DownLoadApkService.startDownload(this);
            //开启离线缓存
            DownLoadVideoService.startDownload(this);
        }
        startNoIntent(HomeActivity.class);
        finish();
    }

    /**
     * Request permissions.
     */
    private void requestPermission(String... permissions) {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        initData();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(
                                WelcomeActivity.this, permissions)) {
                            showSettingDialog(WelcomeActivity.this, permissions);
                        } else {
                            requestPermission(needPermissions);
                        }
                    }
                })
                .start();
    }

    /**
     * Display setting dialog.
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed,
                TextUtils.join("\n", permissionNames));
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(needPermissions);
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        requestPermission(needPermissions);
                    }
                })
                .start();
    }

}