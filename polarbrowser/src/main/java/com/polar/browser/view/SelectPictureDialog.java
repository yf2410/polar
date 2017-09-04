package com.polar.browser.view;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by lzk-pc on 2017/4/6.
 */

public class SelectPictureDialog extends CommonBaseDialog implements View.OnClickListener{

    private OnItemClickListener onItemClickListener;

    public SelectPictureDialog(Context context) {
        super(context, R.style.common_dialog);
        initView();
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialog_anim_show_from_bottom);

        WindowManager windowManager = ((Activity)context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth(); //设置宽度
        getWindow().setAttributes(lp);
    }

    private void initView(){
        View view = View.inflate(getContext(), R.layout.dialog_select_photo, null);
        setContentView(view);
        view.findViewById(R.id.tv_select_gallery).setOnClickListener(this);
        view.findViewById(R.id.tv_select_camera).setOnClickListener(this);
        view.findViewById(R.id.tv_select_cancel).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_select_gallery:
                if(onItemClickListener != null) onItemClickListener.onAlbum();
                break;
            case R.id.tv_select_camera:
                if(onItemClickListener != null) onItemClickListener.onCamera();
                break;
            case R.id.tv_select_cancel:
                if(onItemClickListener != null) onItemClickListener.onCancel();
                break;
        }
    }

    public interface OnItemClickListener{
        void onAlbum();
        void onCamera();
        void onCancel();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
