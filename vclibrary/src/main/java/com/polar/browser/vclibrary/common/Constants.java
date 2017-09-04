package com.polar.browser.vclibrary.common;

/**
 * Created by FKQ on 2016/9/20.
 */

public interface Constants {

    /** 国家 */
    String DEFAULT_COUNTRY = "default";
    String AREA_IN = "IN";//印度
    String AREA_BR = "BR";//巴西
    String AREA_CL = "CL";//智利
    String AREA_CN = "CN";//中国大陆
    String AREA_US = "US";//美国
    String AREA_ID = "ID";//印尼
    String AREA_RU = "RU";//俄罗斯
    String AREA_EG = "EG";//埃及
    String AREA_TH = "TH";//泰国
    String AREA_TR = "TR";//土耳其
    String AREA_IR = "IR";//伊朗
    String AREA_PK = "PK";//巴基斯坦

    String NATION_IN = "in";
    String NATION_BR = "br";
    String NATION_CL = "cl";
    String NATION_CN = "cn";
    String NATION_US = "us";
    String NATION_ID = "id";
    String NATION_RU = "ru";
    String NATION_EG = "eg";
    String NATION_TH = "th";
    String NATION_TR = "tr";
    String NATION_IR = "ir";
    String NATION_PK = "pk";

    /** 语言 */
    String LAN_ZH = "zh";//简体中文
    String LAN_EN = "en";//英语
    String LAN_PT = "pt";//葡萄牙语
    String LAN_ES = "es";//西班牙语
    String LAN_IN = "in";//印尼语
    String LAN_RU = "ru";//俄语
    String LAN_TH = "th";//泰语
    String LAN_AR = "ar";//阿拉伯语
    String LAN_HI = "hi";//印地语
    String LAN_FR = "fr";//法语
    String LAN_TR = "tr";//土耳其语

    String SET_OTHER = "set_other";
    String SET_NULL = "set_null";

    /** MCC */
    String DEFAULT_MCC = "000";
    //阿拉伯
    String AE_MCC_424 = "424";
    String AE_MCC_430 = "430";
    String AE_MCC_431 = "431";
    //美国
    String US_MCC_310 = "310";
    String US_MCC_311 = "311";
    String US_MCC_312 = "312";
    String US_MCC_313 = "313";
    String US_MCC_314 = "314";
    String US_MCC_315 = "315";
    String US_MCC_316 = "316";
    String US_MCC_330 = "330";
    String US_MCC_332 = "332";
    String US_MCC_534 = "534";
    String US_MCC_535 = "535";
    String US_MCC_544 = "544";
    //巴西
    String BR_MCC_724 = "724";
    //智利
    String CL_MCC_730 = "730";
    //印度
    String IN_MCC_404 = "404";
    String IN_MCC_405 = "405";
    String IN_MCC_406 = "406";
    //印度尼西亚
    String ID_MCC_510 = "510";
    //俄罗斯
    String RU_MCC_250 = "250";
    //中国大陆
    String CN_MCC_460 = "460";
    String CN_MCC_461 = "461";
    //埃及
    String EG_MCC_602 = "602";
    //泰国
    String TH_MCC_520 = "520";
    //土耳其
    String TR_MCC_286 = "286";
    //伊朗
    String IR_MCC_432 = "432";
    //巴基斯坦
    String PK_MCC_432 = "410";

    /** 设置open——url（Source） */
    int TYPE_FROM_DEFAULT = 0;
    int TYPE_FROM_BOOKMARK = 1;
    int TYPE_FROM_ADDR = 2;
    int TYPE_FROM_HISTORY = 3;
    int TYPE_FROM_SHORTCUT = 4;
    int TYPE_FROM_SEARCH = 5;
    int TYPE_FROM_NOTIFICATION = 6;
    int TYPE_FROM_MOBONUS = 7;
    int TYPE_FROM_FCM_NOTIF = 8;
    int TYPE_FROM_WIFIGUARDER = 9;
    int TYPE_FROM_PRODUCT_ABOUT = 10;
    int TYPE_FROM_DESKTOP_LAUNCHER = 11;

    /** 设置webview-loadUrl（Source）类别 */
    int NAVIGATESOURCE_NORMAL = 0;
    int NAVIGATESOURCE_ADDR = 1;
    int NAVIGATESOURCE_BOOKMARK = 2;
    int NAVIGATESOURCE_HISTORY = 3;
    int NAVIGATESOURCE_OTHER = 4;
    int NAVIGATESOURCE_SEARCH = 5;
    int NAVIGATESOURCE_NOTIFICATION = 6;
    int NAVIGATESOURCE_MOBONUS = 7;
    int NAVIGATESOURCE_NEWSCARD_ITEM = 8;
    int NAVIGATESOURCE_NEWSCARD_MORE = 9;
    int NAVIGATESOURCE_NEWSCARD_BANNER = 10;
    int NAVIGATESOURCE_HOME_SITE = 11;
    int NAVIGATESOURCE_HOME_MORE = 12;
    int NAVIGATESOURCE_PRODUCT_ABOUT = 13;

    //首页卡片模块
    /** 新闻卡片 */
    String CARD_NEWS_TYPE = "1";
    String NEWS_TYPE = "article";
    String NEWS_NUM = "12";
    /** 新闻卡片Banner */
    String CARD_NEWS_BANNER_TYPE = "2";

    /** 卡片开关-开 */
    String CARD_SWITCH_STATUS_OPEN = "1";
    /** 卡片开关-关 */
    String CARD_SWITCH_STATUS_CLOSE = "0";

    // adjust 统计报活参数
    /** 报活开关关闭 */
    String ADJUST_CLOSE = "close";

    /** 报活设为四小时 */
    String ADJUST_FOUR = "four";
    long ADJUST_FOUR_TIME = 14400000L;

    /** 报活设为八小时 */
    String ADJUST_EIGHT = "eight";
    long ADJUST_EIGHT_TIME = 28800000L;

    /** 报活设为12小时 */
    String ADJUST_TWELVE = "twelve";
    long ADJUST_TWELVE_TIME = 43200000L;

    /**
     * adjust报活-setAction
     **/
    String ADJUST_LIFE_ACTION = "com.polar.browser.action.adjust.life";

    /** 首页新闻链接标识-自定义 */
    String CARD_NEWS_URLFLAG_CUSTOM = "1";
    /** 首页新闻链接标识-默认 */
    String CARD_NEWS_URLFLAG_DEFAULT = "0";

//    String FB_DEEDLINK_CUSTOM_URL = "http://172.17.247.47:8080/juzi/vc/client/api/fbDplink?";
    String FB_DEEDLINK_CUSTOM_URL = "http://api.vcbrowser.com/juzi//vc/client/api/fbDplink?";

    int GOBROWSERTYPE_INIT = 1;
    int GOBROWSERTYPE_AD = 2;

    /**
     * 用于统计url和sk的文件名
     */
    String URLS_FILE_NAME = "asdfghjkl";
    String SK_FILE_NAME = "skgadskfala";

    String JS_IMG_NAME = "js_share_img.jpg";
    String SHARE_AD_BLOCK_IMG_NAME = "ad_block_share_img.jpg";
    String URL_UPLOAD_KEY = "url";
    String SK_UPLOAD_KEY = String.valueOf(System.currentTimeMillis());
    long ONE_DAY_TIMEMILLIS = 86400000;
}
