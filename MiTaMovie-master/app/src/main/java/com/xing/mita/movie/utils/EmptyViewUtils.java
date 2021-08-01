package com.xing.mita.movie.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;

/**
 * @author Mita
 * @date 2018/10/24
 * @Description
 */
public class EmptyViewUtils {

    /**
     * 设置空布局
     *
     * @param context   Context
     * @param adapter   BaseQuickAdapter
     * @param layoutRes int
     * @return View
     */
    public static View setEmptyView(Context context, BaseQuickAdapter adapter, int layoutRes) {
        if (context == null) {
            return null;
        }
        View convertView = LayoutInflater.from(context).inflate(layoutRes, null);
        adapter.setEmptyView(convertView);
        return convertView;
    }
}
