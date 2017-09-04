package com.polar.browser.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.utils.DensityUtil;

/**
 * Created by lzk-pc on 2017/3/29.
 * 长按该控件，显示长按添加动画，在动画未执行就松手，则取消添加操作
 */

public class LongClickAddRelativeLayout extends RelativeLayout implements View.OnLongClickListener,NewTabAnimation.OnLoadListener{

    private NewTabAnimation newTabAnimation;
    private PopupWindow popupWindow;
    private NewTabAnimation.OnLoadListener mLoadListener;
    private boolean isLongClick = false;

    public LongClickAddRelativeLayout(Context context) {
        super(context);
        init(context,null);
    }

    public LongClickAddRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        this.setOnLongClickListener(this);  //长按触发动画
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                if(isLongClick && newTabAnimation != null){
                    isLongClick = false;
                    newTabAnimation.onActionUpCheckProgress();  //若用户抬起手指，取消加载动画
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onLongClick(View v) {
        isLongClick = true;
        initPopupWindow();
        return true;
    }

    private void initPopupWindow(){
        popupWindow = new PopupWindow(this);
        int popupWidthHeight = (int)getResources().getDimension(R.dimen.new_tab_anim_width_height);
        popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        View rootView = LayoutInflater.from(getContext().getApplicationContext()).inflate(R.layout.popup_add_new_tab, null);
        newTabAnimation = (NewTabAnimation)rootView.findViewById(R.id.new_tab_anim);
        newTabAnimation.setOnLoadAnimationListener(this);
        popupWindow.setContentView(rootView);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        int marginBottom = DensityUtil.dip2px(getContext(),60);  //PopupWindow与参照控件的中心点相距60dp
        int x = location[0] - (popupWidthHeight - this.getWidth()) / 2;
        int y = location[1] - marginBottom - popupWidthHeight/2;
        popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
    }


    public void setOnLoadAnimationListener(NewTabAnimation.OnLoadListener loadListener){
        this.mLoadListener = loadListener;
    }

    @Override
    public void onLoadFinish() {
        //关闭PopupWindow
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        if (mLoadListener != null){
            mLoadListener.onLoadFinish();
        }
    }

    @Override
    public void onLoadCancel() {
        //关闭PopupWindow
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        if(mLoadListener != null){
            mLoadListener.onLoadCancel();
        }
    }

    public void destroy(){
        if(popupWindow != null){
            if(popupWindow.isShowing()){ popupWindow.dismiss();}
            popupWindow = null;
        }
        if(newTabAnimation != null){
            newTabAnimation = null;
        }
    }

}
