package com.polar.browser.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by yd_lzk on 2016/12/19.
 */

public class SoftInputMethodUtils {

    /**
     * 隐藏软键盘
     * @param context
     * @param etName
     */
    public static void hideSoftInputFromWindow(Context context, EditText etName){
        etName.setFocusable(true);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
    }
}
