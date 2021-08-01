package com.xing.mita.movie.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Mita
 * @date 2018/11/8
 * @Description
 */
public class ShareUtils {

    private static final String TAG = "ShareUtils";

    /**
     * 分享图片到微信朋友圈,分享的是本地图片
     *
     * @param context Context
     * @param path    String
     */
    public static void shareImagesToWeiXin(Context context, String path) {
        try {
            Intent weChatIntent = new Intent();
            weChatIntent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI"));
            weChatIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> imageList = new ArrayList();
            File f = new File(path);
            if (f.exists()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    //android 7.0以下
                    imageList.add(Uri.fromFile(f));
                } else {//android 7.0及以上
                    Uri uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(context.getContentResolver(), f.getAbsolutePath(), f.getName(), null));
                    imageList.add(uri);
                }
            }
            weChatIntent.setType("image/*");
            weChatIntent.putExtra(Intent.EXTRA_STREAM, imageList);
            //分享描述
            weChatIntent.putExtra("Kdescription", "aaaa");
            context.startActivity(weChatIntent);
        } catch (Exception e) {
            Log.i(TAG, "分享失败: " + e.getMessage());
        }
    }

    /**
     * 分享
     *
     * @param context     Context
     * @param packageName 包名
     * @param className   类名
     * @param imagePath   图片路径
     */
    public static void share(Context context, String packageName, String className, String imagePath) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        if (!Utils.isInstall(context, packageName)) {
            Toast.makeText(context, "请先安装应用", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(imagePath);
        //由文件得到uri
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(context.getApplicationContext(),
                    "com.xing.mita.movie.fileprovider", file);
        } else {
            imageUri = Uri.fromFile(file);
        }
        Intent shareIntent = new Intent();
        if (TextUtils.isEmpty(className)) {
            shareIntent.setPackage(packageName);
        } else {
            shareIntent.setClassName(packageName, className);
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        context.startActivity(Intent.createChooser(shareIntent, "分享影片"));
    }
}
