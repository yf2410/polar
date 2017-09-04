package com.polar.browser.view;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.ISuspensionWindow;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.UrlUtils;


/**
 * Created by duan on 2017/3/15.
 */

public class SuspenWindowController implements View.OnClickListener, ISuspensionWindow {
    private ImageView mBtnSuspensionWindow;
    // Activity
    private Activity mActivity;
    private View mSuspensionWindow;
    private boolean isFirstShow = true;
    private boolean isPlaying = false;
    private boolean isHit = false;//默认提示条是没有被点击过的
    public SuspenWindowController(Activity activity, ImageView suspensionWindowBtn , View suspensionWindow){

        this.mActivity = activity;
        this.mBtnSuspensionWindow = suspensionWindowBtn;
        this.mSuspensionWindow = suspensionWindow;
        initView();
        initListener();
    }

    private Runnable mShowBtnTask = new Runnable() {
        @Override
        public void run() {
            if (mBtnSuspensionWindow != null) {
                String url;
                if (TabViewManager.getInstance() == null || TabViewManager.getInstance().getCurrentTabView() == null) {
                    return;
                }
                url = TabViewManager.getInstance().getCurrentTabView().getUrl();
                if (UrlUtils.matchInstagramUrl(url)) {
                    if (!ConfigManager.getInstance().getAlbumAvailable()) {
                        return;
                    }
                    mBtnSuspensionWindow.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    /**
     * 显示浮标提示条
     */
    @Override
    public void show() {
        if (isImageBrowseBtnShown()) return;
        String url;
        if (TabViewManager.getInstance() == null || TabViewManager.getInstance().getCurrentTabView() == null) {
            return;
        }
        url = TabViewManager.getInstance().getCurrentTabView().getUrl();
        if (UrlUtils.matchInstagramUrl(url)) {
            if (!ConfigManager.getInstance().getAlbumAvailable()) {
                return;
            }
            if (isFirstShow && ConfigManager.getInstance().isShowSusWin()) {
                ConfigManager.getInstance().setSusWinShow(false);
                isPlaying = true;
                mSuspensionWindow.setVisibility(View.VISIBLE);
                startTime();
            } else {
                if (!isPlaying) {
                    ThreadManager.getUIHandler().removeCallbacks(mShowBtnTask);
                    ThreadManager.postDelayedTaskToUIHandler(mShowBtnTask, 2000);
                }
            }
        }
    }

    /**
     * 隐藏看图模式按钮
     */
    @Override
    public void hide() {
        ThreadManager.getUIHandler().removeCallbacks(mShowBtnTask);
        if (mBtnSuspensionWindow.isShown()) {
            mBtnSuspensionWindow.setVisibility(View.GONE);
        }
    }

    /**
     * 看图模式按钮是否显示
     * @return
     */
    public boolean isImageBrowseBtnShown() {
        if (isPlaying) return true;
        if (mBtnSuspensionWindow.isShown()) return true;
        return false;
    }

    private void startTime() {
        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                displayAnimation();
            }
        } , 4000);
    }


    private void displayAnimation() {
        if(isHit){
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.suspension_window_out);
        mSuspensionWindow.startAnimation(animation);
        mBtnSuspensionWindow.setVisibility(View.VISIBLE);
        Animation tralateAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.suspension_window_btn_in);
        mBtnSuspensionWindow.startAnimation(tralateAnimation);
        isFirstShow = false;
        isPlaying = false;
    }


    private void initListener() {
        mActivity.findViewById(R.id.tv_only_image).setOnClickListener(this);
        mBtnSuspensionWindow.setOnClickListener(this);
    }

    private void initView() {
        //TextView start_open = (TextView) mActivity.findViewById(R.id.start_open);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.suspension_window_btn:
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.PAGE_OPERATION, GoogleConfigDefine.FLOAT_ICON);
//                Intent intent = new Intent(mActivity, ImageBrowseActivity.class);
//                mActivity.startActivity(intent);
                JavaScriptManager.injectAlbumInsJs(TabViewManager.getInstance().getCurrentTabView().getContentView().getWebView());
                break;
            case R.id.tv_only_image:
                ((TextView)mActivity.findViewById(R.id.tv_only_image)).setClickable(false);
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.PAGE_OPERATION, GoogleConfigDefine.FLOAT_ICON_BTN);
                displayAnimation();
                isHit = true;
                break;
            default:
                break;
        }

    }


}
