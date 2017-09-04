package com.polar.browser.homepage.customlogo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.polar.browser.R;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.view.ObservableScrollView;

import java.util.ArrayList;
import java.util.List;

public class HomeLogoView extends LinearLayout {

    private LogoItem mItem1;
    private LogoItem mItem2;
    private LogoItem mItem3;
    private LogoItem mItem4;
    private LogoItem mItem5;
    private LogoItem mItem6;
    private LogoItem mItem7;
    private LogoItem mItem8;
    private LogoItem mItem9;
    private LogoItem mItem10;
    private LogoItem mItem11;
    private LogoItem mItem12;
    private LogoItem mItem13;
    private LogoItem mItem14;
    private LogoItem mItem15;
    private LogoItem mItem16;
    private LogoItem mItem17;
    private LogoItem mItem18;
    private LogoItem mItem19;
    private LogoItem mItem20;

    private List<LogoItem> items = new ArrayList<>();

    private View mLine1;
    private View mLine2;
    private View mLine3;
    private View mLine4;

    private IEditLogo mEditLogoDelegate;
    private ObservableScrollView mScrollView;

    private boolean mIsLogoLongClick;

    public HomeLogoView(Context context) {
        this(context, null);
    }

    public HomeLogoView(Context context, AttributeSet attrs) {
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

        inflate(getContext(), R.layout.view_home_logos, this);
        mItem1 = (LogoItem) findViewById(R.id.logo_item1);
        items.add(mItem1);
        mItem2 = (LogoItem) findViewById(R.id.logo_item2);
        items.add(mItem2);
        mItem3 = (LogoItem) findViewById(R.id.logo_item3);
        items.add(mItem3);
        mItem4 = (LogoItem) findViewById(R.id.logo_item4);
        items.add(mItem4);
        mItem5 = (LogoItem) findViewById(R.id.logo_item5);
        items.add(mItem5);
        mItem6 = (LogoItem) findViewById(R.id.logo_item6);
        items.add(mItem6);
        mItem7 = (LogoItem) findViewById(R.id.logo_item7);
        items.add(mItem7);
        mItem8 = (LogoItem) findViewById(R.id.logo_item8);
        items.add(mItem8);
        mItem9 = (LogoItem) findViewById(R.id.logo_item9);
        items.add(mItem9);
        mItem10 = (LogoItem) findViewById(R.id.logo_item10);
        items.add(mItem10);
        mItem11 = (LogoItem) findViewById(R.id.logo_item11);
        items.add(mItem11);
        mItem12 = (LogoItem) findViewById(R.id.logo_item12);
        items.add(mItem12);
        mItem13 = (LogoItem) findViewById(R.id.logo_item13);
        items.add(mItem13);
        mItem14 = (LogoItem) findViewById(R.id.logo_item14);
        items.add(mItem14);
        mItem15 = (LogoItem) findViewById(R.id.logo_item15);
        items.add(mItem15);
        mItem16 = (LogoItem) findViewById(R.id.logo_item16);
        items.add(mItem16);
        mItem17 = (LogoItem) findViewById(R.id.logo_item17);
        items.add(mItem17);
        mItem18 = (LogoItem) findViewById(R.id.logo_item18);
        items.add(mItem18);
        mItem19 = (LogoItem) findViewById(R.id.logo_item19);
        items.add(mItem19);
        mItem20 = (LogoItem) findViewById(R.id.logo_item20);
        items.add(mItem20);
        mLine1 = findViewById(R.id.ll_line1);
        mLine2 = findViewById(R.id.ll_line2);
        mLine3 = findViewById(R.id.ll_line3);
        mLine4 = findViewById(R.id.ll_line4);
    }

    public void setIEditLogo(IEditLogo editLogo) {
        this.mEditLogoDelegate = editLogo;
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setEditLogoDelegate(mEditLogoDelegate);
        }
    }

    public synchronized void resetData(List<HomeSite> data) {
        if (ListUtils.isEmpty(data)) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }
        for (int i = 0; i < data.size(); i++) {
            items.get(i).bind(data.get(i));
            items.get(i).setDelegate(mScrollView, this);
            items.get(i).setVisibility(View.VISIBLE);
        }
        int totalCount = data.size();
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
        if (totalCount > 20) {
            mLine1.setVisibility(View.VISIBLE);
            mLine2.setVisibility(View.VISIBLE);
            mLine3.setVisibility(View.VISIBLE);
            mLine4.setVisibility(View.VISIBLE);
        } else if (totalCount > 15) {
            mLine1.setVisibility(View.VISIBLE);
            mLine2.setVisibility(View.VISIBLE);
            mLine3.setVisibility(View.VISIBLE);
            mLine4.setVisibility(View.VISIBLE);
        } else if (totalCount > 10) {
            mLine1.setVisibility(View.VISIBLE);
            mLine2.setVisibility(View.VISIBLE);
            mLine3.setVisibility(View.VISIBLE);
            mLine4.setVisibility(View.GONE);
        } else if (totalCount > 5) {
            mLine1.setVisibility(View.VISIBLE);
            mLine2.setVisibility(View.VISIBLE);
            mLine3.setVisibility(View.GONE);
            mLine4.setVisibility(View.GONE);
        } else if (totalCount > 0) {
            mLine1.setVisibility(View.VISIBLE);
            mLine2.setVisibility(View.GONE);
            mLine3.setVisibility(View.GONE);
            mLine4.setVisibility(View.GONE);
        } else {

        }
    }


    public void resetDatas(List<HomeSite> data) {
        synchronized (data) {
            if (data == null || data.size() == 0) {
                setVisibility(GONE);
                return;
            } else {
                setVisibility(VISIBLE);
            }
            for (int i = 0; i < data.size(); i++) {
                items.get(i).bind(data.get(i));
                items.get(i).setDelegate(mScrollView, this);
                items.get(i).setVisibility(View.VISIBLE);
            }
            int totalCount = data.size();
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
            if (totalCount > 20) {
                mLine1.setVisibility(View.VISIBLE);
                mLine2.setVisibility(View.VISIBLE);
                mLine3.setVisibility(View.VISIBLE);
                mLine4.setVisibility(View.VISIBLE);
            } else if (totalCount > 15) {
                mLine1.setVisibility(View.VISIBLE);
                mLine2.setVisibility(View.VISIBLE);
                mLine3.setVisibility(View.VISIBLE);
                mLine4.setVisibility(View.VISIBLE);
            } else if (totalCount > 10) {
                mLine1.setVisibility(View.VISIBLE);
                mLine2.setVisibility(View.VISIBLE);
                mLine3.setVisibility(View.VISIBLE);
                mLine4.setVisibility(View.GONE);
            } else if (totalCount > 5) {
                mLine1.setVisibility(View.VISIBLE);
                mLine2.setVisibility(View.VISIBLE);
                mLine3.setVisibility(View.GONE);
                mLine4.setVisibility(View.GONE);
            } else if (totalCount > 0) {
                mLine1.setVisibility(View.VISIBLE);
                mLine2.setVisibility(View.GONE);
                mLine3.setVisibility(View.GONE);
                mLine4.setVisibility(View.GONE);
            } else {

            }
        }
    }
}
