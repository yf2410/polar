package com.polar.browser.bookmark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonBottomBar3;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.i.IEditStateObserver;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookmarkView extends LinearLayout implements IBookmarkObserver,View.OnClickListener {

    private final BookmarkActivity mActivity;
    private DragSortListView mList;

    private BookmarkListAdapter mAdapter;

    private DragSortListView.DropListener mOnDrop;

    private CommonBottomBar3 mBottomBar;

    private RelativeLayout bottomSyncLayout;

    private IBookmarkItemClick mItemClickDelegate;

    private IBookmarkDeleteCallback mDeleteCallback;

    private IEditStateObserver mEditObserver;

    private View mEmptyView;

    private View mBackground;

    //private View mBtnEdit;

    private boolean mIsEditing = false;
    private OnItemClickListener mImportListItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            switch (position) {
                case ImportBookmarManager.IMPORT_FROM_CHROME:
                    if (!ImportBookmarManager.getInstance().hasChromeBookmakr() || !ImportBookmarManager.getInstance().importBookmarkFromChrome()) {
                        CustomToastUtils.getInstance().showTextToast(getContext().getString(R.string.add_bookmark_no_find_chrome));
                    }
                    break;
                case ImportBookmarManager.IMPORT_FROM_SYSTEM:
                    if (!ImportBookmarManager.getInstance().importBookmarkFromSystemBrowser()) {
                        CustomToastUtils.getInstance().showTextToast(getContext().getString(R.string.add_bookmark_system_empty));
                    }
                    break;
                case ImportBookmarManager.IMPORT_FROM_FILE:
                    Intent i = new Intent(getContext(), BookmarkImportActivity.class);
                    getContext().startActivity(i);
                    ((Activity) getContext()).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;
                default:
                    break;
            }
        }
    };
    private View bottomEditTv;
    private View tvImportBookmark;
    private View syncBookmarkLayout;
    private TextView lastTimeTv;

    public BookmarkView(Context context) {
        super(context);
        this.mActivity = (BookmarkActivity)context;
    }

    public void init(IBookmarkItemClick delegate,
                     IBookmarkDeleteCallback deleteCallback,
                     IEditStateObserver editObserver) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_bookmark, this);
        mItemClickDelegate = delegate;
        mDeleteCallback = deleteCallback;
        mEditObserver = editObserver;
        //mBtnEdit = btnEdit;
        initView();
        initListeners();
        initData();

    }

    private void initListeners() {
        syncBookmarkLayout.setOnClickListener(this);
    }

    private void initView() {
        mList = (DragSortListView) findViewById(R.id.list);
        mBackground = findViewById(R.id.rl_bookmark);
        mOnDrop = new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    BookmarkManager.getInstance().changePosition(from, to);
                    mList.moveCheckState(from, to);
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        mList.setDropListener(mOnDrop);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int index,
                                    long arg3) {
                if (mItemClickDelegate != null) {
                    if (!mIsEditing) {
                        mItemClickDelegate.onClick(mAdapter.getData().get(index).url);
                        Statistics.sendOnceStatistics(GoogleConfigDefine.FAVORITE_HISTORY, GoogleConfigDefine.FAVORITE_HISTORY_TYPE_FAVORITE);
                    } else {
                        mItemClickDelegate.onCheckedChange();
                        mBottomBar.setDeleteBtnEnabled(hasItemChecked());
                    }
                }
            }
        });
        /**
         * 长按进入编辑状态，还需要选中当前长按的条目
         */
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                if (!mIsEditing) {
                    changeEditState(true);
                    mEditObserver.onEditStateChanged(true);
                    mList.setItemChecked(index, true);
                    mAdapter.setChecked(index);
                    mItemClickDelegate.onCheckedChange();
                    return true;
                }
                return false;
            }
        });
        mBottomBar = (CommonBottomBar3) findViewById(R.id.bottom_bar);
        ImportBookmarManager.getInstance().init(JuziApp.getAppContext(), this);
        tvImportBookmark = findViewById(R.id.tv_import_bookmark);
        tvImportBookmark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ListDialog dialog = new ListDialog(getContext());
                String[] items = new String[]{
                        getContext().getString(R.string.add_bookmark_from_chrome),
                        getContext().getString(R.string.add_bookmark_from_system),
                        getContext().getString(R.string.add_bookmark_from_file)
                };
                dialog.setItems(items, -1);
                dialog.setOnItemClickListener(mImportListItemClickListener);
                dialog.show();
            }
        });
        bottomSyncLayout = (RelativeLayout) findViewById(R.id.bottom_sync_bar);

        bottomEditTv = findViewById(R.id.btn_edit);
        bottomEditTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditObserver.onEditStateChanged(true);
            }
        });
