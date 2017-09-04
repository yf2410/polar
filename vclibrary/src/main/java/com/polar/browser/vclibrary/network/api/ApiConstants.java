package com.polar.browser.vclibrary.network.api;

/**
 * Created by James on 2016/9/18.
 */

public interface ApiConstants {

    //公共参数
    String PARAM_MOBILE_PLATFORM = "mp";
    String PARAM_OS_VERSION = "os";
    String PARAM_APP_VERSION = "ver";
    String PARAM_APP_VERSION_CODE = "verCode";
    String PARAM_APP_VERSION_CODE_OLD = "vercode";
    String PARAM_LANGUAGE = "lan";
    String PARAM_AREA = "area";
    String PARAM_MMOD = "mmod";
    String PARAM_MCC = "mcc";
    String PARAM_MID = "mid";
    String PARAM_CV = "cv";

    //业务参数
    String PARAM_SITE_LIST_VERSION = "siteListVersion";
    String PARAM_SITE_TYPE = "siteType";

    // 下载资源上报打点
    String PARAM_TYPE = "type";
    String PARAM_URL = "url";
    String PARAM_USER_AGENT = "userAgent";
    String PARAM_CONTENT_DISPOSITION = "contentDisposition";
    String PARAM_MIMETYPE = "mimetype";
    String PARAM_CONTENT_LENGTH = "contentLength";
    String PARAM_REFERER = "referer";
    String PARAM_COOKIES = "cookies";

    //用户反馈
    String PARAM_FEEDBACK_CONTENT = "content";
    String PARAM_FEEDBACK_CONTACT = "contact";
    String PARAM_FEEDBACK_CPURATE = "cpuRate";
    String PARAM_FEEDBACK_TOTALMEMORY = "totalMemory";
    String PARAM_FEEDBACK_PIXEL = "pixel";
    String PARAM_FEEDBACK_NETTYPE = "netType";
    String PARAM_FEEDBACK_AVAILMEMORY = "availMemory";
    String PARAM_FEEDBACK_USEMEMORY = "useMemory";
    //广告SDK服务端开关控制
    String PARAM_AD_VERSION = "adVersion";

    /**搜索引擎下发*/
    String PARAM_SEARCH_ENGINE_V = "version";

    String PARAM_APP_NAME = "appName";
    String PARAM_USER_TOKEN = "token";

    /** BASE_URL */
    String SERVER_API_ADDRESS = "http://api.polarbrowser.com/juzi/";
    String SERVER_API_UPLOAD_FILE_DEBUG = "http://172.17.228.120/juzi/api/vcd/urlLog.do";
    String SERVER_API_UPLOAD_FILE = "http://api.vcbrowser.com/juzi/api/vcd/urlLog.do";
    //上传sk的接口
    String API_UPLOAD_SK_DEBUG = "http://172.17.238.48/news/api/vcd/skLog.do";
    String API_UPLOAD_SK = "http://api.polarbrowser.com/juzi/api/vcd/skLog.do";
    //youtube视频下载弹框banner链接
    String YTB_VIDEO_BANNER = "http://api.vcbrowser.com/juzi/ad/api/icon.do?p=p";
    //youtube视频下载弹框banner点击跳转链接
    String YTB_VIDEO_BANNER_ACTION = "http://api.vcbrowser.com/juzi/ad/api/url.do?p=p";

}
