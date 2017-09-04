package com.polar.browser.setting;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.view.RangeBar;

/**
 * Created by duan on 16/9/9.
 */
public class FontSizeSettingDialog extends CommonBaseDialog{

    protected Context mContext;

    private RangeBar mRangeBar;
    private RangeAdapter mAdapter;

    public FontSizeSettingDialog(Context context) {
        super(context, R.style.common_dialog);
        mContext = context;
        initCenterView();
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialog_anim_show_from_bottom);
    }

    protected void initCenterView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_menu_font_size_setting, null);
        mRangeBar = (RangeBar) view.findViewById(R.id.rangeBar);
        Resources resources = mContext.getResources();
        mAdapter = new RangeAdapter(resources, new int[]{
                R.drawable.font_size_thumb,
                R.drawable.font_size_thumb,
                R.drawable.font_size_thumb,
                R.drawable.font_size_thumb});

        mAdapter.setTextColor(new int[]{
                mContext.getResources().getColor(R.color.sys_news),
                mContext.getResources().getColor(R.color.sys_news),
                mContext.getResources().getColor(R.color.sys_news),
                mContext.getResources().getColor(R.color.sys_news),
        });

        mAdapter.setContent(new String[]{
                mContext.getString(R.string.setting_font_size_min),
                mContext.getString(R.string.setting_font_size_mid),
                mContext.getString(R.string.setting_font_size_big),
                mContext.getString(R.string.setting_font_size_large)
        });

        mRangeBar.setAdapter(mAdapter);
        mRangeBar.setPosition(ConfigManager.getInstance().getFontSize() + 1);
        mRangeBar.setOnGbSlideBarListener(new RangeBar.RangeBarListener() {
            @Override
            public void onPositionSelected(int position) {
                ConfigManager.getInstance().setFontSize(position - 1);
            }
        });

        view.findViewById(R.id.rl_menu_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(view);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void show() {
        super.show();
        WindowManager windowManager = ((Activity)mContext).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int)display.getWidth(); //设置宽度
        getWindow().setAttributes(lp);
    }


}
