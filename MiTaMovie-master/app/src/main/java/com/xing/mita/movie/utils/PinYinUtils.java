package com.xing.mita.movie.utils;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

/**
 * @author MiTa
 * @date 2017/12/21.
 */
public class PinYinUtils {

    /**
     * 将hanzi转成拼音
     *
     * @param word 汉字或字母
     * @return 拼音
     */
    public static String getPinyin(String word) {
        if (TextUtils.isEmpty(word)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //由于不能直接对多个汉子转换，只能对单个汉子转换
        char[] arr = word.toCharArray();
        for (char anArr : arr) {
            if (Character.isWhitespace(anArr)) {
                continue;
            }
            try {
                String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(anArr, format);
                if (pinyinArr != null) {
                    sb.append(pinyinArr[0]);
                } else {
                    sb.append(anArr);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //不是正确的汉字
                sb.append(anArr);
            }

        }
        return sb.toString();
    }
}
