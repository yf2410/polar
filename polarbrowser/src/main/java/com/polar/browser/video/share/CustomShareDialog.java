package com.polar.browser.video.share;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.LoadingDialog;
import com.polar.browser.cropedit.CropEditActivity;
import com.polar.browser.cropedit.CropStorageUtil;
import com.polar.browser.i.IDownloadCallBack;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.BitmapUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.GlideUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.common.Constants;

import java.io.File;

/**
 * Created by liuzikuo on 17-3-13.
 * 自定义分享弹框
 */
public class CustomShareDialog extends CommonBottomDialog implements View.OnClickListener {

    private static final String TAG = CustomShareDialog.class.getSimpleName();
    private Activity mActivity;

    //分享的渠道
    public static final int CHANNEL_VIDEO_SHARE = 1;
    public static final int CHANNEL_IMAGE_SHARE = 2;
    public static final int CHANNEL_INSTAGRAM_SHARE = 3;   //instagram share
    public static final int CHANNEL_AD_BLOCK_SHARE = 4;   // 广告拦截分享


    private Animation mOutAnim;
    private String mShareUrl;

    private String mShareTitle;
    private ShareContent shareContent;

    private TextView mBtnFaceBook;
    private TextView mBtnTwitter;
    private TextView mBtnWhatsapp;
    private TextView mBtnMail;
    private TextView mBtnMessage;
    private TextView mBtnSystemShare;
    private TextView mBtnCopyLink;
    private LinearLayout mShareRootLayout;
    //编辑图片
    private LinearLayout mEditImgLL;
    //加载对话框
    private LoadingDialog loadingDialog;
    private int mShareChannel;
    private int previousWidth;

    public CustomShareDialog(Activity activity, String shareUrl, int shareChannel) {
        super(activity);
        initData(activity,null,shareUrl,shareChannel);
        init();
    }

    public CustomShareDialog(Activity activity, String prefix, String shareUrl, int shareChannel){
        super(activity);
        initData(activity,prefix,shareUrl,shareChannel);
        init();
    }

    private void initData(Activity activity,String prefix, String shareUrl, int shareChannel){

        this.mActivity = activity;
        this.mShareTitle = getContext().getString(R.string.share);
        this.mShareUrl = shareUrl;
        this.mShareChannel = shareChannel;

        shareContent = new ShareContent(mShareTitle);
        shareContent.setShareUrl(mShareUrl); //必须设置url

        if (CHANNEL_IMAGE_SHARE == shareChannel || CHANNEL_INSTAGRAM_SHARE == shareChannel) {  //图片分享
            shareContent.setType(ShareContent.TYPE_SHARE_PICTURE);
            shareContent.setContent(shareUrl);  //当分享图片失败时将分享图片url
        } else if(CHANNEL_VIDEO_SHARE == shareChannel){  //视频分享
            if(prefix == null){
                prefix = getContext().getString(R.string.share_video_prefix)+" ";
            }
            shareContent.setContent(prefix + shareUrl);
            shareContent.setType(ShareContent.TYPE_SHARE_TEXT);
        } else if(CHANNEL_AD_BLOCK_SHARE == shareChannel){  //广告拦截分享
            if(prefix == null){
                prefix = "";
            }
            shareContent.setContent(prefix + shareUrl);
            shareContent.setType(ShareContent.TYPE_SHARE_MIX);
            shareContent.setImgUri(Uri.fromFile(new File(BitmapUtils.saveResourceToFile(getContext(),R.drawable.adblock_share_img,false))));
        }
    }




