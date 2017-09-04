package com.polar.browser.download.download;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.i.IDownloadViewOperateDelegate;
import com.polar.browser.statistics.Statistics;

/**
 * Created by duan on 16/9/27.
 */
public class OperateView extends RelativeLayout implements View.OnClickListener {

    private View mLlEdit;
    private View mLlOperate;

    private View mViewClear;
    private View mViewEdit;
//    private View mViewShare;
    private View mViewCheckAll;
    private View mViewDelete;
    private View mViewMore;
    private View mViewComplete;

    private IDownloadViewOperateDelegate mDelegate;
    private CommonCheckBox1 mCbCheckAll;

    public OperateView(Context context) {
        this(context, null);
    }

    public OperateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_operate, this);
        initView();
        initListener();
    }

    public void init(IDownloadViewOperateDelegate delegate) {
        mDelegate = delegate;
    }

    private void initView() {
        mLlEdit = findViewById(R.id.ll_edit);
        mLlOperate = findViewById(R.id.ll_operate);
        mViewClear = findViewById(R.id.tv_clear);
        mViewEdit = findViewById(R.id.tv_edit);
        mCbCheckAll = (CommonCheckBox1)findViewById(R.id.cb_check_all);
        mViewCheckAll = findViewById(R.id.tv_check_all);
        mViewDelete = findViewById(R.id.tv_delete);
        mViewMore = findViewById(R.id.tv_more);
        mViewComplete = findViewById(R.id.tv_complete);
    }

    private void initListener() {
        mViewClear.setOnClickListener(this);
        mViewEdit.setOnClickListener(this);
        mViewDelete.setOnClickListener(this);
        mViewMore.setOnClickListener(this);
        mViewComplete.setOnClickListener(this);
        mCbCheckAll.setOnClickListener(this);
    }

    public void intoEditingMode() {
        mLlEdit.setVisibility(View.GONE);
        mLlOperate.setVisibility(View.VISIBLE);
    }

    public void exitEditingMode() {
        mLlEdit.setVisibility(View.VISIBLE);
        mLlOperate.setVisibility(View.GONE);
        mCbCheckAll.setChecked(false);
    }

    public boolean isEditing() {
        return mLlOperate.isShown();
    }

    /**
     * 刷新按钮的可用状态
     * @param checkedCount 选择的条目数
     */
    public void notifyEnabled(int checkedCount) {
        if (checkedCount == 0) {
            mViewDelete.setEnabled(false);
            mViewMore.setEnabled(false);
            mViewComplete.setEnabled(true);
        } else if (checkedCount == 1) {
            mViewDelete.setEnabled(true);
            mViewMore.setEnabled(true);
            mViewComplete.setEnabled(true);
        } else {
            mViewDelete.setEnabled(true);
            mViewMore.setEnabled(true);
            mViewComplete.setEnabled(true);
        }
        mCbCheckAll.setChecked(((DownloadActivity) getContext()).itemCount() == checkedCount);


    }

    /**
     * 设置底部编辑控件是否可用
     * @param enabled
     */
    public void updateEditEnabled(boolean enabled) {
        mViewEdit.setEnabled(enabled);
        mViewClear.setEnabled(enabled);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_clear:
                if (mDelegate != null) {
                    mDelegate.clear();
                }
                break;
            case R.id.tv_edit:
                if (mDelegate != null) {
                    mDelegate.edit();
                }
                break;
            case R.id.cb_check_all:
                mViewCheckAll.performClick();
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
            case R.id.tv_complete:
                if (mDelegate != null) {
                    mDelegate.complete();
                }
                break;
            default:
                break;
        }
    }

    public void initAllStart(int pageSelected) {
        String type;
        if (pageSelected == DownloadActivity.TAB_DOWNLOAD){
            mLlOperate.setVisibility(isEditing()?View.VISIBLE: View.GONE);
            mLlEdit.setVisibility(View.VISIBLE);
            type = GoogleConfigDefine.DOWNLOAD_CLICK_DOWNLOAD_TAB;
        }else{
            mLlEdit.setVisibility(View.GONE);
            mLlOperate.setVisibility(View.GONE);
            type = GoogleConfigDefine.DOWNLOAD_CLICK_FILE_TAB;
        }
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,type);
    }


    public void onIntoEditMode() {
        if(mDelegate!=null&&mDelegate instanceof IDownloadViewOperateDelegate.IEditCallback){
            ((IDownloadViewOperateDelegate.IEditCallback)mDelegate).onIntoEditMode();
        }
    }


    public void onExitEditMode() {
        if(mDelegate!=null&&mDelegate instanceof IDownloadViewOperateDelegate.IEditCallback){
            ((IDownloadViewOperateDelegate.IEditCallback)mDelegate).onExitEditMode();
        }
    }

    public View getCheckAllView() {
        return mViewCheckAll;
    }

    public void setCheckedAll(boolean isCheckedAll) {
        mCbCheckAll.setChecked(isCheckedAll);
    }

    public View getViewMore() {
        return mViewMore;
    }
}
