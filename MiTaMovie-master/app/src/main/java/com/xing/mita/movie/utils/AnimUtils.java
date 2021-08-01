package com.xing.mita.movie.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.xing.mita.movie.R;

/**
 * @author Mita
 * @date 2018/10/30
 * @Description
 */
public class AnimUtils {

    /**
     * 加载点击动画
     *
     * @param v View
     */
    public static void loadClickAnim(View v) {
        Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.anim_icon_click);
        v.startAnimation(animation);
    }

}
