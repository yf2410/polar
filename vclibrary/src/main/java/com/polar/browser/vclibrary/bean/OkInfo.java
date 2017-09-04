package com.polar.browser.vclibrary.bean;

/**
 * Created by yangfan on 2017/6/8.
 */

public class OkInfo {
    /**
     * 视频下载
     **/
    public boolean plugVideoDownload;

    /**
     * 比价插件开关状态
     **/
    public boolean plugPriceCompare;

    /**
     * 是否 显示搜索建议
     */
    public boolean plugSuggestion;

    /**
     * 退出时保存标签
     */
    public boolean saveTab;

    /**
     * 设置竖屏锁定
     */
    public boolean screenLock;

    /**
     * 是否快捷搜索
     **/
    public boolean quickSearch;

    /**
     * 安全警告开关
     */
    public boolean safeTip;

    /**
     * 最常访问
     */

    public boolean oftenVisit;

    /**
     * 通知-新闻通知
     **/
    public boolean notifyNews;

    /**
     * 通知-系统通知
     **/
    public boolean notifySystem;

    /**
     * facebook-消息通知
     **/
    public boolean notifyFacebook;

    /**
     * 广告拦截
     **/
    public boolean plugAdBlock;

    /**
     * 保存账号和密码
     **/
    public boolean saveAcount;

    /**
     * 是否开启了仅wifi下载
     **/
    public boolean onlyWifiDownload;

    /**
     * 当前存储的下载路径
     **/
    public String currentDownloadFolder;

    /**
     * 搜索引擎
     **/
    public int searchEngine;
    /**
     * 滑屏翻页手势
     **/
    public int slidingBackForward;
    /**
     * 浏览器标识
     **/
    public int UAType;

    /**
     * 书签同步
     */
    public boolean isBookmarkSync;
    /**
     * 主页图标同步
     */
    public boolean isHomeSiteSync;
    /**
     * 浏览器设置同步
     */
    public boolean isSettingSync;

    /**
     * 仅在WiFi下同步
     */
    public boolean isOnlywifiSync;

    @Override
    public String toString() {
        return "OkInfo{" +
                "plugVideoDownload=" + plugVideoDownload +
                ", plugPriceCompare=" + plugPriceCompare +
                ", plugSuggestion=" + plugSuggestion +
                ", saveTab=" + saveTab +
                ", screenLock=" + screenLock +
                ", quickSearch=" + quickSearch +
                ", safeTip=" + safeTip +
                ", oftenVisit=" + oftenVisit +
                ", notifyNews=" + notifyNews +
                ", notifySystem=" + notifySystem +
                ", notifyFacebook=" + notifyFacebook +
                ", plugAdBlock=" + plugAdBlock +
                ", saveAcount=" + saveAcount +
                ", onlyWifiDownload=" + onlyWifiDownload +
                ", currentDownloadFolder='" + currentDownloadFolder + '\'' +
                ", searchEngine=" + searchEngine +
                ", slidingBackForward=" + slidingBackForward +
                ", UAType=" + UAType +
                ", isBookmarkSync=" + isBookmarkSync +
                ", isHomeSiteSync=" + isHomeSiteSync +
                ", isSettingSync=" + isSettingSync +
                ", isOnlywifiSync=" + isOnlywifiSync +
                '}';
    }
}
