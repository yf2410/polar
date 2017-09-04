package com.polar.browser.download.download;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.bean.BaseFileClass;
import com.polar.browser.download.FilePropActivity;
import com.polar.browser.download.view.AbstractFileListView;
import com.polar.browser.i.IDownloadViewOperateDelegate;
import com.polar.browser.utils.DensityUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * 文件 编辑，更多点击后出现的 PopWindow
 */
public class FileMoreClickView extends PopupWindow implements
        OnClickListener {

    private static final int WIDTH_DP = 160;
    private static final int HEIGHT_DP = 184;
    private final IDownloadViewOperateDelegate.IEditCallback mDelegate;
    private final List<? extends BaseFileClass> files;
    private final Context context;
    private View mAnchorView;
    private View itemShare;
    private View itemMove;
    private View itemRename;
    private View itemFileDetail;
    private boolean isShowing;
    private View mMenuView;


    @Override
    public void onClick(View v) {
        dismiss();
        isShowing  = false;
        AbstractFileListView.EventCallback callback = new AbstractFileListView.EventCallback();
        switch (v.getId()) {
            case R.id.item_share:
                mDelegate.share();
                break;
            case R.id.item_move:
//                ((AbstractFileListView) mDelegate).changeLocation(files);
                callback.type = AbstractFileListView.EventCallback.TYPE_CHANGE_LOCATION;
                callback.files = files;
                EventBus.getDefault().post(callback);
                break;
            case R.id.item_rename:
//                ((AbstractFileListView) mDelegate).reName(files.get(0));
                callback.type = AbstractFileListView.EventCallback.TYPE_RENAME;
                callback.files = files;
                EventBus.getDefault().post(callback);
                break;
            case R.id.item_file_detail:
                FilePropActivity.start(context, files.get(0));
                break;
            default:
                break;
        }
        mDelegate.onExitEditMode();
    }


    public FileMoreClickView(Context context, IDownloadViewOperateDelegate.IEditCallback mDelegate,
                             List<? extends BaseFileClass> files, View mAnchorView) {
        this.context = context;
        this.files = files;
        this.mAnchorView = mAnchorView;
        this.mDelegate = mDelegate;
        initView();
        initListeners();
    }

    private void initListeners() {
        itemShare.setOnClickListener(this);
        itemMove.setOnClickListener(this);
        itemRename.setOnClickListener(this);
        itemFileDetail.setOnClickListener(this);
    }

    private void initView() {
        LayoutInflater mInflater = LayoutInflater.from(JuziApp.getAppContext());

        mMenuView = mInflater.inflate(R.layout.file_list_more, null);

        itemShare = mMenuView.findViewById(R.id.item_share);
        itemMove = mMenuView.findViewById(R.id.item_move);
        itemRename = mMenuView.findViewById(R.id.item_rename);
        itemFileDetail = mMenuView.findViewById(R.id.item_file_detail);
        setWidth(DensityUtil.dip2px(JuziApp.getAppContext(), WIDTH_DP));

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

    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    public void setMulti(boolean isMulti) {
        setHeight(DensityUtil.dip2px(JuziApp.getAppContext(), isMulti ? HEIGHT_DP / 3 : HEIGHT_DP));
        itemShare.setVisibility(isMulti ? View.GONE : View.VISIBLE);
        itemRename.setVisibility(isMulti?View.GONE: View.VISIBLE);
        itemFileDetail.setVisibility(isMulti?View.GONE:View.VISIBLE);
//            itemMove.setBackgroundResource(R.drawable.list_row_one_selector);
        mMenuView.findViewById(R.id.first_line).setVisibility(isMulti?View.GONE:View.VISIBLE);
        mMenuView.findViewById(R.id.second_line).setVisibility(isMulti?View.GONE:View.VISIBLE);
        mMenuView.findViewById(R.id.third_line).setVisibility(isMulti?View.GONE:View.VISIBLE);
    }
}

