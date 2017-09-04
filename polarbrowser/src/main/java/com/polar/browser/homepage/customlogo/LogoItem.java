package com.polar.browser.homepage.customlogo;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.custom_logo.DrawBitmapUtils;
import com.polar.browser.custom_logo.LogoBitmapUtils;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.push.fbnotify.FbNotifyManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.StringUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.events.BottomMenuNotifyBrowserEvent;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.util.ImageLoadUtils;
import com.polar.browser.view.ObservableScrollView;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LogoItem extends RelativeLayout implements android.view.View.OnClickListener, OnLongClickListener {

    private HomeSite homeSite;
    /**
     * 图标
     **/
    private ImageView sitePicImageView;
    /**
     * 标题
     **/
    private TextView siteNameTextView;

    private IEditLogo mEditLogoDelegate;
    private ObservableScrollView mScrollView;
    private HomeLogoView mHomeLogoView;
    private TextView mFacebookDot;

    public LogoItem(Context context) {
        this(context, null);
    }

    public LogoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item_logo_home, this);
        sitePicImageView = (ImageView) findViewById(R.id.site_pic);
        siteNameTextView = (TextView) findViewById(R.id.site_name);
        mFacebookDot = (TextView) findViewById(R.id.face_book_dot);

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void bind(HomeSite homeSite) {
        this.homeSite = homeSite;
        if (this.homeSite == null) {
            return;
        }
        if (!TextUtils.isEmpty(homeSite.getSiteAddr())
                && homeSite.getSiteAddr().contains(FbNotifyManager.FB_URL_PREFIX)) {
            int fbNotifyMsgNumber = ConfigManager.getInstance().getFbNotifyMsgNumber();
            if (0 == fbNotifyMsgNumber || fbNotifyMsgNumber<0) {
                mFacebookDot.setVisibility(View.GONE);
            } else {
                if (fbNotifyMsgNumber > 9) {
                    mFacebookDot.setText("9+");
                } else {
                    mFacebookDot.setText(String.valueOf(fbNotifyMsgNumber));
                }
                mFacebookDot.setVisibility(View.VISIBLE);
            }

        } else {
            mFacebookDot.setVisibility(View.GONE);
        }
        String siteName = this.homeSite.getSiteName();
        if (!TextUtils.isEmpty(siteName)) {
            if (HomeSiteUtil.SITE_ID_MORE.equals(homeSite.getSiteId())) {
                //homelogo 引导多语言
                siteNameTextView.setText(JuziApp.getInstance().getString(R.string.home_logo_more));
            } else {
                siteNameTextView.setText(siteName);
            }
        } else {
            siteNameTextView.setText("");
        }
        String sitePic = this.homeSite.getSitePic();
        String format = String.format("%s/%s", JuziApp.getAppContext().getFilesDir().toString(), CommonData.ICON_DIR_NAME);
        if (TextUtils.isEmpty(sitePic)) {
//            ImageLoadUtils.loadImage(getContext(),R.drawable.default_shortcut,sitePicImageView,R.drawable.logo_click,R.drawable.default_shortcut);
//            ImageLoadUtils.loadEllipseImage(getContext(), R.drawable.default_shortcut, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
            processCustomLogo(siteName, this.homeSite.getSiteAddr());
        } else {
            if (sitePic.contains(format)) {
                processCustomLogo(siteName, this.homeSite.getSiteAddr());
//                ImageLoadUtils.loadImage(getContext(),R.drawable.default_shortcut,sitePicImageView,R.drawable.logo_click,R.drawable.default_shortcut);
            } else {
//                ImageLoadUtils.loadImage(getContext(),sitePic,sitePicImageView,R.drawable.logo_click,R.drawable.default_shortcut);
                ImageLoadUtils.loadEllipseImage(getContext(), sitePic, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
            }
        }
    }

    private void processCustomLogo(final String siteName, final String url) {
        //创建一个上游 Observable：
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                Bitmap bitmapLogo = null;
                String name = "";
                String firstText = StringUtils.getFirstChar(siteName);
                String host = UrlUtils.getHost(url);
                if (TextUtils.isEmpty(host)) {
                    name = SecurityUtil.getMD5(url)+firstText;
                }else {
                    name = host + firstText;
                }
                bitmapLogo = FileUtils.getBitmapFromFile(LogoBitmapUtils.getLogoPathByName(name));
                if (bitmapLogo == null) {
                    bitmapLogo= DrawBitmapUtils.drawBitmapDepenRGB(StringUtils.getFirstChar(siteName));
                    //保存
                    LogoBitmapUtils.saveLogoBitmapToFile(CommonData.LOGO_DIR_NAME, url,
                            firstText , bitmapLogo);
                }
                emitter.onNext(bitmapLogo);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmapLogo) throws Exception {
                        if (bitmapLogo != null) {
                            sitePicImageView.setImageBitmap(bitmapLogo);
                        }else {
                            ImageLoadUtils.loadEllipseImage(getContext(), R.drawable.default_shortcut, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
                        }
                    }
                });
    }


    public void setDelegate(ObservableScrollView scrollView, HomeLogoView homeLogoView) {
        this.mScrollView = scrollView;
        this.mHomeLogoView = homeLogoView;
    }

    public void setEditLogoDelegate(IEditLogo editLogoDelegate) {
        this.mEditLogoDelegate = editLogoDelegate;
   }

    @Override
    public void onClick(View v) {

        if (TextUtils.equals(HomeSiteUtil.SITE_ID_ADD,homeSite.getSiteId())) {
            int[] location = new int[2];
            mHomeLogoView.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
            int gridTop = location[1];
            mScrollView.getLocationOnScreen(location);
            Statistics.sendOnceStatistics(GoogleConfigDefine.COMMON_URLS, GoogleConfigDefine.CLICK_LOGO_ADD);
            mEditLogoDelegate.openEditLogoView(gridTop - location[1], location[1], mScrollView, true);
        } else if (TextUtils.equals(HomeSiteUtil.SITE_ID_MORE,homeSite.getSiteId())) {
            TabViewManager.getInstance().loadUrl(Statistics.getLoadMoreUrl(), Constants.NAVIGATESOURCE_HOME_MORE);
        } else if (TextUtils.equals(HomeSiteUtil.SITE_HISTORY_OPEN_ADDR, homeSite.getSiteAddr())) {
            //打开"历史/书签"页面
            EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.BOOKMARK_HISTORY));
        } else {
            TabViewManager.getInstance().loadUrl(homeSite.getSiteAddr(), Constants.NAVIGATESOURCE_HOME_SITE);
            Statistics.sendOnceStatistics(GoogleConfigDefine.LOAD_URL, GoogleConfigDefine.OPEN_LOGO_URL, homeSite.getSiteName());
        }
        if (!TextUtils.isEmpty(homeSite.getSiteAddr())
                && homeSite.getSiteAddr().contains(FbNotifyManager.FB_URL_PREFIX)) {
            mFacebookDot.setVisibility(View.GONE);
            ConfigManager.getInstance().setFbNotifyMsgNumber(0);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int[] location = new int[2];
        mHomeLogoView.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
        int gridTop = location[1];
        mScrollView.getLocationOnScreen(location);
        mEditLogoDelegate.openEditLogoView(gridTop, location[1], mScrollView, false);
        mHomeLogoView.setIsLogoLongClick(true);
        return true;
    }
}
