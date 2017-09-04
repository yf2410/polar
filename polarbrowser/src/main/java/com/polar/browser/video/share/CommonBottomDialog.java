package com.polar.browser.video.share;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by duan on 16/10/21.
 */
public class CommonBottomDialog extends CommonBaseDialog {

    private Context mContext;

    public CommonBottomDialog(Context context) {
        super(context, R.style.common_dialog);
        mContext = context;
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialog_anim_show_from_bottom);
    }

    public CommonBottomDialog(Context context, int style) {
        super(context, style);
        mContext = context;
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialog_anim_show_from_bottom);
    }

    @Override
    public void show() {
        super.show();
        WindowManager windowManager = ((Activity)mContext).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int)(display.getWidth()); //设置宽度
        getWindow().setAttributes(lp);
    }

}
