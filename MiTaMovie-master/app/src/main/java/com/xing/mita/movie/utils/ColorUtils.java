package com.xing.mita.movie.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

/**
 * @author Mita
 * @date 2018/10/13
 * @Description
 */
public class ColorUtils {

    /**
     * 设置文字颜色
     *
     * @param context Context
     * @param tv      TextView
     * @param color   int
     */
    public static void setColor(Context context, TextView tv, int color) {
        tv.setTextColor(ContextCompat.getColor(context, color));
    }
}
