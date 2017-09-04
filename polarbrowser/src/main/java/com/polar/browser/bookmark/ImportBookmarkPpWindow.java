package com.polar.browser.bookmark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.polar.browser.R;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;

/**
 * Created by saifei on 17/4/11.
 */

public class ImportBookmarkPpWindow extends PopupWindow implements View.OnClickListener {
    private static final int WIDTH_DP = 200;
    private static final int HEIGHT_DP = 56;
    private final Context mContext;
    private View mAnchorView;

    private View importBookmarkTv;


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
//            case R.id.ppwindow_from_chrome_tv:
//                if (!ImportBookmarManager.getInstance().hasChromeBookmakr() || !ImportBookmarManager.getInstance().importBookmarkFromChrome()) {
//                    CustomToastUtils.getInstance().showTextToast(mContext.getString(R.string.add_bookmark_no_find_chrome));
//                }
//                break;
//            case R.id.ppwindow_from_android_tv:
//                if (!ImportBookmarManager.getInstance().importBookmarkFromSystemBrowser()) {
//                    CustomToastUtils.getInstance().showTextToast(mContext.getString(R.string.add_bookmark_system_empty));
//                }
//                break;
//            case R.id.ppwindow_from_backup_tv:
//                Intent i = new Intent(mContext, BookmarkImportActivity.class);
//                mContext.startActivity(i);
//                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
//                break;
            case R.id.ppwindow_import_bookmark:
                ListDialog dialog = new ListDialog(mContext);
                String[] items = new String[]{
                        mContext.getString(R.string.add_bookmark_from_chrome),
                        mContext.getString(R.string.add_bookmark_from_system),
                        mContext.getString(R.string.add_bookmark_from_file)
                };
                dialog.setItems(items, -1);
                dialog.setOnItemClickListener(mImportListItemClickListener);
                dialog.show();
                break;
        }
        dismiss();
    }


    public ImportBookmarkPpWindow(Context c,View mAnchorView) {
        this.mContext = c;
        this.mAnchorView = mAnchorView;
        initView(c);
        initListeners();
    }

    private void initListeners() {
//        fromChromeTv.setOnClickListener(this);
//        fromAndroidTv.setOnClickListener(this);
//        fromBackupTv.setOnClickListener(this);
        importBookmarkTv.setOnClickListener(this);
    }

    private void initView(Context c) {
        LayoutInflater mInflater = LayoutInflater.from(c);

        View mMenuView = mInflater.inflate(R.layout.ppwindow_import_bookmark_, null);

//        fromChromeTv = mMenuView.findViewById(R.id.ppwindow_from_chrome_tv);
//        fromAndroidTv = mMenuView.findViewById(R.id.ppwindow_from_android_tv);
//        fromBackupTv = mMenuView.findViewById(R.id.ppwindow_from_backup_tv);
        importBookmarkTv = mMenuView.findViewById(R.id.ppwindow_import_bookmark);
        setWidth(DensityUtil.dip2px(c, WIDTH_DP));
        setHeight(DensityUtil.dip2px(c,HEIGHT_DP));

        setFocusable(false);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(android.R.color.white));
        setContentView(mMenuView);

    }


    /**
     * 计算菜单弹出的位置，弹出菜单
     */
    public void showMenu() {
        showAtLocation(mAnchorView,Gravity.RIGHT|Gravity.TOP,0,mAnchorView.getHeight()+DensityUtil.getStatusBarHeight(mAnchorView.getContext()));
    }

    private AdapterView.OnItemClickListener mImportListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            switch (position) {
                case ImportBookmarManager.IMPORT_FROM_CHROME:
                    if (!ImportBookmarManager.getInstance().hasChromeBookmakr() || !ImportBookmarManager.getInstance().importBookmarkFromChrome()) {
                        CustomToastUtils.getInstance().showTextToast(mContext.getString(R.string.add_bookmark_no_find_chrome));
                    }
                    break;
                case ImportBookmarManager.IMPORT_FROM_SYSTEM:
                    if (!ImportBookmarManager.getInstance().importBookmarkFromSystemBrowser()) {
                        CustomToastUtils.getInstance().showTextToast(mContext.getString(R.string.add_bookmark_system_empty));
                    }
                    break;
                case ImportBookmarManager.IMPORT_FROM_FILE:
                    Intent i = new Intent(mContext, BookmarkImportActivity.class);
                    mContext.startActivity(i);
                    ((Activity)mContext).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;
                default:
                    break;
            }
        }
    };

}
