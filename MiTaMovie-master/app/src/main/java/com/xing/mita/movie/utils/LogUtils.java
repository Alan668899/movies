package com.xing.mita.movie.utils;

import android.util.Log;

/**
 * @author Mita
 * @date 2018/10/11
 * @Description
 */
public class LogUtils {

    /**
     * 打印超长log
     *
     * @param tag tag
     * @param msg msg
     */
    public static void i(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int maxStrLength = 2001 - tag.length();
        //大于4000时
        while (msg.length() > maxStrLength) {
            Log.i(tag, msg.substring(0, maxStrLength));
            msg = msg.substring(maxStrLength);
        }
        //剩余部分
        Log.i(tag, msg);
    }

}