    public void init() {

        setContentView(R.layout.view_custom_share);

        mEditImgLL = (LinearLayout) findViewById(R.id.share_ll_edit_img);
        findViewById(R.id.share_edit_Graffiti).setOnClickListener(this);
        resetEditImgLayout(shareContent.getType());

        mShareRootLayout = (LinearLayout) findViewById(R.id.content_layout);
        mBtnFaceBook = (TextView) findViewById(R.id.share_facebook);
        mBtnFaceBook.setOnClickListener(this);
        mBtnTwitter = (TextView) findViewById(R.id.share_twitter);
        mBtnTwitter.setOnClickListener(this);
        mBtnWhatsapp = (TextView) findViewById(R.id.share_whatsapp);
        mBtnWhatsapp.setOnClickListener(this);
        mBtnMail = (TextView) findViewById(R.id.share_email);
        mBtnMail.setOnClickListener(this);
        mBtnMessage = (TextView) findViewById(R.id.share_message);
        mBtnMessage.setOnClickListener(this);
        mBtnCopyLink = (TextView) findViewById(R.id.copy_link);
        mBtnCopyLink.setOnClickListener(this);
        mBtnSystemShare = (TextView) findViewById(R.id.vc_share_system);
        mBtnSystemShare.setOnClickListener(this);

        onConfigurationChanged();
        //支持横竖屏布局切换
        findViewById(R.id.rl_root).getViewTreeObserver().addOnGlobalLayoutListener(mOGLListener);

    }

    private void onConfigurationChanged() {
        int curWidth = getWidthByOrientation();
        if(curWidth == previousWidth) return;
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        previousWidth = curWidth;
        lp.width = curWidth;
        SimpleLog.d(TAG,"onConfigurationChanged lp.width = "+lp.width);
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setAttributes(lp);
    }

    private int getWidthByOrientation(){
        int size;
        if(isLandscape()){
            size = Math.max(AppEnv.SCREEN_WIDTH,AppEnv.SCREEN_HEIGHT);
        }else{
            size = Math.min(AppEnv.SCREEN_WIDTH,AppEnv.SCREEN_HEIGHT);
        }
        return size;
    }


    private boolean isLandscape(){
        Configuration cf= getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = cf.orientation ; //获取屏幕方向
        if(ori == Configuration.ORIENTATION_LANDSCAPE){
            return true;
        }
        return false;
    }

