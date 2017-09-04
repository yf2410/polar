package com.polar.browser.homepage;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.UserHomeSite;
import com.polar.browser.vclibrary.util.Util;

import java.util.List;

/**
 * Created by James on 2016/10/9.
 */

public class HomeSiteUtil {


    public static final String SITE_ID_ADD = "SiteIdAdd";
    public static final String SITE_ID_MORE = "SiteIdMore";
    public static final String SITE_NAME_MORE = "More";
    public static final String SITE_NAME_ADD = "Add";
    public static final String SITE_ADDR_ADD = "www.add.com";
    /**打开"历史/书签"快捷方式logo 地址*/
    public static final String SITE_HISTORY_OPEN_ADDR = "###History";


    /**
     * add more, 增加更多,可以跳转到推荐列表,
     *
     * @return
     */
    public static HomeSite getAddHomeSite(long order) {
        HomeSite homeSite = new HomeSite(SITE_ID_ADD, null, null, "file:///android_asset/add.png", order, false);
        return homeSite;
    }

    public static UserHomeSite getAddUserHomeSite(long order) {
        UserHomeSite userHomeSite = new UserHomeSite(SITE_ID_ADD, null, null, "file:///android_asset/add.png", order, false);
        return userHomeSite;
    }

    /**
     * more, 浏览更多网站
     *
     * @return
     */
    public static HomeSite getMoreHomeSite(long order) {
        HomeSite homeSite = new HomeSite(SITE_ID_MORE, JuziApp.getInstance().getString(R.string.home_logo_more), null, "file:///android_asset/more.png", order, false);
        return homeSite;
    }

    public boolean exist(Site site, List<HomeSite> homeSites) {
        if (Util.isCollectionEmpty(homeSites)) {
            return false;
        }
        //优先比较siteId,其次比较siteAddr
        String siteId = site.getSiteId();
        String siteAddr = site.getSiteAddr();
        for (HomeSite homeSite : homeSites) {
            if (Util.equals(homeSite.getSiteId(), siteId) || Util.equals(homeSite.getSiteAddr(), siteAddr)) {
                return true;
            }
        }
        return false;
    }
}
