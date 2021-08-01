package com.xing.mita.movie.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * @author Mita
 * @Date 2018/8/28
 */
public class EditTextUtil {

    /**
     * 获焦
     *
     * @param context  Context
     * @param editText EditText
     */
    public static void searchPoint(Context context, EditText editText) {
        InputMethodManager mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.findFocus();
        if (mInputMethodManager != null) {
            mInputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * 失焦
     *
     * @param context  Context
     * @param editText EditText
     */
    public static void losePoint(Context context, EditText editText) {
        InputMethodManager mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        editText.setFocusable(false);
        if (mInputMethodManager != null && mInputMethodManager.isActive()) {
            mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

}