    private ViewTreeObserver.OnGlobalLayoutListener mOGLListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            onConfigurationChanged();
        }
    };

    /**
     * 设置是否显示编辑图片功能
     */
    private void resetEditImgLayout(int shareType) {
        if (ShareContent.TYPE_SHARE_PICTURE == shareType) {
            mEditImgLL.setVisibility(View.VISIBLE);
        } else {
            mEditImgLL.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(final View v) {
        hideDialog();

        //编辑图片
        if(v.getId() == R.id.share_edit_Graffiti){
            editImg();
            return;
        }

        if (ShareContent.TYPE_SHARE_PICTURE == shareContent.getType() && v.getId() != R.id.copy_link ) {  //copy link 不必下载图片

            if (shareContent.getShareUrl() != null) {
                //下载图片
                showLoadingDialog();
                GlideUtils.getBitmapByLoadUrl(mActivity, shareContent.getShareUrl(), 0, new IDownloadCallBack<Bitmap>() {
                    @Override
                    public void onDownloadSuccess(final Bitmap source) {
                        //IO Thread save Bitmap
                        ThreadManager.postTaskToIOHandler(new Runnable() {
                            @Override
                            public void run() {
                                FileUtils.saveBitmapToFile(source,VCStoragerManager.getInstance().getImageDirPath(),Constants.JS_IMG_NAME);
                                File file = new File(VCStoragerManager.getInstance().getImageDirPath(),Constants.JS_IMG_NAME);
                                if(file.exists()){
                                    shareContent.setImgUri(Uri.fromFile(new File(file.getAbsolutePath())));
                                }else{  //图片下载失败
                                    shareContent.setImgUri(null); //url = null，将分享图片url
                                }
                                //UI
                                ThreadManager.postTaskToUIHandler(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissLoadingDialog();
                                        shareTo(v);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onDownloadFailed(String error) {
                        dismissLoadingDialog();
                        shareContent.setImgUri(null); //url = null，将分享图片url
                        shareTo(v);
                    }
                });
            }else{
                CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
            }
        } else {  //分享文本
            shareTo(v);
        }

    }

    private void shareTo(View v) {
        switch (v.getId()) {

            case R.id.share_facebook://face book
                ShareUtils.share2Facebook(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {  //长按菜单
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_FACEBOOK);
                } else if (isShareVideo()) {  //视频
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_FACEBOOK);
                } else if(isInstagramShareImg()){  //instagram网站
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_FACEBOOK);
                }
                break;

            case R.id.share_twitter://twitter
                ShareUtils.share2Twitter(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_TWITTER);
                } else if (isShareVideo()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_TWITTER);
                }else if(isInstagramShareImg()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_TWITTER);
                }
                break;

            case R.id.share_whatsapp://whatsapp
                ShareUtils.share2Whatsapp(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_WHATSAPP);
                } else if (isShareVideo()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_WHATSAPP);
                }else if(isInstagramShareImg()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_WHATSAPP);
                }
                break;

            case R.id.share_message://send msg
                ShareUtils.share2Msg(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_MESSAGE);
                } else if (isShareVideo()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_MESSAGE);
                }else if(isInstagramShareImg()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_MESSAGE);
                }
                break;

            case R.id.share_email://send mail
                ShareUtils.share2Mail(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_EMAIL);
                } else if (isShareVideo()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_EMAIL);
                }else if(isInstagramShareImg()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_EMAIL);
                }
                break;

            case R.id.vc_share_system://call system share
                ShareUtils.onSystemShare(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_SYSTEM);
                } else if (isShareVideo()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_SYSTEM);
                }else if(isInstagramShareImg()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_SYSTEM);
                }
                break;

            case R.id.copy_link://copy link
                ShareUtils.cpLink(mActivity, shareContent);
                //statistics
                if (isLongClickShareImg()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                            GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.VIDEO_SHARE_COPYLINK);
                } else if (isShareVideo()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                            GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_COPYLINK);
                }else if(isInstagramShareImg()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                            GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.VIDEO_SHARE_COPYLINK);
                }
                break;
            default:
                break;
        }
    }

    private void hideDialog() {
        this.dismiss();
    }

    /**
     * 判读是否为分享图片
     *
     * @return
     */
    private boolean isLongClickShareImg() {
        if (CHANNEL_IMAGE_SHARE == mShareChannel) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否为视频分享
     * */
    private boolean isShareVideo() {
        if (CHANNEL_VIDEO_SHARE == mShareChannel) {
            return true;
        }
        return false;
    }

    /**
     * 是否为instagram分享
     * */
    private boolean isInstagramShareImg() {
        if (CHANNEL_INSTAGRAM_SHARE == mShareChannel) {
            return true;
        }
        return false;
    }

    private boolean isNeedShow = true;  //判断是否需要显示加载框
    /**
     * 编辑图片
     */
    private void editImg(){

        showLoadingDialog();

        GlideUtils.getBitmapByLoadUrl(mActivity.getApplicationContext(), shareContent.getShareUrl(),0, new IDownloadCallBack<Bitmap>() {
            @Override
            public void onDownloadSuccess(Bitmap bitmap) {
                SimpleLog.d(TAG,"onDownloadSuccess --- ");
                dismissLoadingDialog();
                if(bitmap != null){
                    CropStorageUtil.recycle();
                    CropStorageUtil.setBitmap(bitmap);
                    Intent intent = new Intent(mActivity, CropEditActivity.class);
                    mActivity.startActivity(intent);
                }else{
                    CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
                }
            }

            @Override
            public void onDownloadFailed(String error) {
                SimpleLog.d(TAG,"onDownloadFailed --- ");
                dismissLoadingDialog();
                CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
            }
        });

        if(isLongClickShareImg()){
            //统计编辑图片次数
            Statistics.sendOnceStatistics(GoogleConfigDefine.LONGTOUCHCLICKMENU,
                    GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE, GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_EDIT_IMAGE);
        }else if(isInstagramShareImg()){
            Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE,
                    GoogleConfigDefine.WEBPAGE_IMAGE_SHARE,GoogleConfigDefine.WEBPAGE_IMAGE_SHARE_EDIT);
        }

    }

    private void showLoadingDialog(){
        mEditImgLL.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(loadingDialog == null){
                    loadingDialog = new LoadingDialog(mActivity);
                    loadingDialog.setMessage(getContext().getString(R.string.loading));
                }
                if(isNeedShow){
                    loadingDialog.show();  //显示加载图片对话框
                }
            }
        },500);  //延时目的：当下载时间很小时，不显示加载框

    }
    private void dismissLoadingDialog(){
        isNeedShow = false;
        if(loadingDialog != null){
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
