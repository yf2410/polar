package com.polar.browser.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.ViewUtils;

public class H5PlayerFullScreenMgr {

    private static H5PlayerFullScreenMgr mInstance;

    private Context mContext;
    private boolean mIsVideoFullScreen;
    private View mWebVideoViewContainer;
    private RelativeLayout mFullScreenViewContainer;
    private WebChromeClient.CustomViewCallback mCallBack;
    private int mCurrentScreenOrientation;
    private View mViewLoading;
    private Bitmap mBitmap;
    private H5FullscreenVideoView mVideoContainer;

    public static H5PlayerFullScreenMgr getInstance() {
        if (mInstance == null) {
            mInstance = new H5PlayerFullScreenMgr();
        }
        return mInstance;
    }

    public void registerInstance(Context c) {
        if (mContext == null) {
            mContext = c;
        }
        if (mFullScreenViewContainer == null) {
            Activity ac = (Activity) c;
            mViewLoading = ac.getLayoutInflater().inflate(R.layout.view_loading_video, null);
            mFullScreenViewContainer = (RelativeLayout) ac.findViewById(R.id.video_Layout);
            mVideoContainer = (H5FullscreenVideoView) mFullScreenViewContainer.findViewById(R.id.video_container);
            mFullScreenViewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            if (mBitmap == null) {
                mBitmap = ViewUtils.getBitmapFromResources(JuziApp.getAppContext(),
                        R.drawable.video_poster);
            }
            //mBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        }
    }

    public Bitmap getDefaultVideoPoster() {
        return mBitmap;
    }

    public boolean isVideoFullScreen() {
        return mIsVideoFullScreen;
    }

    public void onBackPressed() {
        onHide();
    }

    public View getVideoLoadingProgressView() {
        if (mViewLoading != null) {
            mViewLoading.setVisibility(View.VISIBLE);
            return mViewLoading;
        }
        return null;
    }

    public void onShow(View view, CustomViewCallback callback) {
        if (mWebVideoViewContainer != null) {
            callback.onCustomViewHidden();
            return;
        }
        if (mFullScreenViewContainer != null) {
            // can set view event here.
            mFullScreenViewContainer.setVisibility(View.VISIBLE);
            mVideoContainer.addVideoView(view);
            mWebVideoViewContainer = view;
            mCallBack = callback;
            Activity ac = (Activity) mContext;
            if (ac != null && ac instanceof Activity) {
                mCurrentScreenOrientation = ac.getRequestedOrientation();
                if (mCurrentScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    ac.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
            // clear custom video player
            VideoManager.getInstance().clearSurface(mContext);
            // set full screen
            SysUtils.setFullScreen((Activity) mContext, true);
            //  < 4.2 set surface view ZOrder
            /*if (view instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) view;
                if (frameLayout != null) {
                    View viewSurFace = (View) frameLayout.getChildAt(0);
                    if (viewSurFace instanceof SurfaceView) {
                        ((SurfaceView) viewSurFace).setZOrderOnTop(true);
                    }
                }
            }*/
            mIsVideoFullScreen = true;
        }
    }

    public void onHide() {
        if (mFullScreenViewContainer != null) {
            if (mWebVideoViewContainer == null) {
                return;
            }
            mVideoContainer.removeVideoView(mWebVideoViewContainer);
            mFullScreenViewContainer.setVisibility(View.INVISIBLE);
            mWebVideoViewContainer = null;
            if (mCallBack != null) {//解决部分机型退出视频全屏播放，出现白屏现象
                mCallBack.onCustomViewHidden();
            }
            Activity ac = (Activity) mContext;
            if (ac != null && ac instanceof Activity && ac.getRequestedOrientation() != mCurrentScreenOrientation) {
                ac.setRequestedOrientation(mCurrentScreenOrientation);
            }
            SysUtils.setFullScreen((Activity) mContext, false);
        }
        mIsVideoFullScreen = false;
        mCallBack = null;
    }

    public void onPause() {
        onHide();
    }

    public void unRegisterInstance() {
        mContext = null;
        mInstance = null;
    }
}
