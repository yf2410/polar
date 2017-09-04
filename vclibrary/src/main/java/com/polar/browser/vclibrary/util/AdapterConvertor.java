package com.polar.browser.vclibrary.util;

import android.content.Context;
import android.text.TextUtils;

import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.UserHomeSite;
import com.polar.browser.vclibrary.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FKQ on 2016/9/29.
 */

public class AdapterConvertor {

    /**
     * MCC转换
     * @param preMCC
     * @return
     * @dec 印度地区获取MCC404，405，406都转换为404
     */
    public static String getMCCFromIN(String preMCC) {
        String mcc = preMCC;
        if (Constants.IN_MCC_405.equals(preMCC) || Constants.IN_MCC_406.equals(preMCC)) {
            mcc = Constants.IN_MCC_404;
        }
        return mcc;
    }

    /**
     * 根据设备MCC转换对应语言预置网址
     * 默认返回英文的预置网址
     * @param preMCC
     * @return
     */
    public static String getLanFromMCC(String preMCC) {
        switch (preMCC) {
            case Constants.AE_MCC_424:
            case Constants.AE_MCC_430:
            case Constants.AE_MCC_431:
                return Constants.LAN_AR;
            case Constants.US_MCC_310:
            case Constants.US_MCC_311:
            case Constants.US_MCC_312:
            case Constants.US_MCC_313:
            case Constants.US_MCC_314:
            case Constants.US_MCC_315:
            case Constants.US_MCC_316:
            case Constants.US_MCC_330:
            case Constants.US_MCC_332:
            case Constants.US_MCC_534:
            case Constants.US_MCC_535:
            case Constants.US_MCC_544:
                return Constants.LAN_EN;
            case Constants.CL_MCC_730:
                return Constants.LAN_ES;
            case Constants.IN_MCC_404:
            case Constants.IN_MCC_405:
            case Constants.IN_MCC_406:
                return Constants.LAN_HI;
            case Constants.ID_MCC_510:
                return Constants.LAN_IN;
            case Constants.BR_MCC_724:
                return Constants.LAN_PT;
            case Constants.RU_MCC_250:
                return Constants.LAN_RU;
            case Constants.CN_MCC_460:
            case Constants.CN_MCC_461:
                return Constants.LAN_HI;
            default:
                return Constants.LAN_EN;
        }
    }

    /**
     * 根据蔡承蒙邮件,此处的地区是指mcc
     * 【判断顺序】
     * 地区>语言
     * <p>
     * 【地区判断】
     * 地区匹配为印度时展示“印度”的内容；
     * 地区匹配为巴西时展示“巴西”的内容；
     * 地区匹配为智利时展示“智利”的内容；
     * <p>
     * 地区匹配不上时展示为“默认（英语）”的内容，默认内容不需要判断语言。
     * <p>
     * 【语言判断】
     * 印度：
     * 语言匹配为印地语时展示“印度（印地语）”的内容；
     * 语言匹配不上时默认展示为“印度（英语）”的内容，默认内容获取不到时展示“默认（英语）”的内容。
     * <p>
     * 巴西：
     * 语言匹配为葡语时展示“巴西（葡语）”的内容；
     * 语言匹配不上时默认展示为“巴西（葡语）”的内容，默认内容获取不到时展示“默认（英语）”的内容。
     * <p>
     * 智利：
     * 语言匹配为西语时展示“智利（西语）”的内容；
     * 语言匹配不上时默认展示为“智利（西语）”的内容，默认内容获取不到时展示“默认（英语）”的内容。
     */
    public static String getConfig(String mcc, String area, String language) {
        String country;
        switch (mcc) {
            case Constants.IN_MCC_404:
            case Constants.IN_MCC_405:
            case Constants.IN_MCC_406:
                country = Constants.AREA_IN;
                break;
            case Constants.BR_MCC_724:
                country = Constants.AREA_BR;
                break;
            case Constants.CL_MCC_730:
                country = Constants.AREA_CL;
                break;
            case Constants.CN_MCC_460:
            case Constants.CN_MCC_461:
                country = Constants.AREA_CN;
                break;
            default:
                country = Constants.DEFAULT_COUNTRY;
                break;
        }

        switch (country) {
            case Constants.AREA_IN:
                language = Constants.LAN_EN;
                break;
            case Constants.AREA_CN:
                country = Constants.AREA_IN;
                language = Constants.LAN_EN;
                break;
            case Constants.AREA_BR:
                language = Constants.LAN_PT;
                break;
            case Constants.AREA_CL:
                language = Constants.LAN_ES;
                break;
            default:
                language = Constants.LAN_EN;
                country = Constants.DEFAULT_COUNTRY;
                break;
        }
        return compose(language, country);
    }

    private static String compose(String language, String country) {
        if (language == null) {
            language = "default";
        }
        if (country == null) {
            country = "default";
        }
        return language + "_" + country;
    }

