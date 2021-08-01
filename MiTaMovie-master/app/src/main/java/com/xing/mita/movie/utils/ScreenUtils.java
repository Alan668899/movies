package com.xing.mita.movie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.xing.mita.movie.R;


/**
 * @author mita
 */
public class ScreenUtils {

    /**
     * 检测是否处于分屏
     *
     * @return boolean
     */
    public static boolean checkIsInMultiWindow(Activity activity,int tipRes) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }
        if (activity.isInMultiWindowMode()) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(tipRes)
                    .setPositiveButton(R.string.confirm, null)
                    .create();
            dialog.show();
            return true;
        }
        return false;
    }

    /**
     * 获取屏幕高度
     *
     * @param context Context
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context Context
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 判断屏幕是否横屏
     *
     * @param context Context
     * @return boolean
     */
    public static boolean getScreenIsLand(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels > dm.heightPixels;
    }

    /**
     * 获取导航栏高度
     *
     * @param context Context
     * @return 导航栏高度
     */
    public static int getDaoHangHeight(Context context) {
        int result = 0;
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else
            return 0;
    }

    /**
     * 改变背景明暗
     *
     * @param activity Activity
     * @param shade    boolean
     */
    public static void changeWindowAlpha(Activity activity, boolean shade) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = shade ? 0.4f : 1f;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

}
