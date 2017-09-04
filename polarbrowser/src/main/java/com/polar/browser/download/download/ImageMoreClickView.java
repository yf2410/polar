package com.polar.browser.download.download;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.polar.browser.R;
import com.polar.browser.bean.ImageItemInfo;
import com.polar.browser.download.FilePropActivity;
import com.polar.browser.download.ImageFolderDetailsActivity;
import com.polar.browser.utils.DensityUtil;

import java.util.List;

/**
 * 下载列表 More点击 PopupWindow
 */
public class ImageMoreClickView extends PopupWindow implements
        OnClickListener {

    private static final int WIDTH_DP = 160;
    private static final int HEIGHT_DP = 184;
    private View mAnchorView;
    private ImageFolderDetailsActivity mContext;
    private View itemRename;
    private View itemFileDetail;

    private List<ImageItemInfo> imageItemInfos;
    private View itemSetWallpaper;
    private View itemMove;
    private boolean isShowing;
    private View mMenuView;

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.item_set_wallpaper:
                mContext.setImageWallpaper(imageItemInfos.get(0));
                break;
            case R.id.item_rename:
                mContext.reName(imageItemInfos.get(0));
                break;
            case R.id.item_move:
                mContext.changeStorageLocation(imageItemInfos);
                break;
            case R.id.item_file_detail:
                FilePropActivity.start(mContext, imageItemInfos.get(0));
                break;
            default:
                break;
        }
        mContext.exitEditMode();
    }


    public ImageMoreClickView(Context c,  View mAnchorView) {
        mContext = (ImageFolderDetailsActivity) c;
        this.mAnchorView = mAnchorView;
        initView(c);
        initListeners();
    }

    private void initListeners() {
        itemSetWallpaper.setOnClickListener(this);
        itemRename.setOnClickListener(this);
        itemMove.setOnClickListener(this);
        itemFileDetail.setOnClickListener(this);
    }

    private void initView(Context c) {
        LayoutInflater mInflater = LayoutInflater.from(c);

        mMenuView = mInflater.inflate(R.layout.image_more_click, null);

        itemSetWallpaper = mMenuView.findViewById(R.id.item_set_wallpaper);
        itemRename = mMenuView.findViewById(R.id.item_rename);
        itemMove = mMenuView.findViewById(R.id.item_move);
        itemFileDetail = mMenuView.findViewById(R.id.item_file_detail);
        setWidth(DensityUtil.dip2px(c, WIDTH_DP));
        setFocusable(false);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(android.R.color.white));
        setContentView(mMenuView);
    }


    public void setMulti(boolean multi) {
        setHeight(DensityUtil.dip2px(mContext, multi ? HEIGHT_DP / 3 : HEIGHT_DP));
        itemSetWallpaper.setVisibility(multi?View.GONE:View.VISIBLE);
        itemRename.setVisibility(multi?View.GONE:View.VISIBLE);
        itemFileDetail.setVisibility(multi?View.GONE:View.VISIBLE);
//            itemReDownload.setBackgroundResource(R.drawable.list_row_one_selector);
        mMenuView.findViewById(R.id.first_line).setVisibility(multi?View.GONE:View.VISIBLE);
        mMenuView.findViewById(R.id.second_line).setVisibility(multi?View.GONE:View.VISIBLE);
        mMenuView.findViewById(R.id.third_line).setVisibility(multi?View.GONE:View.VISIBLE);
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

    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    public void setImageItemInfos(List<ImageItemInfo> imageItemInfos) {
        this.imageItemInfos = imageItemInfos;
    }
}

