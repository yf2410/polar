package com.polar.browser.video.share;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonBaseDialog;
import com.polar.browser.statistics.Statistics;

/**
 * Created by yd_lp on 2017/3/13.
 */

public class VideoShareFSDialog extends CommonBaseDialog implements View.OnClickListener {

    private TextView mFacebookShare;
    private TextView mTwitterShare;
    private TextView mWhatsappShare;
    private TextView mEmailShare;
    private TextView mShortmessageShare;
    private TextView mSystemShare;
    private TextView mCopylinkShare;
    private String mShareTitle;
    private String mShareUrl;
    private ShareContent shareContent;
    private Context mContext;

    public VideoShareFSDialog(Context context, String url) {
        super(context, R.style.common_dialog);
        mContext = context;
        getWindow().setWindowAnimations(R.style.dialog_anim_show_from_right);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.dialog_video_fs_share_layout);
        initViews();
        initListeners();
        initData(url);
    }

    private void initData(String url) {
        this.mShareTitle = getContext().getString(R.string.share);
        String prefix = getContext().getString(R.string.share_video_prefix);
        this.mShareUrl = url;
        shareContent = new ShareContent(mShareTitle, prefix +" "+ mShareUrl);
        shareContent.setShareUrl(mShareUrl); // copy link
        shareContent.setType(ShareContent.TYPE_SHARE_TEXT);
    }

    private void initListeners() {
        mFacebookShare.setOnClickListener(this);
        mTwitterShare.setOnClickListener(this);
        mWhatsappShare.setOnClickListener(this);
        mEmailShare.setOnClickListener(this);
        mShortmessageShare.setOnClickListener(this);
        mSystemShare.setOnClickListener(this);
        mCopylinkShare.setOnClickListener(this);
    }

    private void initViews() {
        mFacebookShare = (TextView) findViewById(R.id.video_share_facebook);
        mTwitterShare = (TextView) findViewById(R.id.video_share_twitter);
        mWhatsappShare = (TextView) findViewById(R.id.video_share_whatsapp);
        mEmailShare = (TextView) findViewById(R.id.video_share_email);
        mShortmessageShare = (TextView) findViewById(R.id.video_share_shortmessage);
        mSystemShare = (TextView) findViewById(R.id.video_share_system);
        mCopylinkShare = (TextView) findViewById(R.id.video_share_copy_link);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.RIGHT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_share_facebook:
                hideDialog();
                ShareUtils.share2Facebook(mContext, shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_FACEBOOK);
                break;
            case R.id.video_share_twitter:
                hideDialog();
                ShareUtils.share2Twitter(mContext,shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_TWITTER);
                break;
            case R.id.video_share_whatsapp:
                hideDialog();
                ShareUtils.share2Whatsapp(mContext,shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_WHATSAPP);
                break;
            case R.id.video_share_email:
                hideDialog();
                ShareUtils.share2Mail(mContext,shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_EMAIL);
                break;
            case R.id.video_share_shortmessage:
                hideDialog();
                ShareUtils.share2Msg(mContext,shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_MESSAGE);
                break;
            case R.id.video_share_system:
                hideDialog();
                ShareUtils.onSystemShare(mContext,shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_SYSTEM);
                break;
            case R.id.video_share_copy_link:
                hideDialog();
                ShareUtils.cpLink(mContext,shareContent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.VIDEO_PLAY,
                        GoogleConfigDefine.VIDEO_PLAY_SHARE, GoogleConfigDefine.VIDEO_SHARE_COPYLINK);
                break;
            default:
                break;
        }
    }

    private void hideDialog() {
        dismiss();
    }
}
