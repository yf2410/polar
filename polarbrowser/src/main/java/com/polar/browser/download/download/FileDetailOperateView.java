package com.polar.browser.download.download;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.download.DecompresstionFolderActivity;
import com.polar.browser.i.IDownloadViewOperateDelegate;

import static com.polar.browser.download_refactor.Constants.TYPE_DECOMPRESS;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;


public class FileDetailOperateView extends LinearLayout implements View.OnClickListener {

    private View mViewComplete;
    private View mViewDelete;
    private View mViewMore;
    private TextView mViewCheckAll;

    private IDownloadViewOperateDelegate.IEditCallback mDelegate;
    private CommonCheckBox1 mCbCheckAll;

    public FileDetailOperateView(Context context) {
        this(context, null);
    }

    public FileDetailOperateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        inflate(getContext(), R.layout.view_file_detail_operate, this);
        setVisibility(View.GONE);
        initView();
        initListener();
    }

    public void init(IDownloadViewOperateDelegate.IEditCallback delegate) {
        mDelegate = delegate;
    }

    private void initView() {
        mViewComplete = findViewById(R.id.tv_complete);
        mViewDelete = findViewById(R.id.tv_delete);
        mViewMore = findViewById(R.id.tv_more);
        mViewCheckAll = (TextView)findViewById(R.id.tv_check_all);
        mCbCheckAll = (CommonCheckBox1)findViewById(R.id.cb_check_all);
    }

    private void initListener() {
        mViewComplete.setOnClickListener(this);
        mViewDelete.setOnClickListener(this);
        mViewMore.setOnClickListener(this);
        mViewCheckAll.setOnClickListener(this);
        mCbCheckAll.setOnClickListener(this);
    }

    public boolean isEditing() {
        return isShown();
    }

    /**
     * 刷新按钮的可用状态
     * @param checkedCount 选择的条目数
     * @param fileType
     */
    public void notifyEnabled(int checkedCount, String fileType) {
        boolean isFolder = false; //选中的文件是否包含文件夹类型
        if(mDelegate instanceof DecompresstionFolderActivity){
            isFolder = ((DecompresstionFolderActivity) mDelegate).isContainFolder();
        }
        if (checkedCount == 0) {
//            mViewComplete.setEnabled(false);
            mViewDelete.setEnabled(false);
            mViewMore.setEnabled(false);
            mViewCheckAll.setEnabled(true);
        } else if (checkedCount == 1) {
//            mViewShare.setEnabled(!(TYPE_IMAGE.equals(fileType) || isFolder));
            mViewDelete.setEnabled(true);
            mViewMore.setEnabled(!(TYPE_IMAGE.equals(fileType) || isFolder));
            mViewCheckAll.setEnabled(true);
        } else {
//            mViewShare.setEnabled(!(TYPE_IMAGE.equals(fileType) || isFolder));
            mViewDelete.setEnabled(true);
            mViewMore.setEnabled(!(TYPE_IMAGE.equals(fileType) || isFolder||TYPE_DECOMPRESS.equals(fileType)));
            mViewCheckAll.setEnabled(true);
        }
        setCheckedAll(checkedCount == mDelegate.getDataCount());

    }

    /**
     * 设置底部编辑控件是否可用
     * @param enabled
     */
    public void updateEditEnabled(boolean enabled) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cb_check_all:
                mViewCheckAll.performClick();
                break;
            case R.id.tv_check_all:
                if (mDelegate != null) {
                    boolean isAllChecked = mDelegate.getCheckedCount()==mDelegate.getDataCount();
                    mDelegate.setAllChecked(!isAllChecked);
                    mCbCheckAll.setChecked(!isAllChecked);
                }
                break;
            case R.id.tv_complete:
                if (mDelegate != null) {
                    mDelegate.complete();
                }
                break;
            case R.id.tv_delete:
                if (mDelegate != null) {
                    mDelegate.delete();
                }
                break;
            case R.id.tv_more:
                if (mDelegate != null) {
                    mDelegate.more();
                }
                break;

            default:
                break;
        }
    }


    public void onIntoEditMode(String fileType) {
        setVisibility(View.VISIBLE);
        if(mDelegate != null){
            checkCheckAllButton(fileType);
            mDelegate.onIntoEditMode();
        }
    }


    public void onExitEditMode() {
        mCbCheckAll.setChecked(false);
        setVisibility(View.GONE);
        if(mDelegate != null){
            mDelegate.onExitEditMode();
        }
    }

    public int getCheckedCount(){
        if(mDelegate!=null)
            return mDelegate.getCheckedCount();
        return 0;
    }

    public void checkCheckAllButton(String fileType) {
        int checkedCount = getCheckedCount();
//        if(mDelegate!=null)
//                mViewCheckAll.setText(getResources().
//                        getString(checkedCount == mDelegate.getDataCount()?R.string.check_all_cancel:R.string.check_all));

        notifyEnabled(checkedCount,fileType);
    }

    public void onFilePathChanged(int token, Object cookie, Uri uri,
                                  ContentValues values, String selection, String[] selectionArgs) {
        if(mDelegate!=null)
            mDelegate.onFilePathChanged(token,cookie,uri,values,selection,selectionArgs);
    }

    public void fileCountChanged(){
        if(mDelegate!=null)
            mDelegate.onFileCountChanged();
    }

    public void setCheckedAll(boolean isCheckedAll) {
        mCbCheckAll.setChecked(isCheckedAll);
    }

    public View getCheckAllView() {
        return mViewCheckAll;
    }

    public View getViewMore() {
        return mViewMore;
    }
}
