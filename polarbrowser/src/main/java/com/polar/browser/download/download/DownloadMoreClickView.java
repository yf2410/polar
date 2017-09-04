package com.polar.browser.download.download;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.polar.browser.R;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download.DownloadDetailActivity;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.utils.DensityUtil;

/**
 * 下载列表 More点击 PopupWindow
 */
public class DownloadMoreClickView extends PopupWindow implements
        OnClickListener {

    private static final int WIDTH_DP = 160;
    private static final int HEIGHT_DP = 138;
    private DownloadItemInfo downloadItemInfo;
    private View mAnchorView;
    private Context mContext;
    private View itemShare;
    private View itemReDownload;
    private View itemRename;
    private View itemDownDetail;

    private boolean isShowing;
    private View mMenuView;


    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.item_share:
                ((DownloadActivity) mContext).share();
                break;
            case R.id.item_redownload:
                ((DownloadActivity) mContext).redownload();
                break;
            case R.id.item_rename:
                ((DownloadActivity) mContext).rename(downloadItemInfo);
                break;
            case R.id.item_download_detail:
                Intent intent = new Intent(mContext, DownloadDetailActivity.class);
                intent.putExtra("DownloadItemInfo", downloadItemInfo);
                mContext.startActivity(intent);
                ((DownloadActivity) mContext).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            default:
                break;
        }
        ((DownloadActivity) mContext).exitEditingMode();
    }


    public DownloadMoreClickView(Context c, DownloadItemInfo downloadItemInfo, View mAnchorView) {
        mContext = c;
        this.downloadItemInfo = downloadItemInfo;
        this.mAnchorView = mAnchorView;
        initView(c);
        initListeners();
    }

    private void initListeners() {
        itemShare.setOnClickListener(this);
        itemReDownload.setOnClickListener(this);
        itemRename.setOnClickListener(this);
        itemDownDetail.setOnClickListener(this);
    }

    private void initView(Context c) {
        LayoutInflater mInflater = LayoutInflater.from(c);

        this.mMenuView = mInflater.inflate(R.layout.download_list_more, null);
        View mMenuBg = mMenuView.findViewById(R.id.menu_bg);

        itemShare = mMenuView.findViewById(R.id.item_share);
        itemReDownload = mMenuView.findViewById(R.id.item_redownload);
        itemRename = mMenuView.findViewById(R.id.item_rename);
        itemDownDetail = mMenuView.findViewById(R.id.item_download_detail);
        setWidth(DensityUtil.dip2px(c, WIDTH_DP));

        setFocusable(false);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(android.R.color.white));
        setContentView(mMenuView);

    }


    /**
     * 计算菜单弹出的位置，弹出菜单
     */
    public void showMenu() {
        if (isShowing()) {
            isShowing = !isShowing;
            return;
        }
        int[] location = new int[2];
        mAnchorView.getLocationOnScreen(location);
        showAtLocation(mAnchorView, Gravity.NO_GRAVITY, location[0] - (getWidth() - mAnchorView.getWidth()) / 2, location[1] - getHeight());
        isShowing = true;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setMulti(boolean isMulti) {
        setHeight(DensityUtil.dip2px(mContext, isMulti ? 61 : HEIGHT_DP));
        itemShare.setVisibility(isMulti?View.GONE:View.VISIBLE);
        itemRename.setVisibility(isMulti?View.GONE: View.VISIBLE);
        itemDownDetail.setVisibility(View.GONE);
//            itemReDownload.setBackgroundResource(R.drawable.list_row_one_selector);
        mMenuView.findViewById(R.id.first_line).setVisibility(isMulti?View.GONE:View.VISIBLE);
        mMenuView.findViewById(R.id.second_line).setVisibility(isMulti?View.GONE:View.VISIBLE);
        mMenuView.findViewById(R.id.third_line).setVisibility(isMulti?View.GONE:View.VISIBLE);
    }

    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }
}

