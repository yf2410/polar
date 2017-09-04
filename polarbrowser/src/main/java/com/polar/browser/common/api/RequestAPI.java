package com.polar.browser.common.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by FKQ on 2016/8/25.
 */
public class RequestAPI {

    /**
     * BASE_URL
     */
    public static String SERVER_API_ADDRESS = "http://api.vcllq.com/";
    public static String SERVER_API_ADDRESS_TEST = "http://test.vcllq.com:7070/";
    /**
     * 用户首次进入—添加-分类：请求接口(暂时lan字段只支持：en、pt、es其他语言默认上传en)
     */
    public static String CLASSIFY_LIST = "func.php?mp=android&type=sort&lan=" + getFormatLanguage();

    /**
     * 配置网址资源接口(暂时lan字段只支持：en、pt、es其他语言默认上传en)
     */
    public static final String WEB_RESOURCES = "func.php?mp=android&type=data&lan=" + getFormatLanguage();

    /**
     * 推荐列表（30条(暂时lan字段只支持：en、pt、es其他语言默认上传en)
     */
    public static final String RECOMMEND_LIST = "func.php?mp=android&type=webrecommend&lan=" + getFormatLanguage();

    /**
     * 上传crash信息接口
     */
    public static final String UPLOAD_CRASH = "dump.php?mp=android";

    /**
     * POST
     * 获取用户Token
     */
    public static final String INIT_TOKEN = "api/auth/init_token1.do";

    /**
     * APK升级接口
     */
    public static final String UPGRADE_APK = "juzi/api/sysUpdate.do?";
    /**
     * APK升级测试接口
     */
    public static final String UPGRADE_APK_TEST = "http://vcserver.vcapp.cn/activity/api/sysUpdate.do?";

    /**
     * 下发配置文件接口
     */
    public static String CONFIG_DATA = "server/api/adblock1.do?mp=android&type=dfupdate";

    /**
     * 下发配置文件接口
     * 老版接口，现已弃用
     */
    public static final String UPDATE_DATA_URL = "func.php?mp=android&type=dfupdate";

    /**
     * 上传用户反馈接口
     */
    public static final String UPLOAD_FEEDBACK = "func.php?mp=android&type=feedback";

    /**
     * 用户自定义网址，图标所在的服务器地址
     **/
    public static String LOGO_IMG = "img/custom/";

    /**
     * 首页加载More详情页
     */
    public static String LOAD_HOME_MORE = "http://api.vcbrowser.com/juzi/title/api/transit.do?";

    /**
     * 首页加载More详情页
     */
//    public static String LOAD_PRODUCT_HELP = "http://api.vcbrowser.com/juzi/title/api/help.do?";
    public static String LOAD_PRODUCT_HELP = "http://m.polarbrowser.com/polarhelp/?";

    /**
     * 用户反馈界面，点击whatsapp加载链接
     */
    public static String LOAD_FEEDBACK_WHATSAPP = "https://chat.whatsapp.com/BGJ0SxE8EqM0ioYElunJ0y";
//    public static String LOAD_FEEDBACK_WHATSAPP = "https://chat.whatsapp.com/invite/8EAF0Nafk2y0X6cPfYQmR7";

    /**
     * 浏览器facebook主页链接
     */
    public static String LOAD_VC_FACEBOOK = "https://www.facebook.com/vcbrowser";

    /**
     * VC官网
     */
    public static String LOAD_OFFICIAL_WEBSITE = "http://m.polarbrowser.com/";

    /**
     * 加入核心用户组织
     */
    public static String LOAD_USER_GROUP = "http://m.vcbrowser.com/about/user.html";

    /**
     * 用户协议
     */
    public static String LOAD_AGREEMENT = "http://m.polarbrowser.com/polar/license.html";

    /**
     * 隐私条款
     */
    public static String LOAD_TERMS = "http://m.polarbrowser.com/polar/privacy.html";

    /**
     * 首页加载Web版卡片页面
     */
//    public static String LOAD_HOME_WEB_CARD_TEST = "http://api.vcbrowser.com/testnewscard/polar.json?";

    public static String LOAD_HOME_WEB_CARD_TEST = "http://api.polarbrowser.com/test/polar.json?";

    public static String LOAD_HOME_WEB_CARD = "http://api.vcbrowser.com/newscard/polar.json?";



    /**
     * 网页请求时数据编码为UTF-8
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getDecoderUTF8(String str) {
        if (null == str)
            return "";
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
    }

    /**
     * url增加cv参数
     * @param cv
     * @return
     */
    public static String appendCv(String cv) {
        return "&cv=" + cv;
    }

    /**
     * 获取后台支持的语言资源(暂时lan字段只支持：en、pt、es其他语言默认上传en)
     * @return
     */
    private static String getFormatLanguage() {
        String lan = Locale.getDefault().getLanguage();
        if ("pt".equalsIgnoreCase(lan) || "en".equalsIgnoreCase(lan) || "es".equalsIgnoreCase(lan)) {
            return lan;
        }
        return "en";
    }
}
