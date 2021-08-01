package com.xing.mita.movie.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Mita
 * @date 2018/11/2
 * @Description
 */
public class DateUtils {

    /**
     * 格式化日期：20181011
     *
     * @return long
     */
    public static long formatToDate(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String time = sdf.format(date);
            return Long.parseLong(time);
        } catch (NumberFormatException e) {
            Log.i("DateUtils", "日期转换错误：" + e.getMessage());
        }
        return 0;
    }

    /**
     * 昨天日期
     *
     * @return long
     */
    public static long formatYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date date = calendar.getTime();
        return formatToDate(date);
    }
}
