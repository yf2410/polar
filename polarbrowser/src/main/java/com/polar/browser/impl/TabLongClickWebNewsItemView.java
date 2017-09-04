package com.polar.browser.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.cropedit.CropStorageUtil;
import com.polar.browser.download.DownloadHelper;
import com.polar.browser.download.savedpage.SavedPageUtil;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.video.share.CustomShareDialog;

/**
 * Created by sw_01 on 2016/9/30.
 */

public class TabLongClickWebNewsItemView extends PopupWindow implements View.OnClickListener {

    private View mMenuView;

    private LayoutInflater mInflater;

    private String mUrl;
    private String mImgUrl;
    private String mWebUrl;
    private String mTitle;
    private String mWebTitle;



    private Context mContext;

    private String mLinkText;

    public TabLongClickWebNewsItemView(Context c, String imgUrl,String src, String webUrl, String webTitle,String title, Bitmap bitmap) {
        mContext = c;
        mUrl = src;
        mImgUrl = imgUrl;
        mWebTitle = webTitle;
        mWebUrl = webUrl;
        mTitle = title;
        CropStorageUtil.setBitmap(bitmap);
        initView(c);
    }

    @SuppressLint("InflateParams") private void initView(Context c) {
        mInflater = LayoutInflater.from(c);

        mMenuView = mInflater.inflate(R.layout.menu_longclick_web_page, null);

        mMenuView.findViewById(R.id.item_view_img_ll).setOnClickListener(this);
        mMenuView.findViewById(R.id.item_download_img_ll).setOnClickListener(this);
        mMenuView.findViewById(R.id.item_share_img_ll).setOnClickListener(this);

        mMenuView.findViewById(R.id.item_open_back_ll).setOnClickListener(this);
        mMenuView.findViewById(R.id.item_open_new_ll).setOnClickListener(this);
        mMenuView.findViewById(R.id.item_save_page_ll).setOnClickListener(this);
        mMenuView.findViewById(R.id.item_copy_link_text_ll).setOnClickListener(this);
        mMenuView.findViewById(R.id.item_copy_link_ll).setOnClickListener(this);


        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout mCopyTextView = (LinearLayout) mMenuView.findViewById(R.id.item_copy_link_text_ll);

        if (null == mTitle ){
            mCopyTextView.setVisibility(View.GONE);
        }else{
            mTitle = mTitle.replaceAll("\n","");
            mTitle = mTitle.replaceAll("\t","");
            mTitle = mTitle.replaceAll(" ","");
            if (mTitle.equals("")){
                mCopyTextView.setVisibility(View.GONE);
            }else{
            }
        }


        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(android.R.color.white));
        setContentView(mMenuView);

        //mMenuBg.setBackgroundColor(mMenuView.getResources().getColor(android.R.color.white));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_view_img_ll:
                dismiss();
                TabViewManager.getInstance().getCurrentTabView().loadUrl(mImgUrl, Constants.NAVIGATESOURCE_NORMAL);
                CropStorageUtil.recycle();
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.LONGTOUCHCLICKMENU,
                        GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE,
                        GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_VIEW_IMAGE);
                break;
            case R.id.item_download_img_ll:
                dismiss();
                String pageUrl = TabViewManager.getInstance().getCurrentUrl();
                DownloadHelper.downloadImg(pageUrl, mImgUrl,false);
                CropStorageUtil.recycle();
                break;
            case R.id.item_share_img_ll:
                dismiss();
                shareImg();
                CropStorageUtil.recycle();
                break;
            case R.id.item_open_back_ll:
                dismiss();
                TabViewManager.getInstance().addTabView(mUrl, false, false, false);
                CropStorageUtil.recycle();
                disableAdBlockToast();
                break;
            case R.id.item_open_new_ll:
                dismiss();
                TabViewManager.getInstance().addTabView(mUrl, false, true, false);
                CropStorageUtil.recycle();
                break;
            case R.id.item_save_page_ll:
                dismiss();
                SavedPageUtil.savePage(mContext, mWebTitle, mWebUrl);
                CropStorageUtil.recycle();
//			if (AppEnv.sIsStatSwitchOpen) {
//				Map<String,String> map = new HashMap<String, String>();
//				map.put(ConfigDefine.WEB_LONGPRESS_MENU_KEY, ConfigDefine.LONGPRESS_SAVEPAGE_CLICK);
//				Statistics.sendEventStatis( ConfigDefine.WEB_LONGPRESS_MENU, map);
//			}
                break;
            case R.id.item_copy_link_ll:
                dismiss();
                CustomToastUtils.getInstance().showTextToast(R.string.toast_copy_link);
                copyText(mUrl);
                CropStorageUtil.recycle();

                break;
            case R.id.item_copy_link_text_ll:
                dismiss();
                if (null == mTitle){
//                    CustomToastUtil.getInstance().showDurationToast(R.string.toast_copy_no_text);
                    return;
                }
                CustomToastUtils.getInstance().showTextToast(R.string.toast_copy_link_text);

                copyText(mTitle);
                CropStorageUtil.recycle();
                break;

            default:
                break;
        }

    }

    // TODO: 图片分享
    private void shareImg() {
        CustomShareDialog dialog = new CustomShareDialog((Activity) mContext,mImgUrl, CustomShareDialog.CHANNEL_IMAGE_SHARE);
        dialog.show();
    }

    private void copyText(String copyText) {
        if (copyText != null && copyText.length() > 0) {
            // 得到剪贴板管理器
            android.content.ClipboardManager cmb = (android.content.ClipboardManager) JuziApp
                    .getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            if (!TextUtils.isEmpty(copyText)) {
                cmb.setPrimaryClip(ClipData.newPlainText(null, copyText));
            }
        }
    }
    public void setLinkText(String text) {
        mLinkText = text;
    }

    private void disableAdBlockToast() {
        ConfigManager.getInstance().setAdBlockToast(false);
    }
}
