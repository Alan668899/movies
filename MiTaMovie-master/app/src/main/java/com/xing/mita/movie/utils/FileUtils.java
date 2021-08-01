package com.xing.mita.movie.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;

import com.xing.mita.movie.activity.common.LocalVideoActivity;
import com.xing.mita.movie.entity.LocalVideo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mita
 * @date 2018/11/24
 * @Description
 */
public class FileUtils {

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void updateMedia(Context context, String path) {
        //MediaStore.Audio.Media.EXTERNAL_CONTENT_URI数据库表所在路径
        //这里用到了后面的参数，第二个表示delete判断的条件，MediaStore.Audio.Media._ID表示
        context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.DATA + "= \"" + path + "\"",
                null);
    }

    /**
     * 调用文件选择软件来选择文件
     **/
    public static void showPicVideoChooser(Activity context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            context.startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
                    0);
        } catch (android.content.ActivityNotFoundException ex) {
            Log.i("xing", "没有文件管理器");
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 查询本地视频文件
     *
     * @param context Context
     * @return List<LocalVideo>
     */
    public static List<LocalVideo> getLocalVideo(Context context) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projections = {
                //名称
                MediaStore.Video.Media.DISPLAY_NAME,
                //时长
                MediaStore.Video.Media.DURATION,
                //大小
                MediaStore.Video.Media.SIZE,
                //路径
                MediaStore.Video.Media.DATA,
        };
        Cursor cursor = context.getContentResolver().query(uri, projections, null,
                null, null);
        if (cursor == null) {
            return null;
        }
        List<LocalVideo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            long duration = cursor.getLong(1);
            String during = TimeUtils.formatPlayTime(duration);
            long size = cursor.getLong(2);
            String videoSize = Formatter.formatFileSize(context, size);
            String data = cursor.getString(3);
            LocalVideo video = new LocalVideo();
            video.setName(name).setDuring(during).setSize(videoSize).setPath(data);
            list.add(video);
        }
        cursor.close();
        return list;
    }
}
