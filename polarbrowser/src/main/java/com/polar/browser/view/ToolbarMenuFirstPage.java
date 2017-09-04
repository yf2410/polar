package com.polar.browser.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.SettingSyncManager;
import com.polar.browser.sync.UserHomeSiteManager;
import com.polar.browser.vclibrary.bean.events.BottomMenuNotifyBrowserEvent;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by saifei on 17/3/16.
 * 菜单栏第一页，目前两页
 * 当前页内容：
 * 添加收藏，历史/收藏夹,下载,保存网页，PC模式，无图模式，设置，退出
 */

public class ToolbarMenuFirstPage extends LinearLayout implements View.OnClickListener {

    private TextView btnAddBookmark;
    private TextView btnBookmarkHistory;
    private TextView btnSaveWeb;
    private TextView btPcmode;
    private TextView btNoImage;
    private TextView btSetting;
    private TextView btQuit;

    private View mDot;
    private View rlDownload;

    public ToolbarMenuFirstPage(Context context) {
        this(context, null);
    }

    public ToolbarMenuFirstPage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.toolbar_menu_first_page, this);
        setOrientation(LinearLayout.VERTICAL);
        initViews();
        initListeners();


    }

    private void initListeners() {
        btnAddBookmark.setOnClickListener(this);
        btnBookmarkHistory.setOnClickListener(this);
        rlDownload.setOnClickListener(this);
        btnSaveWeb.setOnClickListener(this);

        btPcmode.setOnClickListener(this);
        btNoImage.setOnClickListener(this);
        btSetting.setOnClickListener(this);
        btQuit.setOnClickListener(this);

    }

    private void initViews() {
        btnAddBookmark = (TextView) findViewById(R.id.btn_add_bookmark);
        btnBookmarkHistory = (TextView) findViewById(R.id.btn_favorite_history);
        rlDownload = findViewById(R.id.rl_download);
        btnSaveWeb = (TextView) findViewById(R.id.btn_save_web);

        btPcmode = (TextView) findViewById(R.id.btn_pcmode);
        btNoImage = (TextView) findViewById(R.id.btn_no_img);
        btSetting = (TextView) findViewById(R.id.btn_setting);
        btQuit = (TextView) findViewById(R.id.btn_quit);

        mDot = findViewById(R.id.dot);

    }

    @Override
    public void onClick(View v) {
        String type = "";
        switch (v.getId()) {
            case R.id.btn_add_bookmark:
                if (!TabViewManager.getInstance().isCurrentHome()){
                    EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.ADD_BOOKMARK));
                    type = GoogleConfigDefine.MENU_ADD_BOOKMARK;
                }
                break;
            case R.id.btn_favorite_history:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.BOOKMARK_HISTORY));
                type = GoogleConfigDefine.MENU_HISTORY_MARK;
                break;
            case R.id.rl_download:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.DOWNLOAD_MANAGE));
                type = GoogleConfigDefine.MENU_DOWNLOAD_MANAGER;
                break;
            case R.id.btn_save_web:
                if (!TabViewManager.getInstance().isCurrentHome()){
                    EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.SAVE_WEB_PAGE));
                    type = GoogleConfigDefine.MENU_SAVE_PAGE;
                }

                break;
            case R.id.btn_pcmode:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.PC_MODE));
                break;
            case R.id.btn_no_img:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.NO_IMAGE));
                break;
            case R.id.btn_setting:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.SETTING));
                type = GoogleConfigDefine.MENU_SETTING;
                break;
            case R.id.btn_quit:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.EXIT));
                type = GoogleConfigDefine.MENU_EXIT;
                BookmarkManager.getInstance().syncBookmark((RxFragmentActivity) getContext(),false);
                //UserHomeSiteManager.getInstance().syncHomeSite(UserHomeSiteManager.SYNC_TYPE_EXIT);
                SettingSyncManager.getInstance().syncSetting(SettingSyncManager.SYNC_TYPE_EXIT);
                if (AccountLoginManager.getInstance().isUserLogined()){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC, GoogleConfigDefine.BOOKMARK_AUTO_SYNC,GoogleConfigDefine.BOOKMARK_EXIT_BROWSER_SYNC);
                }
                break;
        }
        sendGoogleStatistics(type);
    }


    public void refreshBtnNoImgUI(boolean isEnabled) {
        if (isEnabled) {
            btNoImage.setTextColor(getResources().getColor(R.color.black87));
            btNoImage.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_noimage_off, 0, 0);
            Statistics.sendOnceStatistics(GoogleConfigDefine.MENU_EVENTS,GoogleConfigDefine.MENU_NO_PIC);
        } else {
            //btNoImage.setTextColor(getResources().getColor(R.color.theme_green));
            btNoImage.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_noimage_on, 0, 0);
        }
    }


    public void refreshBtnWebUI() {
        int uaType = ConfigManager.getInstance().getUaType();
        if (uaType == ConfigDefine.UA_TYPE_PC) {
            //btPcmode.setTextColor(getResources().getColor(R.color.theme_green));
            btPcmode.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.web_on, 0, 0);
        } else {
            btPcmode.setTextColor(getResources().getColor(R.color.black87));
            btPcmode.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.web, 0, 0);
        }
    }

    public void changeBtnWebUaType(int value) {
        if (value == ConfigDefine.UA_TYPE_PC) {
            btPcmode.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.web_on, 0, 0);
        } else {
            btPcmode.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.web, 0, 0);
        }
    }

    public void refreshAddBookmarkUI() {
        setTextViewBg(btnAddBookmark, R.drawable.addbookmarks,
                R.drawable.addbookmarks_disabled);
    }

    public void refreshSaveWebUI() { //TODO 需要替换 不能点击图片
        setTextViewBg(btnSaveWeb,
                R.drawable.menu_web, R.drawable.menu_web_disable);
    }

    private void setTextViewBg(TextView tv, int enableId, int disableId) {
        boolean isCurrentHome = TabViewManager.getInstance().isCurrentHome();
        tv.setCompoundDrawablesWithIntrinsicBounds(0, isCurrentHome ? disableId : enableId
                , 0, 0);
        tv.setBackgroundResource(isCurrentHome ? R.color.transparent : R.drawable.menu_item_selector);
        tv.setTextColor(isCurrentHome? Color.GRAY:getResources().getColor(R.color.black87));
    }

    private void sendGoogleStatistics(String type) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.MENU_EVENTS, type);
    }

    public void refreshDownloadUI() {

        if (ConfigManager.getInstance().isHasDownload()) {
            mDot.setVisibility(View.VISIBLE);
        } else {
            mDot.setVisibility(View.GONE);
        }

    }
}
