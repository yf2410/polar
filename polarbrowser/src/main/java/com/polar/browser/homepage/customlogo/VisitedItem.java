package com.polar.browser.homepage.customlogo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.custom_logo.DrawBitmapUtils;
import com.polar.browser.custom_logo.LogoBitmapUtils;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.homepage.sitelist.recommand.exception.HomeSiteExistException;
import com.polar.browser.homepage.sitelist.recommand.exception.OutOfMaxNumberException;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.StringUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.util.ImageLoadUtils;
import com.polar.browser.view.ObservableScrollView;

import java.sql.SQLException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VisitedItem extends RelativeLayout implements View.OnClickListener, OnLongClickListener {

    private HistoryRecord mHistoryRecord;
    /**
     * 图标
     **/
    private ImageView sitePicImageView;
    /**
     * 标题
     **/
    private TextView siteNameTextView;

    private ObservableScrollView mScrollView;
    private HomeVisitedView mHomeVisitedView;
    private int mPostion;

    public VisitedItem(Context context) {
        this(context, null);
    }

    public VisitedItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item_visited_home, this);
        sitePicImageView = (ImageView) findViewById(R.id.site_pic);
        siteNameTextView = (TextView) findViewById(R.id.site_name);

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void bind(HistoryRecord historyRecord) {
        this.mHistoryRecord = historyRecord;
        if (this.mHistoryRecord == null) {
            return;
        }
        siteNameTextView.setText(mHistoryRecord.getHistoryTitle());

        processCustomLogo(mHistoryRecord.getHistoryTitle(),mHistoryRecord.getHistoryAddr());


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


    public void setDelegate(ObservableScrollView scrollView, HomeVisitedView homeVisitedView, int postion) {
        this.mScrollView = scrollView;
        this.mHomeVisitedView = homeVisitedView;
        this.mPostion = postion;
    }

    @Override
    public void onClick(View v) {

        if (mHistoryRecord != null && !TextUtils.isEmpty(mHistoryRecord.getHistoryAddr())) {
            TabViewManager.getInstance().loadUrl(mHistoryRecord.getHistoryAddr(), Constants.NAVIGATESOURCE_HOME_SITE);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        showPopWindow(v);
        return true;
    }

    private PopupWindow popWindow;
    public void showPopWindow(View v) {
        if(popWindow==null){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.home_visited_popup, null);
            TextView item1Tv = (TextView) view.findViewById(R.id.popup_item1);
            TextView item2Tv = (TextView) view.findViewById(R.id.popup_item2);
            item1Tv.setText(R.string.visited_add_home);
            item2Tv.setText(R.string.delete);
            view.findViewById(R.id.lin_popup_item1).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    popWindow.dismiss();
                    Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY_VISITED, GoogleConfigDefine.VISITED_ADD_HOME);
                    String iconPath = String.format("%s/%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
                            CommonData.ICON_DIR_NAME, UrlUtils.getHost(mHistoryRecord.getHistoryAddr()));
                    try {
                        if (SiteManager.getInstance().add2Home(new Site(
                                mHistoryRecord.getHistoryTitle(), mHistoryRecord.getHistoryAddr(), iconPath),false)) {
                            mHistoryRecord.setCount(0);
                            SiteManager.getInstance().getHistoryRecords().remove(mPostion);
                            HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).updateAllHistoryRecord(mHistoryRecord);
                            mHomeVisitedView.resetData(SiteManager.getInstance().getHistoryRecords());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (OutOfMaxNumberException e) {
                        e.printStackTrace();
                        CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_max_tip);
                    } catch (HomeSiteExistException e) {
                        e.printStackTrace();
                        CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
                    }
                }
            });
            view.findViewById(R.id.lin_popup_item2).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v){
                    popWindow.dismiss();
                    Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY_VISITED, GoogleConfigDefine.VISITED_DEL);
                    try {
                        mHistoryRecord.setCount(0);
                        SiteManager.getInstance().getHistoryRecords().remove(mPostion);
                        HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).updateAllHistoryRecord(mHistoryRecord);
//                                HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).deleteHistoryRecordByAddr(mHistoryRecord.getHistoryAddr());
                        mHomeVisitedView.resetData(SiteManager.getInstance().getHistoryRecords());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            int menuWidth = DensityUtil.dip2px(getContext(), 180);
            popWindow = new PopupWindow(view, menuWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popWindow.setOutsideTouchable(false);
            popWindow.setBackgroundDrawable(new BitmapDrawable());
//            popWindow.setAnimationStyle(R.style.pop);
        }
//        popWindow.showAsDropDown(v,0,0);

        int[] location = new int[2];
        v.getLocationOnScreen(location);
        popWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] + 80 - v.getHeight()*2);
//        popWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - DensityUtil.dip2px(getContext(), v.getHeight()));
//        popWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0] - (popWindow.getWidth() - v.getWidth()) / 2, location[1] - popWindow.getHeight());
    }
}
