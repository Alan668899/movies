package com.xing.mita.movie.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Mita
 * @date 2018/10/13
 * @Description SharedPreferences操作类
 */
public class SpUtils {

    /**
     * 保存电影首页下标
     *
     * @param context Context
     * @param index   下标
     */
    public static void saveMovieHome(Context context, int index) {
        SharedPreferences sp = getSp(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("movieHome", index).apply();
    }

    /**
     * 获取电影首页下标
     *
     * @param context Context
     * @return 0:首播     1：吉吉
     */
    public static int getMovieHome(Context context) {
        return getSp(context).getInt("movieHome", 0);
    }

    /**
     * 保存视频排序
     *
     * @param context Context
     * @param order   String
     */
    public static void saveVideoOrder(Context context, String order) {
        SharedPreferences sp = getSp(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("videoOrder", order).apply();
    }

    /**
     * 获取保存的排序
     *
     * @param context Context
     * @return String
     */
    public static String getVideoOrder(Context context) {
        return getSp(context).getString("videoOrder", "hits");
    }

    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences("XingMi", 0);
    }
}
