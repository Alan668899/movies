package com.xing.mita.movie.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.message.UmengMessageService;
import com.xing.mita.movie.dao.option.UpdateOption;
import com.xing.mita.movie.entity.Update;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.Utils;

import org.android.agoo.common.AgooConstants;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Mita
 * @date 2018/11/13
 * @Description 友盟推送服务
 */
public class UmengNotificationService extends UmengMessageService {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onMessage(Context context, Intent intent) {
        String message = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        if (TextUtils.isEmpty(message)) {
            return;
        }
        JSONTokener parse = new JSONTokener(message);
        Log.i(TAG, "onMessage：" + message);
        try {
            JSONObject obj = (JSONObject) parse.nextValue();
            JSONObject extraObj = obj.optJSONObject("extra");
            if (extraObj == null) {
                return;
            }
            String command = extraObj.optString("command");
            dealCommand(context, command, extraObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理自定义消息
     *
     * @param context Context
     * @param command String
     * @param obj     JSONObject
     */
    private void dealCommand(Context context, String command, JSONObject obj) {
        switch (command) {
            case Constant.UMENG_MSG_UPDATE:
                String vCode = obj.optString("versionCode");
                int code = Integer.parseInt(vCode);
                if (code <= Utils.getLocalVersionCode(context)) {
                    return;
                }
                String updateContent = obj.optString("updateContent");
                String versionName = obj.optString("versionName");
                String apkUrl = obj.optString("apkUrl");
                //保存升级信息
                Update update = new Update();
                update.setVersionCode(code)
                        .setVersionName(versionName)
                        .setContent(updateContent)
                        .setUrl(apkUrl);
                UpdateOption.saveUpdate(update);
                //开始下载
                DownLoadApkService.startDownload(context);
                break;

            case Constant.UMENG_MSG_UPDATE_LIVE:
                //启动直播源下载
                DownLoadLiveService.startDownload(context);
                break;

            default:
                break;
        }
    }

}
