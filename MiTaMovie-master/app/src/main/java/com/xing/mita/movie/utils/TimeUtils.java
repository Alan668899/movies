package com.xing.mita.movie.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Mita
 * @date 2018/11/26
 * @Description
 */
public class TimeUtils {

    /**
     * 格式化播放时间
     *
     * @param time long
     * @return String
     */
    public static String formatPlayTime(long time) {
        SimpleDateFormat sdf;
        if (time >= 60 * 60 * 1000) {
            sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        } else {
            sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
        }
        return sdf.format(time);
    }

    /**
     * 格式化当前时间
     *
     * @return String
     */
    public static String nowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }
}
