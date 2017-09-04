package com.polar.browser.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.i.IVideoControl;
import com.polar.browser.i.IVideoPlay;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;

public class VideoManager {

    private static VideoManager mInstance;

    private boolean mIsVideoPlayerRunning;

    /**
     * 是否可以播放，当用户是非wifi情况下播放or在wifi下切换到了非wifi，提示弹框，让用户选择是否播放
     **/
    private boolean mCanPlay = false;

    /**
     * 显示网络状况改变对话框
     **/
    private CommonDialog mNetWorkChangedDialog;

    private int mStopPosition;
    private VideoItem mCurrentVideoItem;
    private IVideoPlay mIVideoPlay;
    private IVideoControl mIVideoControl;

    private VideoManager() {
    }

    public static VideoManager getInstance() {
        if (mInstance == null) {
            mInstance = new VideoManager();
        }
        return mInstance;
    }

    /**
     * 播放器是否正在运行
     *
     * @return
     */
    public boolean isVideoPlayerRunning() {
        return mIsVideoPlayerRunning;
    }

    /**
     * 改变播放器状态标识
     *
     * @param isRunning
     */
    public void setVideoPlayerRunning(boolean isRunning) {
        this.mIsVideoPlayerRunning = isRunning;
    }

    /**
     * 播放完成
     */
    public void onCompletion() {
        if (mIVideoPlay != null) {
            mIVideoPlay.onNotifyPlayEnd();
        }
    }

    /**
     * 暂停
     */
    public void onPause(Context c) {
        if (mIVideoControl != null) {
            mIVideoControl.onPause();
        }
        RelativeLayout root = (RelativeLayout) ((BrowserActivity) c)
                .findViewById(R.id.root);
        if (root != null) {
            View video = root.findViewById(R.id.video_root);
            CustomVideoView videoView = null;
            if (video != null) {
                videoView = (CustomVideoView) video.getParent();
                mStopPosition = videoView.getCurrentPosition();
            }
        }
    }

    public void onResume(Context c) {
        RelativeLayout root = (RelativeLayout) ((BrowserActivity) c)
                .findViewById(R.id.root);
        View video = root.findViewById(R.id.video_root);
        CustomVideoView videoView = null;
        if (video != null) {
            videoView = (CustomVideoView) video.getParent();
            if (videoView.isShown()) {
                SysUtils.setFullScreen((Activity) c, true);
                ((Activity) c)
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                videoView.resumePosition(mStopPosition);
            }
        }
        SimpleLog.e("", "-VideoManager------->>>>>-------------onResume()");
    }

    /**
     * 注册播放监听
     *
     * @param videoPlay
     */
    public void registerPlayDelegate(IVideoPlay videoPlay) {
        this.mIVideoPlay = videoPlay;
    }

    /**
     * 反注册广告监听
     */
    public void unRegisterPlayDelegate() {
        this.mIVideoPlay = null;
    }

    /**
     * 注册播放监听
     *
     * @param videoControl
     */
    public void registerPlayControl(IVideoControl videoControl) {
        this.mIVideoControl = videoControl;
    }

    /***
     * 反注册播放监听
     */
    public void unregisterPlayControl(){
        this.mIVideoControl=null;
    }

    /**
     * 视频播放时点击物理返回键
     */
    public void onBackPressed() {
        if (mIVideoControl != null) {
            mIVideoControl.onBackPressed();
        }
    }

    /**
     * 与上次播放的视频url是否相同。
     *
     * @param newVideoUrl
     * @return
     */
    public boolean sameWithCurrentUrl(String newVideoUrl) {
        if (mCurrentVideoItem == null) {
            return false;
        }
        if (TextUtils.isEmpty(mCurrentVideoItem.url)) {
            return false;
        } else {
            if (TextUtils.equals(mCurrentVideoItem.url, newVideoUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 播放视频
     *
     * @param videoItem
     * @param c
     */
    public void playVideo(VideoItem videoItem, Context c) {
        mCurrentVideoItem = videoItem;
        playVideo(c);
    }

    public boolean checkShowDialog(Context c) {
        int networkState = NetWorkUtils.getNetworkState(c);
        // 未允许，而且联网了 非wifi环境
        return (!mCanPlay && NetWorkUtils.NETWORN_WIFI != networkState && NetWorkUtils.NETWORN_NONE != networkState);
    }

    private void playVideo(Context c) {
        if (c instanceof BrowserActivity) {
            RelativeLayout root = (RelativeLayout) ((BrowserActivity) c)
                    .findViewById(R.id.root);
            View video = root.findViewById(R.id.video_root);
            CustomVideoView videoView = null;
            if (video == null) {
                videoView = new CustomVideoView(c);
                root.addView(videoView);
                videoView.init();
                videoView.setVisibility(View.GONE);
            } else {
                videoView = (CustomVideoView) video.getParent();
                videoView.init();
            }
            if (!videoView.isShown()) {
                // TODO 统计
                Statistics.sendOnceStatistics(GoogleConfigDefine.PLAY_VIDEO, GoogleConfigDefine.PLAY_VIDEO);
            }
            videoView.playVideo(mCurrentVideoItem);
            videoView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 部分机型播放器占用画布，清理之前的播放器。
     *
     * @param c
     */
    public void clearSurface(Context c) {
        if (c instanceof BrowserActivity) {
            RelativeLayout root = (RelativeLayout) ((BrowserActivity) c)
                    .findViewById(R.id.root);
            View video = root.findViewById(R.id.video_root);
            CustomVideoView videoView = null;
            if (video != null) {
                try {
                    videoView = (CustomVideoView) video.getParent();
                    root.removeView(videoView);
                    videoView.setVisibility(View.GONE);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    /**
     * 是否可以播放，当用户是非wifi情况下播放or在wifi下切换到了非wifi，提示弹框，让用户选择是否播放
     *
     * @return
     */
    public boolean canPlay() {
        return mCanPlay;
    }

    /**
     * 显示对话框: 取消播放or继续播放
     */
    public void showNetWorkChangedDialog(final Context context,
                                         final IPlay iPlay) {
        if (mNetWorkChangedDialog == null || !mNetWorkChangedDialog.isShowing()) {
            // 弹提示框
            mNetWorkChangedDialog = new CommonDialog(context,
                    context.getString(R.string.tips),
                    context.getString(R.string.play_network_gprs_tip));
            mNetWorkChangedDialog.setContentTxt(R.string.play_network_gprs_tip);
            mNetWorkChangedDialog.setBtnCancel(
                    context.getString(R.string.play_cancel),
                    new android.view.View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mNetWorkChangedDialog.dismiss();
                            if (mIVideoControl != null) {
                                mIVideoControl.onExit();
                            }
                        }
                    });
            mNetWorkChangedDialog.setBtnOk(
                    context.getString(R.string.play_continue),
                    new android.view.View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ThreadManager.postTaskToUIHandler(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    mNetWorkChangedDialog.dismiss();
                                    mCanPlay = true;
                                    if (iPlay != null) {
                                        iPlay.play();
                                    } else {
                                        playVideo(context);
                                    }
                                }
                            });
                        }
                    });
            mNetWorkChangedDialog.show();
        }
    }

    /**
     * 隐藏
     */
    public void hideNetWorkChangedDialog() {
        if (mNetWorkChangedDialog != null && mNetWorkChangedDialog.isShowing()) {
            mNetWorkChangedDialog.dismiss();
        }
    }

    public void onDestory() {
        mCurrentVideoItem = null;
        if (mIVideoPlay != null) {
            mIVideoPlay.onNotifyVideoPlayerDestroy();
        }
    }
}
