package com.polar.browser.common.data;

import com.polar.browser.env.AppEnv;
import com.polar.browser.vclibrary.network.api.ApiConstants;

public class CommonData {
	public static final String ICON_DIR_NAME = "icon";
	public static final String LOGO_DIR_NAME = "logo_dir";
	public static final String SUB_IMAGE_PATH = "sub_image";
	/** -----action------ **/

	/**
	 * 从其他界面跳转到主界面时需要打开的url
	 */
	public static final String ACTION_GOTO_URL = "com.polar.browser.BrowserActivity.goto";
	/**
	 * 是从什么渠道跳转的 TYPE_FROM_XXX:渠道的类型
	 */
	public static final String ACTION_TYPE_FROM = "com.polar.browser.BrowserActivity.type.from";
	public static final String ACTION_LOAD_SAVED_PAGES = "com.polar.browser.BrowserActivity.load.savedpage";
	public static final String EXTRA_LOAD_SAVED_PAGES_DATA = "com.polar.browser.BrowserActivity.type.load.savedpage.data";
	/**
	 * 下载列表刷新
	 **/
	public static final String DOWNLOAD_UPDATE_LIST = "com.polar.browser.download_update_list";
	/**
	 * 网络连接——WIFI
	 **/
	public static final String NETWORK_WIFI_CONNECT = "com.polar.browser.network_wifi_connect";
	/**
	 * 网络连接——GPRS
	 **/
	public static final String NETWORK_GPRS_CONNECT = "com.polar.browser.network_gprs_connect";
	/**
	 * 网络连接——断开
	 **/
	public static final String NETWORK_UN_CONNECT = "com.polar.browser.network_un_connect";
	/**
	 * 是否有下载任务（菜单页的下载icon显示相关）
	 **/
	public static final String ACTION_HAS_DOWNLOADING_TASK = "com.polar.browser.action_has_downloading_task";
	/**
	 * 通知下载增加统计
	 **/
	public static final String ACTION_DOWNLOAD_ADD_STATISTICS = "com.polar.browser.action_add_download_statistics";

	/**
	 * Firebase 通知相关参数变量
	 */
	public static final String ACTION_CLICK_NOTIFICATION = "com.polar.browser.action_click_notification";

	/**
	 * facebook 登录成功
	 */
	public static final String ACTION_LOGIN_SUCCESS_TIP = "com.polar.browser.action_login_success_tip";

	/**
	 * 跨进程---夜间模式改变，发送广播
	 **/
	public static final String ACTION_NIGHT_MODE_CHANGED = "com.polar.browser.ACTION_NIGHT_MODE_CHANGED";

	/**
	 * 跨进程---竖屏锁定，发送广播
	 **/
	public static final String ACTION_SCREEN_LOCKED = "com.polar.browser.ACTION_SCREEN_LOCKED";

	/**
	 * 跨进程---全屏，发送广播
	 **/
	public static final String ACTION_FULL_SCREEN_CHANGED = "com.polar.browser.ACTION_FULL_SCREEN_CHANGED";

	/**
	 * 跨进程---自定义下载文件夹路径，发送广播
	 **/
	public static final String ACTION_DOWNLOAD_FOLDER_CHANGED = "com.polar.browser.ACTION_DOWNLOAD_FOLDER_CHANGED";

	/**
	 * 跨进程---仅在wifi下下载开关改变，发送广播
	 **/
	public static final String ACTION_DOWNLOAD_ONLY_WIFI = "com.polar.browser.ACTION_DOWNLOAD_ONLY_WIFI";

	/**
	 * 网络状态改变action
	 **/
	public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

	/**
	 * 视频播放页面亮度
	 **/
	public static final String KEY_BRIGHTNESS = "key_brightness";

	/**
	 * userAgent
	 **/
	public static final String KEY_UA = "key_ua";
	/**
	 * 请求获取的最大字节数
	 **/
	public static final int QUERY_JSON_MAX_SIZE = 512 * 1024;

