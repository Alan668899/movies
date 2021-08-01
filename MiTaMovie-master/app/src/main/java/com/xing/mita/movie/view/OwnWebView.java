package com.xing.mita.movie.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import me.jessyan.autosize.AutoSize;


/**
 * @author Mita
 * @date 2018/12/1
 * @Description
 */
public class OwnWebView extends WebView {

    private Activity activity;

    public OwnWebView(Context context) {
        super(context);
        activity = (Activity) context;
    }

    public OwnWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
    }

    public OwnWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = (Activity) context;
    }

    @Override
    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
        if (activity != null) {
            //WebView适配
            AutoSize.autoConvertDensityOfGlobal(activity);
        }
    }
}
