package com.polar.browser.vclibrary.bean.events;

/**
 * Created by saifei on 17/3/17.
 * 底部菜单栏 点击事件 通知  BrowserActivity 事件
 */

public class BottomMenuNotifyBrowserEvent {
    private int menu;

    public BottomMenuNotifyBrowserEvent(int menu) {
        this.menu = menu;
    }

    public int getMenu() {
        return menu;
    }

    public interface Menu{
        int ADD_BOOKMARK = 1;
        int BOOKMARK_HISTORY = 2;
        int DOWNLOAD_MANAGE = 3;
        int SAVE_WEB_PAGE = 4;
        int PC_MODE = 5;
        int NO_IMAGE = 6;
        int SETTING = 7;
        int EXIT = 8;
        int SCREEN_SHOT = 9;
        int FIND_IN_PAGE = 10;
        int SHARE = 11;
        int NIGHT_MODE = 12;
        int FULL_SCREEN = 13;
        int CLEAN_DATA = 14;
        int OPEN_SYSTEM_NEWS = 15;
        int AD_BLOCK = 16;
        int FONT_SIZE = 17;

    }


}