	public static final String MIME_TYPE_JPEG = "image/jpeg";
	/**
	 * 下载路径管理
	 **/
	public static final String KEY_DOWN_ROOT = "key_down_root";
	/**
	 * 下载路径类型：手机存储orSD卡
	 **/
	public static final String KEY_DOWN_TYPE = "key_down_type";

	/**
	 * 当前存储的下载路径
	 **/
	public static final String KEY_CURRENT_DOWN_FOLDER = "key_current_down_folder";
	/**
	 * 仅wifi下载,数据传递
	 **/
	public static final String KEY_ONLY_WIFI_DOWNLOAD = "key_only_wifi_download";
	/**
	 * 手机存储
	 **/
	public static final int DOWN_TYPE_PHONE = 0;

	/**
	 * SD卡
	 **/
	public static final int DOWN_TYPE_SD_CARD = 1;
	/**
	 * history_often 更新时间 （7天）604800000;
	 **/
	public static final long HISTORY_OFTEN_UPDATETIME = 604800000L;

	/**
	 * 通知搜索栏统计 时间 （1天）86400000;
	 **/
	public static final long NOTIFI_SEARCH_STATISTICAL = 30000L;
	//	分辨率：
//	sr=1 480*320
//	sr=2 800*480
//	sr=3 854*480
//	sr=4 960*540
//	sr=5 1184*720
//	sr=6 1280*720
//	sr=7 1280*800
//	sr=8 1920*1080
//	sr=9 2K以上
//	sr=10 其他
	public static final int SCREEN_RESOLUTION_480_320 = 1;
	public static final int SCREEN_RESOLUTION_800_480 = 2;
	public static final int SCREEN_RESOLUTION_854_480 = 3;
	public static final int SCREEN_RESOLUTION_960_540 = 4;
	public static final int SCREEN_RESOLUTION_1184_720 = 5;
	public static final int SCREEN_RESOLUTION_1280_720 = 6;
	public static final int SCREEN_RESOLUTION_1280_800 = 7;
	public static final int SCREEN_RESOLUTION_1920_1080 = 8;
	public static final int SCREEN_RESOLUTION_2K = 9;

	public static final int SCREEN_RESOLUTION_OTHER = 10;

	public static final float NIGHT_MODE_BRIGHTNESS = 0.1f;

	public static final String EXEC_JAVASCRIPT = "javascript:";

	public static final String SCREENSHOT_FILENAME = "screenshot.jpg";

	public static final String WELCOME_FILE = "welcome_file";
	public static final String UA_PC = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36";

	public static final String UA_IPHONE6 = "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.3 (KHTML, like Gecko) Version/8.0 Mobile/12A4345d Safari/600.1.4";
	public static final String KEY_PROCESS = "key_process";
	public static final String PROCESS_MAIN = "main";

	public static final String PROCESS_DOWNLOAD = "download";

	/**
	 * 用户类型，对自定义网址而言。1，不敏感； 2，敏感
	 **/
	public static final String USER_TYPE = "user_type";
	public static final int USER_TYPE_UNSENSITIVE = 1;

	public static final int USER_TYPE_SENSITIVE = 2;

	public static final String USER_ADD_LOGO_LIST = "user_add_logo_list";

	public static final int NAVIGATE_MORE_ID = 999999;

	//webview 加载网页不同阶段类型值
	public static final int WBLOADURL_STATUS_PAGESTART = 21;

	public static final int WBLOADURL_STATUS_PAGEFINSH = 22;

	//webview 加载网页不同阶段类型值(但进度条加载到85%时)
	public static final int WBLOADURL_STATUS_PROGRESS = 23;
	//历史、常访问记录变化通知类型
	public static final int DB_HISTORY = 24;

	public static final int DB_OFTENHISTORY = 25;

	//mobonus链接打开VC业务相关常量_mobonus在完成查看任务时，需要调用VC打开链接，此时会调起VC，并通过Intent传递相应参数
	//接收mobonus传来的intent-action
	public static final String ACTION_INTENT_MOBONUS_DATA = "com.polar.browser.action.loadlink.from.mobonus";

