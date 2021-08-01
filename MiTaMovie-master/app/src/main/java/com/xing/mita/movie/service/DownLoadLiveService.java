package com.xing.mita.movie.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.xing.mita.movie.dao.option.ProgramOption;
import com.xing.mita.movie.entity.Program;
import com.xing.mita.movie.utils.Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mita
 * @date 2018/11/23
 * @Description 直播源下载服务
 */
public class DownLoadLiveService extends IntentService {

    private final String TAG = getClass().getSimpleName();

    public DownLoadLiveService() {
        super("DownLoadLiveService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        File file = new File(Constant.DIR_LIVE);
        if (!file.exists()) {
            file.mkdirs();
        }
        downLoadFromUrl(Constant.LIVE_DOWNLOAD_URL);
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     */
    public void downLoadFromUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            InputStream inputStream = conn.getInputStream();

            String name = Constant.DIR_LIVE + Constant.FILE_LIVE_NAME;
            FileOutputStream fos = new FileOutputStream(name);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            inputStream.close();
            Log.w(TAG, "downLoadFromUrl: 直播源下载成功");
            convert();
        } catch (Exception e) {
            Log.e(TAG, "downLoadFromUrl file download Fail: " + e.getMessage());
            //通知界面直播源下载失败
            Intent intent = new Intent(Constant.BROADCAST_LIVE_SOURCE_DOWNLOAD_FAIL);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * 解析直播源
     */
    private void convert() {
        File file = new File(Constant.DIR_LIVE + Constant.FILE_LIVE_NAME);
        if (!file.exists()) {
            return;
        }
        FileInputStream fis = null;
        InputStreamReader reader = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis);
            br = new BufferedReader(reader);
            List<Program> list = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                Program program = convertProgram(line);
                if (program != null) {
                    list.add(program);
                }
            }
            //保存节目
            ProgramOption.save(list);

            //通知界面更新直播源
            Intent intent = new Intent(Constant.BROADCAST_LIVE_SOURCE_UPDATE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "convert 直播源解析异常: " + e.getMessage());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析一行数据成Program对象
     *
     * @param line String
     * @return Program
     */
    private Program convertProgram(String line) {
        String splitStr = ";";
        if (!line.contains(splitStr)) {
            return null;
        }
        String[] info = line.split(splitStr);
        Program program = new Program();
        int length = info.length;
        if (length > 0) {
            program.setSource(info[0]);
        }
        if (length > 1) {
            program.setName(info[1]);
        }
        if (length > 2) {
            program.setParadeUrl(info[2]);
        }
        return program;
    }

    public static void startDownload(Context context) {
        Intent intent = new Intent(context, DownLoadLiveService.class);
        context.startService(intent);
    }
}
