package com.polar.browser.video;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.video.share.FbVideoShareUtils;

/**
 * Created by yd_lp on 2017/2/13.
 */

public class H5FullscreenVideoView extends RelativeLayout implements View.OnClickListener {

    private VideoBrightnessView mChangeBright;
    private VideoVoiceView mChangeVoice;
    private ImageView mVideoLock;
    private ImageView mImageBack;
    private boolean mIsLocked;
    private int mWidth;
    private int mHeight;

    private static final int MARGIN_FROM_MIDDLE = 100;
    private AudioManager mAudioManager;
    private RelativeLayout mVideoRoot;
    private float mLastBrightness;
    private View mTopGroup;
    private ImageView mImgShare;
    private ImageView mImgDownload;

    public H5FullscreenVideoView(Context context) {
        super(context);
        init();
    }

    public H5FullscreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflateLayout();
        initViews();
        initListeners();
        initData();
    }

    private void inflateLayout() {
        inflate(getContext(), R.layout.fullscreen_video_layout, this);
    }

    private void initData() {
        mAudioManager = (AudioManager) JuziApp.getInstance().getSystemService(Context.AUDIO_SERVICE);
        if (AppEnv.SCREEN_WIDTH > AppEnv.SCREEN_HEIGHT) {
            mWidth = AppEnv.SCREEN_WIDTH;
            mHeight = AppEnv.SCREEN_HEIGHT;
        } else {
            mWidth = AppEnv.SCREEN_HEIGHT;
            mHeight = AppEnv.SCREEN_WIDTH;
        }
        mLastBrightness = ((Activity) getContext()).getWindow()
                .getAttributes().screenBrightness;
    }

    private void initViews() {
        mVideoRoot = (RelativeLayout) findViewById(R.id.group_video_root);
        mVideoRoot.setClickable(true);
        mChangeBright = (VideoBrightnessView) findViewById(R.id.view_change_bright);
        mChangeVoice = (VideoVoiceView) findViewById(R.id.view_change_voice);
        mVideoLock = (ImageView) findViewById(R.id.view_video_lock);
        mImageBack = (ImageView) findViewById(R.id.image_back);
        mTopGroup = findViewById(R.id.group_top);
        mImgShare = (ImageView) findViewById(R.id.image_share);
        mImgDownload = (ImageView) findViewById(R.id.image_download);

        LayoutParams paramsBright = (LayoutParams) mChangeBright.getLayoutParams();
        LayoutParams paramsVoice = (LayoutParams) mChangeVoice.getLayoutParams();
        int height = AppEnv.SCREEN_HEIGHT > AppEnv.SCREEN_WIDTH ? AppEnv.SCREEN_WIDTH : AppEnv.SCREEN_HEIGHT;
        paramsBright.setMargins(0, height / 2 - DensityUtil.dip2px(JuziApp.getInstance(), MARGIN_FROM_MIDDLE), 0, 0);
        paramsVoice.setMargins(0, height / 2 - DensityUtil.dip2px(JuziApp.getInstance(), MARGIN_FROM_MIDDLE), 0, 0);
        mChangeBright.setLayoutParams(paramsBright);
        mChangeVoice.setLayoutParams(paramsVoice);
    }

    /**
     * 在facebook站内
     */
    private void isInFacebook() {
        if (TabViewManager.getInstance() != null && TabViewManager.getInstance().getCurrentTabView() != null) {
            String url = TabViewManager.getInstance().getCurrentTabView().getUrl();
            if (FbVideoShareUtils.isInFacebook(url)) {
                mImgShare.setVisibility(View.VISIBLE);
                mImgDownload.setVisibility(View.VISIBLE);
            } else {
                mImgShare.setVisibility(View.GONE);
                mImgDownload.setVisibility(View.GONE);
            }
        }
    }

    private void initListeners() {
        mVideoLock.setOnClickListener(this);
        mImageBack.setOnClickListener(this);
        mImgShare.setOnClickListener(this);
        mImgDownload.setOnClickListener(this);
        mVideoRoot.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                if (mIsLocked) {
                    return true;
                }
                return false;
            }
        });
    }

    public void addVideoView(View videoView) {
        if (videoView != null) {
            analyzeVideoSite();
            isInFacebook();
            mVideoRoot.addView(videoView, 0, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (videoView instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) videoView;
                View surfaceView;
                if (frameLayout != null) {
                    int childCount = frameLayout.getChildCount();
                    if (childCount <= 0) {
                        return;
                    } else if (childCount == 1) {
                        surfaceView = frameLayout.getChildAt(0);
                    } else {//huawei p8第二个child是想要的全屏播放控件
                        surfaceView = frameLayout.getChildAt(1);
                    }

                    surfaceView.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            onTouchEvent(event);
                            if (mIsLocked) {
                                return true;
                            }
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_MOVE:
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                }
            }
            setBrightness(0.5f);
        }
    }

    public void removeVideoView(View videoView) {
        if (videoView != null) {
            mVideoRoot.removeView(videoView);
            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    setBrightness(mLastBrightness);
                }
            });
        }
        resetLockState();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 重置锁屏状态
     */
    private void resetLockState() {
        mIsLocked = false;
        mVideoLock.setImageResource(R.drawable.video_unlock);
        mVideoLock.setVisibility(View.VISIBLE);

        mTopGroup.setVisibility(View.VISIBLE);
    }

    private void setBrightness(float f) {
        WindowManager.LayoutParams lp = ((Activity) getContext()).getWindow()
                .getAttributes();
        lp.screenBrightness = f;
        ((Activity) getContext()).getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_video_lock:
                lockVideoScreen();
                break;
            case R.id.image_back:
                exitFullscreen();
                break;
            case R.id.image_share:
                shareVideo();
                break;
            case R.id.image_download:
                downloadVideo();
                break;
            default:
                break;
        }
    }

    private void downloadVideo() {
        if (TabViewManager.getInstance() != null && TabViewManager.getInstance().getCurrentTabView() != null) {
            TabViewManager.getInstance().getCurrentTabView().loadUrl(
                    CommonData.EXEC_JAVASCRIPT + "fullScreenDownload();", Constants.NAVIGATESOURCE_NORMAL);
        }
        Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY, GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_DOWNLOAD);
    }

    private void shareVideo() {
        if (TabViewManager.getInstance() != null && TabViewManager.getInstance().getCurrentTabView() != null) {
            TabViewManager.getInstance().getCurrentTabView().loadUrl(
                    CommonData.EXEC_JAVASCRIPT + "fullScreenShare();", Constants.NAVIGATESOURCE_NORMAL);
        }
        Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY, GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_SHARE);
    }

    private void exitFullscreen() {
        Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_EXIT);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //模拟返回键点击事件
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void showOrHide() {
        if (mVideoLock.isShown()) {
            mVideoLock.setVisibility(View.INVISIBLE);
        } else {
            mVideoLock.setVisibility(View.VISIBLE);
        }

        if (mIsLocked) {
            mTopGroup.setVisibility(View.INVISIBLE);
        } else {
            if (mTopGroup.isShown()) {
                mTopGroup.setVisibility(View.INVISIBLE);
            } else {
                mTopGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    private void lockVideoScreen() {
        mIsLocked = !mIsLocked;
        if (mIsLocked) {
            Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY, GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_LOCK);
            mVideoLock.setImageResource(R.drawable.video_lock);
            CustomToastUtils.getInstance().showTextToast(R.string.video_screen_locked);
        } else {
            Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY, GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_UNLOCK);
            mVideoLock.setImageResource(R.drawable.video_unlock);
            CustomToastUtils.getInstance().showTextToast(R.string.video_screen_unlocked);
        }
    }

    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new VideoGestureListener());

    private class VideoGestureListener extends GestureDetector.SimpleOnGestureListener {
        final double FLING_MIN_DISTANCE = 0.5;
        final double FLING_MIN_VELOCITY = 0.5;

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            showOrHide();
            endGesture();
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mIsLocked) {
                return true;
            }
            boolean isVertical = Math.abs(e1.getY() - e2.getY()) > Math.abs(e1.getX() - e2.getX());
            if (e1.getX() < mWidth / 2) { //亮度调节
                if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE
                        && Math.abs(distanceY) > FLING_MIN_VELOCITY && isVertical) {
                    changeBrightness(distanceY);
                }
                if (e1.getY() - e2.getY() < FLING_MIN_DISTANCE
                        && Math.abs(distanceY) > FLING_MIN_VELOCITY && isVertical) {
                    changeBrightness(distanceY);
                }
            } else { //音量调节
                if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE
                        && Math.abs(distanceY) > FLING_MIN_VELOCITY && isVertical) {
                    changeVolume(distanceY);
                }
                if (e1.getY() - e2.getY() < FLING_MIN_DISTANCE
                        && Math.abs(distanceY) > FLING_MIN_VELOCITY && isVertical) {
                    changeVolume(distanceY);
                }
            }
            return true;
        }
    }

    public void changeBrightness(float brightness) {
        Window window = ((Activity) getContext()).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else {
            if (lp.screenBrightness <= 0) {
                lp.screenBrightness = 0.01f;
            }
        }
        window.setAttributes(lp);
        mChangeVoice.setVisibility(View.GONE);
        mChangeBright.setProgreess((int) (lp.screenBrightness * 100));
        Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_BRIGHTNESS);
    }

    private void changeVolume(float delatY) {
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (delatY > 0) {
            if (current < max) {
                current++;
            }
        } else {
            if (current > 0) {
                current--;
            }
        }

        try {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, 0);
        } catch (Exception e) {
            return;
        }

        int transformatVolume = current * 100 / max;
        mChangeBright.setVisibility(View.GONE);
        mChangeVoice.setProgreess(transformatVolume);
        Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_VOICE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return false;
    }

    private void endGesture() {
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, HIDE_DELAY_MILIS);
    }

    private static final int MSG_HIDE = 0;
    private static final long HIDE_DELAY_MILIS = 2000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE:
                    hideViews();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 隐藏锁屏，返回按钮
     */
    private void hideViews() {
        mVideoLock.setVisibility(View.INVISIBLE);
        mTopGroup.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mIsLocked) {//锁定屏幕时，禁用物理返回键
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                return true;
            }
        } else {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_FULLSCREEN, GoogleConfigDefine.VIDEO_FULLSCREEN_BACK_KEY);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**统计视频播放网站*/
    private void analyzeVideoSite() {
        if (TabViewManager.getInstance() != null && TabViewManager.getInstance().getCurrentTabView() != null) {
            String url = TabViewManager.getInstance().getCurrentTabView().getUrl();
            String host = UrlUtils.getHost(url);
            if (!TextUtils.isEmpty(host)) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SITES, host);
            }
        }
    }
}