	public static final String ACTION_INTENT_WIFIGUARDER_DATA = "com.polar.browser.action.loadlink.from.wifiguarder";
	//打开外链
	public static final String MOBONUS_DATA_URL = "com.polar.browser.BrowserActivity.goto";
	//mobonus数据成功加载返回数据
	public static final String MOBONUS_LOADFINSH_BACKDATA = "com.polar.browser.page.finished";

	//VC海外版发送广播通知mobonus页面加载完成-setAction
	public static final String ACTION_MOBONUS_LOADLINK_FINSH = "com.mobonus.mobonus.action.load.page";

	public static final String SYSTEM_CONTENT_URL = "system_content_url";
	//点击通知快速搜索设置-setAction
	public static final String QUICK_SEARCH_SETTING = "gosetting";
	public static final String QUICK_SEARCH_SEARCH = "search";
	public static final int QUICK_SEARCH_ID = 250;
	//点击历史或书签item打开url-setAction
	public static final String OPEN_HISTORY_OR_BOOKMARK_ITEM = "OPEN_HISTORY_OR_BOOKMARK_ITEM";

	//点击推荐列表item打开url-setAction
	public static final String ACTION_OPEN_RECOMMEND_DATA = "ACTION_OPEN_RECOMMEND_DATA";

	//点击消息推送列表item打开url-setAction
	public static final String ACTION_OPEN_SYSTEMNEWS_DATA = "ACTION_OPEN_SYSTEMNEWS_DATA";

	//点击产品介绍页item打开url-setAction
	public static final String ACTION_OPEN_PRODUCT_ABOUT = "ACTION_OPEN_PRODUCT_ABOUT";

	/**
	 * 新闻的notification id从100~105,请勿占用该id
	 */
	public static final int NOTIFICATION_ID_NEWS = 100;
	public static final int NOTIFICATION_ID_FB_MSG = 200;
	public static final int NOTIFICATION_ID_SYSTEM_NEWS = 2;
	public static final String EXTRA_NEWS = "extra_news";
	public static final String EXTRA_SYSTEM_NEWS = "extra_system_news";
	public static final String DATA_VERSION = "version";
	public static final String DATA_TYPE = "type";
	public static final String DATA_CONTENT = "content";
	public static final String VERSION_1 = "1";
	public static final String TYPE_NEWS = "1";
	public static final String TYPE_SYSTEM_NEWS = "2";

	/** facebook 消息通知 */
	public static final String ACTION_CLICK_FB_NOTIFICATION = "com.polar.browser.action_click_fb_notification";
	public static final String FB_NOTIFY_DATA_TYPE = "fb_type";
	public static final String FB_NOTIFY_DATA_LINK = "fb_link";
	public static final String IS_APP_FG = "IS_APP_FG";

	/** 跳转至系统通知加载界面，intent传递加载的URL */

	//应用升级状态码
	public static final int APP_NO_UPDATE = 0;
	public static final int APP_NEED_UPDATE = 1;
	public static final int APP_FORCE_UPDATE = 2;

	public static final int PAGE_EDIT_CLICK = 3;
	public static final int WEBCONTENT_EDIT_CLICK = 4;
	public static final int NOTIFY_SEARCH_CLICK = 5;
	public static final String CHANGED_FILE_LIST = "changed_file_list";
	public static final String KEY_CHANGED_FILE_TYPE = "changedType";
	public static final String ACTION_FILE_COUNT_CHANGED = "action.file.count.changed";
    public static final String ADDR_URL = AppEnv.DEBUG ? ApiConstants.SERVER_API_UPLOAD_FILE_DEBUG : ApiConstants.SERVER_API_UPLOAD_FILE;
	public static final String ADDR_SK = AppEnv.DEBUG ? ApiConstants.API_UPLOAD_SK_DEBUG : ApiConstants.API_UPLOAD_SK;
	public static final String WEATHER_DETAIL_URL = "https://m.dailyweather.io/?clientId=50073&brand=vc-home";
}
