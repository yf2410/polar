package com.polar.browser.homepage.customlogo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.polar.browser.R;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.common.ui.DragGridView;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.sitelist.AddMoreSiteActivity;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EditLogoView，用来编辑首页图标的界面
 *
 * @author dpk
 */
public class EditLogoView extends RelativeLayout implements IEditLogo, OnClickListener {

    private static final String TAG = "EditLogoView";
    private static final int MARGIN_TOP_DP = 18;
    private static final int MARGIN_LEFT_DP = 15;
    private static final int MARGIN_RIGHT_DP = 15;
    private static final int TITLE_BAR_HEIGHT_DP = 48;
    private static final int PADDING_BOTTOM_NORMAL = 68;
    private static final int PADDING_BOTTOM_MAX = 98;
    private final int mPaddingNormal;
    private final int mPaddingMax;
    private TextView mMaxTip;
    private DragGridView mGridView;
    private View mMainView;
    private List<HomeSite> mData = new ArrayList<>();
    private int mHeight;
    private boolean mIsReduceHeight = false;
    private boolean mIsInit = false;
    private DragAdapter logoAdapter;

    public EditLogoView(Context context) {
        this(context, null);
    }

    public EditLogoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaddingNormal = DensityUtil.dip2px(context, PADDING_BOTTOM_NORMAL);
        mPaddingMax = DensityUtil.dip2px(context, PADDING_BOTTOM_MAX);
    }

    /**
     * @return
     */
    public boolean isInit() {
        return mIsInit;
    }

    /**
     * 延迟加载
     */
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_edit_logo, this);
        setBackgroundColor(getResources().getColor(R.color.day_mode_bg));
        TextView completeBtn = (TextView) findViewById(R.id.tv_edit_logo_complete);
        mGridView = (DragGridView) findViewById(R.id.grid_edit_logo);
        CommonTitleBar commonTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
        commonTitleBar.setBackVisible(false);
        commonTitleBar.setTitleHorizontalCenter();
        mMaxTip = (TextView) findViewById(R.id.tv_logo_max_size);
        View bottomBar = findViewById(R.id.ll_confirm);
        completeBtn.setOnClickListener(this);
        bottomBar.setOnClickListener(this);
        setOnClickListener(this);
        logoAdapter = new DragAdapter(getContext(), mData, mGridView);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setAdapter(logoAdapter);
        refreshData();
        updateUI();
        mIsInit = true;
    }


    private void updateUI() {
        mMaxTip.setVisibility(View.GONE);
        mGridView.setPadding(0, 0, 0, mPaddingNormal);
    }

    private void refreshData() {
        mData.clear();
        try {
            List<HomeSite> homeSites = SiteManager.getInstance().getAllHomeSite();
            mData.addAll(homeSites);
            logoAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void openEditLogoView(final int height, int scrollTop, View mainView, boolean addLogo) {

        if (addLogo) {
            Intent intent = new Intent(getContext(), AddMoreSiteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        } else {
            if (!mIsInit) {
                init();
            } else {
                refreshData();
                updateUI();
            }
            int titleHeight = DensityUtil.dip2px(getContext(), TITLE_BAR_HEIGHT_DP)
                    + AppEnv.STATUS_BAR_HEIGHT;
            mHeight = height - titleHeight;
            if (AppEnv.DEBUG) {
                int marginTop = DensityUtil.dip2px(getContext(), MARGIN_TOP_DP);
                SimpleLog.d(TAG, "mHeight:" + mHeight);
                SimpleLog.d(TAG, "titleHeight:" + titleHeight);
                SimpleLog.d(TAG, "scrollTop:" + scrollTop);
                SimpleLog.d(TAG, "marginTop:" + DensityUtil.dip2px(getContext(), MARGIN_TOP_DP));
                SimpleLog.d(TAG, "delta:" + String.valueOf(marginTop + height));
            }
            if (Build.VERSION.SDK_INT < 20 && (mHeight < -titleHeight)) {
                mHeight += titleHeight;
                mIsReduceHeight = true;
            } else {
                mIsReduceHeight = false;
            }

            if (AppEnv.DEBUG) {
                SimpleLog.d(TAG, "height=" + String.valueOf(height));
                SimpleLog.d(TAG, "mHeight=" + String.valueOf(mHeight));
            }
            setVisibility(View.VISIBLE);
            int marginleft = DensityUtil.dip2px(getContext(), MARGIN_LEFT_DP);
            int marginright = DensityUtil.dip2px(getContext(), MARGIN_RIGHT_DP);
            LayoutParams params = (LayoutParams) mGridView.getLayoutParams();
            params.setMargins(marginleft, mHeight, marginright, 0);
            mMainView = mainView;
            mGridView.setLayoutParams(params);
        }
    }

    /**
     * 关闭的时候进行操作
     */
    public void hide() {

        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                SiteManager siteManager = SiteManager.getInstance();
                for (int i = 0; i < mData.size(); i++) {
                    HomeSite homeSite = mData.get(i);
                    homeSite.setOrder(i);
                }
                try {
                    siteManager.reset(mData);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                ThreadManager.postTaskToUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(View.GONE);
                        mMainView.setVisibility(View.VISIBLE);
                        mMainView.scrollTo(0,0);
                    }
                });
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_edit_logo_complete:
                hide();

                break;
            case R.id.common_img_back:
                onBackPressed();
                break;
        }
    }

    private void slideGridView(final float pos) {
        final int marginTop = DensityUtil.dip2px(getContext(), MARGIN_TOP_DP);
        final int marginleft = DensityUtil.dip2px(getContext(), MARGIN_LEFT_DP);
        final int marginright = DensityUtil.dip2px(getContext(),
                MARGIN_RIGHT_DP);
        int titleHeight = DensityUtil.dip2px(getContext(), TITLE_BAR_HEIGHT_DP)
                + AppEnv.STATUS_BAR_HEIGHT;
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0,
                mIsReduceHeight ? marginTop - pos + titleHeight : marginTop
                        - pos);
        animation.setDuration(300);
        animation.setStartOffset(0);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mGridView.clearAnimation();
                LayoutParams params = (LayoutParams) mGridView
                        .getLayoutParams();
                params.setMargins(marginleft, marginTop, marginright, 0);
                mGridView.setLayoutParams(params);
            }
        });
        mGridView.startAnimation(animation);
        mMainView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLongClickUp() {
        if (!mIsInit) {
            init();
        }
        slideGridView(mHeight);
    }

    public void onBackPressed() {
        hide();
    }

    public void onHind() {
        if (mGridView != null) {
            mGridView.setVisibility(View.VISIBLE);
        }
        if (mMainView != null) {
            mMainView.setVisibility(View.VISIBLE);
        }
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onSyncHomeSite(SyncHomeSiteEvent syncHomeSiteEvent) {
//        refreshData();
//    }
}
