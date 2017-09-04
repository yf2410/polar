package com.polar.browser.homepage.sitelist.recommand;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.homepage.sitelist.recommand.exception.HomeSiteExistException;
import com.polar.browser.homepage.sitelist.recommand.exception.OutOfMaxNumberException;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.push.loadWebDetailsActivity;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.events.SyncSiteItemHideEvent;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

import org.greenrobot.eventbus.EventBus;

import java.sql.SQLException;

public class RecommendItem extends RelativeLayout implements
        android.view.View.OnClickListener {

    private String TAG = "RecommendItem";
    /**
     * 标题
     **/
    private TextView mTvTitle;
    /** 描述 **/
//	private TextView mTvDesc;
    /**
     * 网址icon
     **/
    private ImageView mImage;
    /**
     * 添加
     **/
    private ImageView mBtnAdd;

    /**
     * 是否添加了该条目
     **/
    private boolean isAdded;
    private Site site;
    private String mSiteName;


    public RecommendItem(Context context) {
        this(context, null);
    }

    public RecommendItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item_recommend, this);
        setId(R.id.item_recommend_root);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mBtnAdd = (ImageView) findViewById(R.id.btn_add);
        mImage = (ImageView) findViewById(R.id.iv_icon);
        setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        findViewById(R.id.rl_btn).setOnClickListener(null);
    }

    private int prePosition;

    public void bind(Site site, int position) {
        prePosition = position;
        this.site = site;
        mSiteName = site.getSiteName();
        if (!TextUtils.isEmpty(mSiteName)) {
            this.mTvTitle.setText(mSiteName);
        } else {
            this.mTvTitle.setText("");
        }
        String sitePic = site.getSitePic();
        String format = String.format("%s/%s", JuziApp.getAppContext().getFilesDir().toString(), CommonData.ICON_DIR_NAME);
        if (!TextUtils.isEmpty(sitePic) && sitePic.contains(format)) {
            ViewGroup.LayoutParams layoutParams = mImage.getLayoutParams();
            layoutParams.width = DensityUtil.dip2px(getContext(), 20);
            layoutParams.height = DensityUtil.dip2px(getContext(), 20);
        }
        ImageLoadUtils.loadEllipseImage(getContext(), sitePic, mImage, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
        // TODO: 2016/9/19  更新图标状态
        try {
            if (SiteManager.getInstance().exist(site)) {
//                mBtnAdd.setImageResource(R.drawable.btn_open);
                mBtnAdd.setVisibility(View.GONE);
                isAdded = true;
            } else {
                mBtnAdd.setVisibility(View.VISIBLE);
                mBtnAdd.setImageResource(R.drawable.btn_add);
                isAdded = false;
            }
        } catch (SQLException e) {
            SimpleLog.e(e);
            mBtnAdd.setVisibility(View.VISIBLE);
            mBtnAdd.setImageResource(R.drawable.btn_add);
            isAdded = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick())
            return;
        switch (v.getId()) {
            case R.id.btn_add:
                try {
                    if (SiteManager.getInstance().add2Home(site, false)) {
                        if (!TextUtils.isEmpty(mSiteName)) {
                            CustomToastUtils.getInstance().showTextToast(getResources().getString(R.string.add_home_logo,mSiteName));
                        }
                        mBtnAdd.setVisibility(View.GONE);
                        EventBus.getDefault().post(new SyncSiteItemHideEvent(prePosition));
//                        mBtnAdd.setImageResource(R.drawable.btn_open);
                        Statistics.sendOnceStatistics(GoogleConfigDefine.NAV_ID, String.valueOf(site.getSiteName()));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // TODO: 2016/10/14 提示用户操作失败
                } catch (OutOfMaxNumberException e) {
                    e.printStackTrace();
                    CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_max_tip);
                } catch (HomeSiteExistException e) {
                    e.printStackTrace();
                    CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
                }
                //检查首页logo是否有改动
                ConfigManager.getInstance().setCheckModifiedHomeSite(true);
                break;
            case R.id.item_recommend_root:
                if (site != null && TextUtils.equals(HomeSiteUtil.SITE_HISTORY_OPEN_ADDR, site.getSiteAddr())) {
                    //历史logo，不可点击
                    return;
                }
                openPreviewPage();
                break;
            default:
                break;
        }
    }

    private void openPreviewPage() {
        Intent intent = new Intent(getContext(), loadWebDetailsActivity.class);
        intent.setAction(CommonData.ACTION_OPEN_RECOMMEND_DATA);
        intent.putExtra(CommonData.SYSTEM_CONTENT_URL, site.getSiteAddr());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}
