package com.polar.browser.homepage.customlogo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.view.ObservableScrollView;
import com.polar.browser.view.TabLongClickLinkView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HomeVisitedView extends LinearLayout {

    private VisitedItem mItem1;
    private VisitedItem mItem2;
    private VisitedItem mItem3;
    private VisitedItem mItem4;
    private VisitedItem mItem5;

    private List<VisitedItem> items = new ArrayList<>();

    private IEditLogo mEditLogoDelegate;
    private ObservableScrollView mScrollView;

    private boolean mIsLogoLongClick;

    public HomeVisitedView(Context context) {
        this(context, null);
    }

    public HomeVisitedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setIsLogoLongClick(boolean isLongClick) {
        mIsLogoLongClick = isLongClick;
    }

    public boolean isLogoLongClick() {
        return mIsLogoLongClick;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mIsLogoLongClick) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mEditLogoDelegate.onLongClickUp();
                mIsLogoLongClick = false;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    public void init(ObservableScrollView scrollView) {
        this.mScrollView = scrollView;

        inflate(getContext(), R.layout.view_home_visited, this);
        findViewById(R.id.rl_visited);
        mItem1 = (VisitedItem) findViewById(R.id.logo_item1);
        items.add(mItem1);
        mItem2 = (VisitedItem) findViewById(R.id.logo_item2);
        items.add(mItem2);
        mItem3 = (VisitedItem) findViewById(R.id.logo_item3);
        items.add(mItem3);
        mItem4 = (VisitedItem) findViewById(R.id.logo_item4);
        items.add(mItem4);
        mItem5 = (VisitedItem) findViewById(R.id.logo_item5);
        items.add(mItem5);

        findViewById(R.id.home_visited_more).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopWindow(v);
            }
        });
    }

    private List<HistoryRecord> mHistoryRecords;
    public synchronized void resetData(List<HistoryRecord> historyRecords) {
        mHistoryRecords = historyRecords;
        if (ListUtils.isEmpty(historyRecords)) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }
        for (int i = 0; i < historyRecords.size(); i++) {
            items.get(i).bind(historyRecords.get(i));
            items.get(i).setDelegate(mScrollView, this, i);
            items.get(i).setVisibility(View.VISIBLE);
        }
        int totalCount = historyRecords.size();
        int a = (int) (Math.ceil(totalCount / 5.0) * 5);
        for (int j = totalCount; j < a; j++) {
            if (j <= SiteManager.MAX_LOGO_SIZE) {
                items.get(j).setVisibility(View.INVISIBLE);
            }
        }
        // GONE隐藏剩余的
        for (int k = a; k < items.size(); k++) {
            items.get(k).setVisibility(View.GONE);
        }
    }

    private PopupWindow popWindow;
    public void showPopWindow(View v) {
        if(popWindow==null){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.home_visited_popup, null);
            TextView item1Tv = (TextView) view.findViewById(R.id.popup_item1);
            TextView item2Tv = (TextView) view.findViewById(R.id.popup_item2);
            item1Tv.setText(R.string.visited_clear);
            item2Tv.setText(R.string.visited_close);
            view.findViewById(R.id.lin_popup_item1).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    popWindow.dismiss();

                    final CommonDialog dialog = new CommonDialog(getContext(),
                            getContext().getString(R.string.tips), getContext().getString(R.string.visited_all_clear));
                    dialog.setBtnCancel(getContext().getString(R.string.cancel),
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.setBtnOk(getContext().getString(R.string.ok), new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            setVisibility(GONE);

                            ThreadManager.getIOHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for (int i = 0; i < mHistoryRecords.size(); i++) {
                                            HistoryRecord historyRecord = mHistoryRecords.get(i);
                                            historyRecord.setCount(0);
                                            HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).updateAllHistoryRecord(historyRecord);
                                        }
                                        mHistoryRecords.clear();
                                        SiteManager.getInstance().updateHistoryRecords(mHistoryRecords);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            CustomToastUtils.getInstance().showTextToast(R.string.visited_already_clear_tip);
                            Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY_VISITED, GoogleConfigDefine.VISITED_CLEAR);

                        }
                    });
                    dialog.show();

                }
            });
            view.findViewById(R.id.lin_popup_item2).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v){
                    popWindow.dismiss();
                    CustomToastUtils.getInstance().showTextToast(R.string.visited_setting_tip);
                    setVisibility(GONE);
                    ConfigManager.getInstance().setHistoryVisitedEnabled(false);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY_VISITED, GoogleConfigDefine.VISITED_HOME_CLOSE);
                }
            });
            int menuWidth = DensityUtil.dip2px(getContext(), TabLongClickLinkView.WIDTH_DP);
            popWindow = new PopupWindow(view, menuWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//            popWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popWindow.setOutsideTouchable(false);
            popWindow.setBackgroundDrawable(new BitmapDrawable());
            popWindow.setAnimationStyle(R.style.pop);
        }
        popWindow.showAsDropDown(v,0,0);
    }



}
