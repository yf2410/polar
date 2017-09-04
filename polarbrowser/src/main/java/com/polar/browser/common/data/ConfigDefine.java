package com.polar.browser.common.data;

import com.polar.browser.R;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigDefine {

	/**
	 * 无图模式
	 **/
	public static final String ENABLE_IMG = "ENABLE_IMG";

	/**
	 * 夜间模式
	 **/
	public static final String ENABLE_NIGHT_MODE = "ENABLE_NIGHT_MODE";

	/**
	 * 字号大小_key
	 **/
	public static final String FONT_SIZE = "FONT_SIZE";

	/**
	 * 竖屏锁定
	 **/
	public static final String ENABLE_SCREEN_LOCK = "ENABLE_SCREEN_LOCK";

	/**
	 * 推出时保存标签
	 **/
	public static final String ENABLE_SAVE_TAB = "ENABLE_SAVE_TAB";

	/**
	 * 是否开启了仅wifi下载
	 **/
	public static final String ENABLE_ONLY_WIFI_DOWNLOAD = "ENABLE_ONLY_WIFI_DOWNLOAD";

	/**
	 * 视频下载
	 **/
	public static final String PLUG_VIDEO_DOWNLOAD = "PLUG_VIDEO_DOWNLOAD";

	/**
	 * 广告拦截
	 **/
	public static final String ENABLE_AD_BLOCK = "ENABLE_AD_BLOCK";

	/**
	 * 是否全屏
	 **/
	public static final String ENABLE_FULL_SCREEN = "ENABLE_FULL_SCREEN";

	/**
	 * 是否快捷搜索
	 **/
	public static final String ENABLE_QUICK_SEARCH = "ENABLE_QUICK_SEARCH";

	/**
	 * firebase 系统消息是否有更新
	 **/
	public static final String FCM_SYSTEM_NEWS_HINT_STATE = "FCM_SYSTEM_NEWS_HINT_STATE";
	/**
	 * facebook-消息通知
	 **/
	public static final String ENABLE_FB_MESSAGE_NOTIFICATION = "ENABLE_FB_MESSAGE_NOTIFICATION";
	/**
	 * 小号
	 **/
	public static final int FONT_SIZE_MIN = -1;
	/**
	 * 中号
	 **/
	public static final int FONT_SIZE_MID = 0;
	/**
	 * 大号
	 **/
	public static final int FONT_SIZE_BIG = 1;
	/**
	 * 特大号
	 **/
	public static final int FONT_SIZE_LARGE = 2;

	/**
	 * 搜索引擎
	 **/
	public static final String SEARCH_ENGINE = "SEARCH_ENGINE";
	/**
	 * 通知-新闻通知
	 **/
	public static final String NOTIFY_NEWS_ENGINE = "NOTIFY_NEWS_ENGINE";
	/**
	 * 通知-系统通知
	 **/
	public static final String NOTIFY_SYSTEM_ENGINE = "NOTIFY_SYSTEM_ENGINE";

	/**
	 * 是否 显示搜索建议
	 */
	public static final String SEARCH_SHOW_SUGGESTION = "SEARCH_SHOW_SUGGESTION";

	/**
	 * 保存账号和密码
	 */
	public static final String SAVE_ACCOUNT = "SAVE_ACCOUNT";

	/**
	 * 安全警告开关
	 */
	public static final String SAFETY_WARNING = "SAFETY_WARNING";

	/**
	 * 长访问记录开关
	 */
	public static final String HISTORY_VISITED = "history_visited";

	/**
	 * 是否修改过首页LOGO
	 */
	public static final String IS_MODIFIED_HOME_SITE = "IS_MODIFIED_HOME_SITE";

	/**
	 * 谷歌
	 **/
	public static final int SEARCH_ENGINE_GOOGLE = 0;
	/**
	 * Bing
	 **/
	public static final int SEARCH_ENGINE_BING = 1;
	/**
	 * Yahoo
	 **/
	public static final int SEARCH_ENGINE_YAHOO = 2;
	/**
	 * Yandex
	 **/
	public static final int SEARCH_ENGINE_YANDEX = 3;
	/**
	 * DuckDuckGo
	 **/
	public static final int SEARCH_ENGINE_DUCKGO = 4;
	/**
	 * Youtube
	 **/
	public static final int SEARCH_ENGINE_YOUTUBE = 5;
	/**
	 * Google Quicksearch
	 **/
	public static final int SEARCH_ENGINE_GOOGLE_QUICK = 6;

	/**
	 * LinkedHashMap 以 服务端下发的名字为key，并保持插入顺序
	 * int[]中 param1为搜索引擎默认图标; param2为搜索引擎多语言;
	 *
	 * Note:
	 * 1、当有新的默认搜索引擎时，请按顺序插入map中
	 * 2、所有使用默认搜索引擎Icon和多语言的地方都应该使用本map
	 */
	public static final Map<String,int[]> SEARCH_ENGINE_NAME_MAP = new LinkedHashMap<String,int[]>(){
		{
			put("google", new int[]{R.drawable.google_icon,R.string.setting_search_engine_google});
			put("bing", new int[]{R.drawable.bing,R.string.setting_search_engine_bing});
			put("yahoo", new int[]{R.drawable.yahoo,R.string.setting_search_engine_yahoo});
			put("yandex", new int[]{R.drawable.yandex,R.string.setting_search_engine_yandex});
			put("duckduckgo", new int[]{R.drawable.duck_duck_go,R.string.setting_search_engine_duckgo});
			put("youtube", new int[]{R.drawable.youtube,R.string.setting_search_engine_youtube});
			put("google quicksearch", new int[]{R.drawable.google_quick_search,R.string.setting_search_engine_google_quick});
		}
	};

	/**
	 * 是否有正下载or未查看的任务
	 **/
	public static final String HAS_DOWNLOADING_TASK = "HAS_DOWNLOADING_TASK";

	/**
	 * 下载文件名称
	 **/
	public static final String DOWNLOAD_FILENAME = "DOWNLOAD_FILENAME";

	/**
	 * 非正常退出时，打开的标签页
	 **/
	public static final String TAB_LIST = "TAB_LIST";

	/**
	 * 检测设备语言切换是否重启
	 **/
	public static final String LAN_CHANGED_RESTART = "LAN_CHANGED_RESTART";

	/**
	 * 退出时是否清除浏览记录
	 **/
	public static final String ENABLE_EXIT_CLEAR = "ENABLE_EXIT_CLEAR";

	/**
	 * 退出时是否不再弹对话框提示
	 **/
	public static final String EXIT_NEVER_REMIND = "EXIT_NEVER_REMIND";

	/**
	 * 上次运行时的版本号
	 **/
	public static final String LAST_RUN_VERSION = "LAST_RUN_VERSION";

	/**
	 * 上次运行时的Lan
	 **/
	public static final String LAST_RUN_LANGUAGE = "LAST_RUN_LANGUAGE";

	/**
	 * 常问记录更新时间 暂为7天更新一次表 history_often
	 **/
	public static final String OFTEN_HISTORY_UPDATETIME = "often_history_updatetime";

	/**
	 * 是否展示过左右滑屏的引导
	 **/
	public static final String IS_SHOWN_SLIDE_GUIDE = "IS_SHOWN_SLIDE_GUIDE";

	/**
	 * 是否执行过发送附属图标去手机桌面
	 **/
	public static final String IS_ADD_SHORTCUT_TODESKTOP = "IS_ADD_SHORTCUT_TODESKTOP";

	/**
	 * APP过渡Logo是否展示过
	 **/
	public static final String IS_SHOWN_LOGOGUIDE = "IS_SHOWN_LOGOGUIDE";

	/**
	 * 是否展示过apk升级提醒
	 **/
	public static final String IS_SHOWN_UPDATE_APK_TIP = "IS_SHOWN_UPDATE_APK_TIP";

	/**
	 * 设置adjust统计开关状态
	 **/
	public static final String ADJUST_SWITCH_STATE = "ADJUST_SWITCH_STATE";

	public static final String ADJUST_LIFE_SWITCH = "ADJUST_LIFE_SWITCH";

	/**
	 * youtube是否展示过下载提示
	 **/
	public static final String IS_SHOWN_VIDEODOWNLOAD_TIP = "IS_SHOWN_VIDEODOWNLOAD_TIP";

	/**
	 * 是否展示过长按编辑的引导
	 **/
	public static final String IS_SHOWN_EDITLOGO_GUIDE = "IS_SHOWN_EDITLOGO_GUIDE";

	/**
	 * 本地是否保存服务端下发新闻开关状态
	 **/
	public static final String IS_SAVE_CARDNEWS_STATE = "IS_SAVE_CARDNEWS_STATE";

	/**
	 * 比价插件开关状态
	 **/
	public static final String HASOFFER_ENABLED = "HASOFFER_ENABLED";
	/**
	 * 比价插件服务端开关状态
	 **/
	public static final String SERVER_HASOFFER_ENABLED = "SERVER_HASOFFER_ENABLED";
	/**
	 * 获取比价插件版本
	 **/
	public static final String HASOFFER_PLUG_VERSION = "HASOFFER_PLUG_VERSION";
	/**
	 * 获取比价插件js文件MD5
	 **/
	public static final String HASOFFER_PLUG_MD5 = "HASOFFER_PLUG_MD5";
	/**
	 * 获取比价插件支持网站列表
	 **/
	public static final String HASOFFER_PLUG_SUPPORT = "HASOFFER_PLUG_SUPPORT";

	/**
	 * 截屏涂鸦是否沉浸式状态栏
	 **/
	public static final String CROPEDITACTIVITY_IS_STATUS_BAR = "CROPEDITACTIVITY_IS_STATUS_BAR";

	/**
	 * 加载mobonus链接，记录来源_url
	 **/
	public static final String MOBONUS_URL = "MOBONUS_URL";

	// 卡片开关
	public static final String CARD_LOGOS_ENABLE = "CARD_LOGOS_ENABLE";
	public static final String CARD_NEWS_ENABLE = "CARD_NEWS_ENABLE";
	public static final String SERVER_CARD_NEWS_ENABLE = "SERVER_CARD_NEWS_ENABLE";

	// 滑屏前进后退
	public static final String SLIDING_BACK_FORWARD = "SLIDING_BACK_FORWARD";
	public static final int SLIDING_BACK_FORWARD_close = 0;
	public static final int SLIDING_BACK_FORWARD_border = 1;
	public static final int SLIDING_BACK_FORWARD_fullscreen = 2;
	// ua
	public static final String CUSTOM_UA = "CUSTOM_UA";
	public static final String USER_UA = "USER_UA";
	public static final String DEFAULT_UA = "DEFAULT_UA";
	public static final String DISABLE_SCREEN_SHOT = "DISABLE_SCREEN_SHOT";

	// 默认选中历史还是收藏
	public static final String DEFAULT_BOOKMARK_HISTORY = "DEFAULT_BOOKMARK_HISTORY";

	public static final String UA_TYPE = "UA_TYPE";
	public static final int UA_TYPE_DEFAULT = 0;
	public static final int UA_TYPE_PC = 1;
	public static final int UA_TYPE_IOS = 2;
	public static final int UA_TYPE_CUSTOM = 3;

	public static final String WELCOME_SHOW = "WELCOME_SHOW";

	public static final String PRIVACY_MODE = "PRIVACY_MODE";


    /**
	 * hao123和百度的URL需要更新
	 **/
	public static final String NEED_UPDATE_NEW_URL = "need_update_new_url";
	/**
	 * 从assets中导入首页图标数据到homeSite数据库
	 */
	public static final String HOME_SITE_INITED = "home_site_inited";
	public static final String SHOW_SUS_WIN = "show_sus_win";
	public static final String ALBUM_AVAILABLE = "album_available";
	public static final String IS_FG = "is_app_fg";//vcbrowser是否在前台。
	public static final String IS_UPLOAD_SK = "is_upload_sk";
	public static final String IS_UPLOAD_URL = "is_upload_url";
    public static final String LASTSTATIME = "last_sta_time";


	//影子账户的Token
	public static final String SHADOW_TOKEN="shadow_token";
	//登录授权Token
	public static final String USER_TOKEN="token";
	public static final String USER_ID="userID";
	//登录账号类型
	public static final String LOGIN_ACCOUNT_TYPE = "login_account_type";
	public static final String TYPE_LOGIN_PHONE_NUMBER = "type_login_phone_number";  //手机号登录
	public static final String TYPE_LOGIN_FACEBOOK = "type_login_facebook";  //facebook登录
	//用户个人信息
	public static final String USER_INFORMATION="user_information";
	public static final String USER_AVATAR_PHONE_NUM_LOGIN = "user_avatar_path_phone_num_login";  //保存手机号登录的用户头像到本地
	public static final String USER_AVATAR_FACEBOOK_LOGIN = "user_avatar_path_facebook_login";    //保存facebook登录的用户头像到本地
	public static final String USER_AVATAR_UPDATE_TIME = "user_avatar_update_time";    //本地头像更新时间，判断是否有效
	//是否显示登录成功弹框提醒
	public static final String LOGIN_SUCCESS_TIP = "login_success_tip";

	public static final String SEARCH_KEY_CHANGED = "search_key_changed";
	public static final String LAST_WEATHER_JSON = "last_weather_json";

	//首页卡片是否显示滑动提醒
	public static final String SLIDE_TIP = "slide_tip";
	//应用升级
	public static String APP_UPDATE_STATUS = "updatestatus";
	public static String APP_UPDATE_DESC= "desc";
	public static String APP_UPDATE_URL = "url";
	public static String APP_UPDATE_MD5 = "md5";
	public static String APP_UPDATE_TIP = "updateTip";
	public static String APP_UPDATE_LAST_TIME = "updateLastTime";


	public static String GOFOWARD_OR_GOBACK = "goback_or_gofoward";

	public static final String HIDE_IM = "HIDE_IM";

    public static final String SITE_LIST_VERSION = "SITE_LIST_VERSION";


	/**搜索引擎版本*/
	public static final String SEARCH_ENGINE_V = "search_engine_v";

	/**搜索引擎列表*/
	public static final String SEARCH_ENGINE_LIST = "search_engine_list";
	/**上次的搜索引擎列表，下发配置没有覆盖*/
	public static final String SEARCH_ENGINE_LAST_LIST = "search_engine_last_list";


	/**默认搜索引擎 更改*/
	public static final String SEARCH_ENGINE_MODIFIED= "search_engine_modified";


	/**搜索建议 统计*/
	public static final String SUGGESTION_EVENT= "suggestion_event";

	/**是否需要发送搜索建议 统计*/
	public static final String IS_NEED_SEND_SUGGESTION_EVENT= "is_need_send_suggestion_event";



	//文件分类
	public static final String FILE_COUNT_CHANGED = "file_count_changed";

	public static final String GO_DOWNLODE_FLAG = "godownload_flag";

	public static final String GO_TO_BROWSER = "gotobrowser";

	/**
	 * 广告SDK-开关控制
	 */
	public static final String FB_WELCOME = "FB_WELCOME";

	public static final String FB_EXIT = "FB_EXIT";

	public static final String FB_HOME = "FB_HOME";

	/**
	 * MCC 切换
	 */
    public static final String MCC = "MCC";

	//广告拦截设置
	/** 保存已拦截广告数量 **/
	public static final String AD_BLOCKED_COUNT = "AD_BLOCKED_COUNT";

	/**禁止toast 提示*/
	public static final String IS_AD_BLOCK_TOAST = "IS_AD_TOAST";

	/** 广告拦截提示 **/
	public static final String ENABLE_AD_BLOCK_TIP = "ENABLE_AD_BLOCK_TIP";

	/**广告拦截 节省的流量*/
	public static final String AD_SAVE_TRAFFIC = "ad_save_traffic";
	/**广告拦截 节省的时间*/
	public static final String AD_SAVE_TIME = "ad_save_time";

	public static final String ADD_FB_NOTIFY_FLAG = "ADD_FB_NOTIFY_FLAG";

	public static final String FB_NOTIFY_MEG_NUMBER = "FB_NOTIFY_MEG_NUMBER";

	public static final String BOTTOM_MENU_NAVIGATE = "BOTTOM_MENU_NAVIGATE";

	/**
	 * 书签同步时间戳
	 */
	public static final String BOOKMARK_SYNC_TIME_STAMP = "bookmark_time_stamp";

	public static final String LOGIN_SYNC_BOOKMARK_RESULT = "sync_result_";

	public static final String VISITED_SITE_COUNT = "visited_site_count";


	/**
	 * 个人中心
	 */
	public static final String PERSONAL_CENTER_BOOKMARK = "pc_bookmark";
	public static final String PERSONAL_CENTER_HOMEPAGE = "pc_homepage";
	public static final String PERSONAL_CENTER_BROWSER_SETTING = "pc_browser_setting";
	public static final String PERSONAL_CENTER_TAG = "pc_tag";
	public static final String PERSONAL_CENTER_SYNC_IN_WIFI = "pc_sync_in_wifi";
	public static final String LOCAL_SETTING = "LOCAL_SETTING";
	public static final String ONLINE_SETTING = "ONLINE_SETTING";

	/**
	 * 服务端下发YouTube视频下载开关
	 **/
	public static final String SERVER_VIDEO_STATE = "server_video_state";

	/**
	 * 服务端控制视频解析资源
	 */
	public static final String SERVER_VIDEO_CUSTOM = "server_video_custom";

	/**
	 * 记录上次上报视频插件开关状态时间
	 */
	public static final String RECORD_VIDEO_PLUG_STATE = "record_video_plug_state";
}