//		mBottomBar.getButtonCancel().setText(R.string.check_all);
//		mBottomBar.getButtonOK().setText(R.string.delete);
        mBottomBar.getDeleteBtn().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteItems();
            }
        });
        mBottomBar.getCheckAllBtn().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheckedAll = isCheckedAll();
                checkListAll(!isCheckedAll);
                mAdapter.setAllChecked(!isCheckedAll);
                updateCheckState();
            }
        });
        mBottomBar.setDeleteBtnEnabled(false);
        mBottomBar.getTvComplete().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditObserver.onEditStateChanged(false);
            }
        });
        mEmptyView = findViewById(R.id.view_empty);
        this.syncBookmarkLayout =  findViewById(R.id.view_bookmark_sync_layout);
        lastTimeTv = (TextView) findViewById(R.id.view_bookmark_sync_last_time_tv);

        setSyncTime();
    }

    public void setSyncTime() {
        String timeStamp = ConfigManager.getInstance().getLocalBookmarkSyncTime();
        if(!TextUtils.isEmpty(timeStamp)&& AccountLoginManager.getInstance().isUserLogined()){
            lastTimeTv.setVisibility(View.VISIBLE);
            String date = DateUtils.formatDate("yyyy-MM-dd HH:mm",new Date(Long.parseLong(timeStamp)));
            lastTimeTv.setText(String.format(getResources().getString(R.string.sync_bookmark_last_time),date));
        }else{
            lastTimeTv.setVisibility(View.GONE);
        }
    }

    public boolean isNoBookmark() {
        boolean isNoBookmark = mAdapter.getCount() == 0;
        bottomEditTv.setEnabled(!isNoBookmark);
        return isNoBookmark;
    }

    private void checkListAll(boolean isCheck) {
        for (int i = 0; i < mList.getCount(); ++i) {
            mList.setItemChecked(i, isCheck);
        }
    }

    private boolean hasItemChecked() {
        boolean ret = false;
        for (int i = 0; i < mAdapter.getData().size(); ++i) {
            ret = mAdapter.getData().get(i).isChecked;
            if (ret) {
                return ret;
            }
        }
        return ret;
    }

    private void deleteItems() {
        List<String> urlList = new ArrayList<String>();
        if (!hasItemChecked()) {
            CustomToastUtils.getInstance().showTextToast(R.string.delete_not_select);
        } else {
            for (int i = 0; i < mAdapter.getData().size(); ++i) {
                if (mAdapter.getData().get(i).isChecked) {
                    urlList.add(mAdapter.getData().get(i).url);
                }
            }
            showDeleteDialog(urlList);
        }
    }
    // private boolean isNoItemChecked(SparseBooleanArray checkedPos) {
    // if (checkedPos.size() == 0) {
    // return true;
    // } else {
    // for (int i = 0; i < checkedPos.size(); ++i) {
    // if (checkedPos.valueAt(i)) {
    // return false;
    // }
    // }
    // }
    //
    // return true;
    // }

    private void showDeleteDialog(final List<String> urlList) {
        final CommonDialog dialog = new CommonDialog(getContext(), getContext()
                .getString(R.string.tips), getContext().getString(
                R.string.bookmark_delete_content));
        dialog.setBtnCancel(getContext().getString(R.string.cancel),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
        dialog.setBtnOk(getContext().getString(R.string.ok),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        BookmarkManager.getInstance().deleteBookmarkByUrlList(
                                urlList);
                    }
                });
        dialog.show();
    }

    private void initData() {
        mAdapter = new BookmarkListAdapter(getContext(), mList, mItemClickDelegate, mEditObserver);
        mAdapter.updateData(BookmarkManager.getInstance().queryBookmarkInfo());
        mList.setAdapter(mAdapter);
        BookmarkManager.getInstance().registerObserver(this);
        updateEmptyView();
    }

    public void destory() {
        ImportBookmarManager.getInstance().unRegisterObserver();
        BookmarkManager.getInstance().unregisterObserver(this);
    }

    @Override
    public void notifyBookmarkChanged(boolean isAdd,boolean showTip) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                mAdapter.updateData(BookmarkManager.getInstance()
                        .queryBookmarkInfo());
                changeEditState(false);
                if (mDeleteCallback != null) {
                    mDeleteCallback.notifyDelete();
                }
                updateEmptyView();
                setSyncTime();
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    public void updateCheckState() {
        boolean isCheckedAll = isCheckedAll();
        mBottomBar.setCheckAll(isCheckedAll);
        boolean hasItemChecked = hasItemChecked();
        mBottomBar.setDeleteBtnEnabled(hasItemChecked);
//		if (isCheckedAll()) {
//			mBottomBar.getButtonCancel().setText(R.string.check_all_cancel);
//		} else {
//			mBottomBar.getButtonCancel().setText(R.string.check_all);
//		}
    }

    public void changeEditState(boolean isEditing) {
        mIsEditing = isEditing;
        mAdapter.changeEditeState(mIsEditing);
        mList.setDragEnabled(mIsEditing);
        checkListAll(false);
        if (mIsEditing) {
            bottomSyncLayout.setVisibility(View.GONE);
            mBottomBar.setVisibility(View.VISIBLE);
        } else {
            mBottomBar.setVisibility(View.GONE);
            updateCheckState();
            bottomSyncLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateEmptyView() {
        if (mAdapter.getCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            //mBtnEdit.setEnabled(false);
            mBackground.setBackgroundColor(getResources().getColor(
                    R.color.empty_view_background_color));
        } else {
            mEmptyView.setVisibility(View.GONE);
            //mBtnEdit.setEnabled(true);
            mBackground.setBackgroundColor(getResources().getColor(
                    R.color.common_bg_color_5));
        }
    }

    private boolean isCheckedAll() {
        boolean isCheckedAll = true;
        for (int i = 0; i < mList.getCount(); ++i) {
            isCheckedAll = mList.isItemChecked(i);
            if (!isCheckedAll) {
                break;
            }
        }
        return isCheckedAll;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_bookmark_sync_layout:
                mActivity.syncBookmark();
                break;
        }
    }
}
