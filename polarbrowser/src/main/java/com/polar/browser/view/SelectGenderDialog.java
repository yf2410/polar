package com.polar.browser.view;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by yangfan on 2017/5/10.
 */

public class SelectGenderDialog extends CommonBaseDialog implements View.OnClickListener{
    private OnItemClickListener onItemClickListener;

    public SelectGenderDialog(Context context) {
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

    private void initView() {
        View view = View.inflate(getContext(), R.layout.dialog_select_gender, null);
        setContentView(view);
        view.findViewById(R.id.tv_select_male).setOnClickListener(this);
        view.findViewById(R.id.tv_select_female).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_select_male:
                if(onItemClickListener != null) onItemClickListener.onMale();
                break;
            case R.id.tv_select_female:
                if(onItemClickListener != null) onItemClickListener.onFemale();
                break;
            default:
                break;
        }
    }

    public interface OnItemClickListener{
        void onMale();
        void onFemale();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