    public static String getNetworkType(Context context) {
        int networkState = NetWorkUtils.getNetworkState(context);
        String networkType = "";
        switch (networkState) {
            case NetWorkUtils.NETWORN_WIFI:
                networkType = "WIFI";
                break;
            case NetWorkUtils.NETWORN_2G:
                networkType = "2G";
                break;
            case NetWorkUtils.NETWORN_3G:
                networkType = "3G";
                break;
            case NetWorkUtils.NETWORN_4G:
                networkType = "4G";
                break;
            default:
                networkType = "获取类型失败";
                break;
        }
        return networkType;
    }

    /**
     * 获取国家topic
     *
     * @return
     */
    public static String getNationTopic(String preMCC) {
        String nationTopic = "nation_";
        if (TextUtils.isEmpty(preMCC)) {
            return nationTopic+Constants.SET_NULL;
        }
        switch (preMCC) {
            case Constants.US_MCC_310://美国
            case Constants.US_MCC_311:
            case Constants.US_MCC_312:
            case Constants.US_MCC_313:
            case Constants.US_MCC_314:
            case Constants.US_MCC_315:
            case Constants.US_MCC_316:
            case Constants.US_MCC_330:
            case Constants.US_MCC_332:
            case Constants.US_MCC_534:
            case Constants.US_MCC_535:
            case Constants.US_MCC_544:
                return nationTopic+Constants.NATION_US;
            case Constants.CL_MCC_730://智利
                return nationTopic+Constants.NATION_CL;
            case Constants.IN_MCC_404://印度
            case Constants.IN_MCC_405:
            case Constants.IN_MCC_406:
                return nationTopic+Constants.NATION_IN;
            case Constants.ID_MCC_510://印度尼西亚
                return nationTopic+Constants.NATION_ID;
            case Constants.BR_MCC_724://巴西
                return nationTopic+Constants.NATION_BR;
            case Constants.RU_MCC_250://俄罗斯
                return nationTopic+Constants.NATION_RU;
            case Constants.CN_MCC_460://中国大陆
            case Constants.CN_MCC_461:
                return nationTopic+Constants.NATION_CN;
            case Constants.EG_MCC_602://埃及
                return nationTopic+Constants.NATION_EG;
            case Constants.TH_MCC_520://泰国
                return nationTopic+Constants.NATION_TH;
            case Constants.TR_MCC_286://土耳其
                return nationTopic+Constants.NATION_TR;
            case Constants.IR_MCC_432://伊朗
                return nationTopic+Constants.NATION_IR;
            case Constants.PK_MCC_432://巴基斯坦
                return nationTopic+Constants.NATION_PK;
            default:
                return nationTopic+Constants.SET_OTHER;
        }
    }

    /**
     * 获取语言topic
     *
     * @return
     */
    public static String getLanguageTopic(String preLan) {
        String languageTopic = "language_";
        if (TextUtils.isEmpty(preLan)) {
            return languageTopic+Constants.SET_NULL;
        }
        switch (preLan) {
            case Constants.LAN_ZH:
            case Constants.LAN_EN:
            case Constants.LAN_PT:
            case Constants.LAN_TR:
            case Constants.LAN_ES:
            case Constants.LAN_IN:
            case Constants.LAN_RU:
            case Constants.LAN_TH:
            case Constants.LAN_AR:
            case Constants.LAN_HI:
            case Constants.LAN_FR:
                return languageTopic+preLan;
            default:
                return languageTopic+Constants.SET_OTHER;
        }
    }

    public static List<HomeSite> listFormatHomeSite(List<UserHomeSite> userHomeSites) {
        List<HomeSite> localHomeSites = new ArrayList<>();
        if (!ListUtils.isEmpty(userHomeSites)) {
            for (UserHomeSite userHomeSite : userHomeSites) {
                HomeSite homeSite = new HomeSite();
                homeSite.setSiteId(userHomeSite.getSiteId());
                homeSite.setSiteName(userHomeSite.getSiteName());
                homeSite.setSiteAddr(userHomeSite.getSiteAddr());
                homeSite.setSitePic(userHomeSite.getSitePic());
                homeSite.setOrder(userHomeSite.getOrder());
                homeSite.setCustom(userHomeSite.isCustom());
                homeSite.setId(userHomeSite.getId());
                localHomeSites.add(homeSite);
            }
        }
        return localHomeSites;
    }

    public static List<UserHomeSite> listFormatUserHomeSite(List<HomeSite> homeSites) {
        List<UserHomeSite> userHomeSiteList = new ArrayList<>();
        if (!ListUtils.isEmpty(homeSites)) {
            for (HomeSite homeSite : homeSites) {
                UserHomeSite userHomeSite = new UserHomeSite();
                userHomeSite.setSiteId(homeSite.getSiteId());
                userHomeSite.setSiteName(homeSite.getSiteName());
                userHomeSite.setSiteAddr(homeSite.getSiteAddr());
                userHomeSite.setSitePic(homeSite.getSitePic());
                userHomeSite.setOrder(homeSite.getOrder());
                userHomeSite.setCustom(homeSite.isCustom());
                userHomeSite.setId(homeSite.getId());
                userHomeSiteList.add(userHomeSite);
            }
        }
        return userHomeSiteList;
    }
}
