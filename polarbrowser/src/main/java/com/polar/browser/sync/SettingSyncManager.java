package com.polar.browser.sync;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.ZipUtil;
import com.polar.browser.vclibrary.bean.SettingSyncInfo;

import java.io.File;

/**
 * Created by yd_lp on 2017/5/10.
 * 同步设置
 */

public final class SettingSyncManager {
    public static final String KEY_SYNC_TYPE = "sync_type";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_ID = "user_id";

    /**用户登录帐号时*/
    public static final int SYNC_TYPE_LOGIN = 0;
    /**用户退出帐号时*/
    public static final int SYNC_TYPE_LOGOUT = 1;
    /**点退出按钮退出浏览器时*/
    public static final int SYNC_TYPE_EXIT = 3;
    /**按home键退出浏览器*/
    public static final int SYNC_TYPE_HOME = 4;

    private static final String FILE_SETTING_LOACL = "setting_local";
    private static final String FILE_SETTING_ONLINE = "setting_online";

    public static final String KEY_SYNC_SETTING_LOCAL = "sync_setting_local_time";

    private static SettingSyncManager instance;

    private SettingSyncManager() {
        //EventBus.getDefault().register(this);
    }

    public synchronized static SettingSyncManager getInstance() {
        if (instance == null) {
            instance = new SettingSyncManager();

        }
        return instance;
    }

    /**
     * 用户登录帐号时
     * @param type
     * SYNC_TYPE_LOGIN
     * SYNC_TYPE_LOGOUT
     * SYNC_TYPE_EXIT
     * SYNC_TYPE_HOME
     */
    public void syncSetting(int type) {
        if (!AccountLoginManager.getInstance().isUserLogined()) {
            return;
        }
        String userID = ConfigWrapper.get(ConfigDefine.USER_ID, "");
        String userToken = ConfigManager.getInstance().getUserToken();
        SyncService.startSyncService(JuziApp.getInstance().getApplicationContext(), type, userToken, userID);
    }

    public String getSettingSyncTime(String userID) {
        if (TextUtils.isEmpty(userID)) {
            return "";
        }
        String key = userID + "_" + KEY_SYNC_SETTING_LOCAL;
        return ConfigWrapper.get(key, "");
    }

    public void setSettingSyncTime(String userID, String time) {
        if (TextUtils.isEmpty(userID)) {
            return;
        }
        String key = userID + "_" + KEY_SYNC_SETTING_LOCAL;
        ConfigWrapper.put(key, time);
        ConfigWrapper.apply();
    }

    public String getLocalSettingFile() {
        return String.format("%s/%s", JuziApp.getAppContext()
                .getFilesDir(), FILE_SETTING_LOACL);
    }

    public String getOnlineSettingFile() {
        if (AccountLoginManager.getInstance().isUserLogined()) {
            String logined = ConfigManager.getInstance().getUserId() + FILE_SETTING_ONLINE;
            return String.format("%s/%s", JuziApp.getAppContext()
                    .getFilesDir(), logined);
        }
        return "";
    }

    /**
     * 备份本地要上传的setting
     */
    public boolean createLocalSetting() {
        String path = getLocalSettingFile();
        File file = new File(path);
        SettingSyncInfo info = getSyncSettingInfo();
        Gson gson = new Gson();
        String json = gson.toJson(info);
        return FileUtils.writeFile(file, ZipUtil.gZip(json.toString().getBytes()));
    }

    /**
     * 服务器setting更新本地setting
     */
    public void applyOnlineSetting() {
        try {
            String path = getOnlineSettingFile();
            if (TextUtils.isEmpty(path)) {
                return;
            }
            File file = new File(path);
            byte[] jsonBytes = ZipUtil.unGZip(FileUtils.readFile(file));//解压
            String result = null;
            StringBuilder sb=new StringBuilder();
            if (jsonBytes != null && jsonBytes.length > 0) {
                Gson gson = new Gson();
                String newJson = new String(jsonBytes);
                String[] split = newJson.split(",");
                if (split.length <= 18){
                    //字段不够，手动添加
                    for(int i = 0; i< split.length; i++){
                        if(i == split.length - 2){
                            sb.append("\"isBookmarkSync\":"+true + ",");
                            sb.append("\"isHomeSiteSync\":"+true + ",");
                            sb.append("\"isSettingSync\":"+true + ",");
                            sb.append("\"isOnlywifiSync\":"+true + ",");
                        }
                        sb.append(split[i] + ",");
                    }
                    result=sb.toString().substring(0,sb.toString().length()-1);
                    SettingSyncInfo info = gson.fromJson(new String(result), SettingSyncInfo.class);
                    updateSetting(info);
                }else {
                    SettingSyncInfo info = gson.fromJson(newJson, SettingSyncInfo.class);
                    updateSetting(info);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取本地setting
     */
    public synchronized SettingSyncInfo getSyncSettingInfo() {
        SettingSyncInfo info = new SettingSyncInfo();
        info.plugVideoDownload = ConfigWrapper.get(ConfigDefine.PLUG_VIDEO_DOWNLOAD, true);
        info.plugAdBlock = ConfigWrapper.get(ConfigDefine.ENABLE_AD_BLOCK, true);
        info.plugPriceCompare = ConfigWrapper.get(ConfigDefine.HASOFFER_ENABLED, true);
        info.plugSuggestion = ConfigWrapper.get(ConfigDefine.SEARCH_SHOW_SUGGESTION, true);
        info.saveTab = ConfigWrapper.get(ConfigDefine.ENABLE_SAVE_TAB, false);
        info.screenLock = ConfigWrapper.get(ConfigDefine.ENABLE_SCREEN_LOCK, false);
        info.quickSearch = ConfigWrapper.get(ConfigDefine.ENABLE_QUICK_SEARCH, true);
        info.safeTip = ConfigWrapper.get(ConfigDefine.SAFETY_WARNING, true);
        info.notifyNews = ConfigWrapper.get(ConfigDefine.NOTIFY_NEWS_ENGINE, true);
        info.notifySystem = ConfigWrapper.get(ConfigDefine.NOTIFY_SYSTEM_ENGINE, true);
        info.notifyFacebook = ConfigWrapper.get(ConfigDefine.ENABLE_FB_MESSAGE_NOTIFICATION, true);
        info.saveAcount = ConfigWrapper.get(ConfigDefine.SAVE_ACCOUNT, true);
        info.onlyWifiDownload = ConfigWrapper.get(ConfigDefine.ENABLE_ONLY_WIFI_DOWNLOAD, true);
        info.currentDownloadFolder = ConfigWrapper.get(CommonData.KEY_DOWN_ROOT, VCStoragerManager.getInstance().getDefaultDownloadDirPath());
        info.searchEngine = ConfigWrapper.get(ConfigDefine.SEARCH_ENGINE, ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK);
        info.slidingBackForward = ConfigWrapper.get(ConfigDefine.SLIDING_BACK_FORWARD,
                ConfigDefine.SLIDING_BACK_FORWARD_border);
        info.UAType = ConfigWrapper.get(ConfigDefine.UA_TYPE, ConfigDefine.UA_TYPE_DEFAULT);
        info.oftenVisit = ConfigWrapper.get(ConfigDefine.HISTORY_VISITED, false);
        info.isBookmarkSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BOOKMARK, true);
        info.isHomeSiteSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_HOMEPAGE, true);
        info.isSettingSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BROWSER_SETTING, true);
        info.isOnlywifiSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI, true);
        return info;
    }

    /**
     * 更新本地setting
     */
    public synchronized void updateSetting(SettingSyncInfo info) {
        if (info == null) {
            return;
        }
        ConfigManager config = ConfigManager.getInstance();
        config.setEnableVedioDownload(info.plugVideoDownload);
        config.setEnableAdBlock(info.plugAdBlock);
        config.setHasofferEnabled(info.plugPriceCompare);
        config.setShowSuggestion(info.plugSuggestion);
        config.setEnableSaveTab(info.saveTab);
        config.setEnableScreenLock(info.screenLock);
        config.setEnableQuickSearch(info.quickSearch);
        config.setSafetyWarningEnabled(info.safeTip);
        config.setNotifyNewsEngine(info.notifyNews);
        config.setNotifySystemEngine(info.notifySystem);
        config.setFbMessageNotificationEngine(info.notifyFacebook);
        config.setEnableSaveAccount(info.saveAcount);
        config.setEnableOnlyWifiDownload(info.onlyWifiDownload);
        config.setSearchEngine(info.searchEngine, true);
        config.setSlidingScreenMode(info.slidingBackForward);
        config.setUaType(info.UAType);
        config.setOftenVisit(info.oftenVisit);

        config.setIsSettingSync(info.isSettingSync);
        config.setIsBookmarSync(info.isBookmarkSync);
        config.setIsHomeSiteSync(info.isHomeSiteSync);
        config.setIsOnlyWifeSync(info.isOnlywifiSync);

        ConfigWrapper.put(CommonData.KEY_DOWN_ROOT, info.currentDownloadFolder);
        ConfigWrapper.apply();

        Gson gson = new Gson();
        String json = gson.toJson(info);
        ConfigManager.getInstance().setOnlineSetting(json);
    }


    public void reflshLocalSetting() {
        String localSetting = ConfigManager.getInstance().getLocalSetting();
        Gson gson = new Gson();
        SettingSyncInfo info = gson.fromJson(new String(localSetting), SettingSyncInfo.class);
        updateSetting(info);
    }

    /**
     * 用户登录时将本地浏览器设置保存起来
     */
    public void saveLocalSettingFile() {
        SettingSyncInfo info = getSyncSettingInfo();
        Gson gson = new Gson();
        String json = gson.toJson(info);
        ConfigManager.getInstance().setLocalSetting(json);
    }

    /**
     * 当用户关闭浏览器设置按钮时的同步操作
     * @return
     */
    public boolean createNoUpdataSettingFile() {
        String path = getLocalSettingFile();
        String onLineSetting = ConfigManager.getInstance().getOnLineSetting();
        File file = new File(path);
        SettingSyncInfo info = new Gson().fromJson(onLineSetting , SettingSyncInfo.class);
        info.isBookmarkSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BOOKMARK, true);
        info.isHomeSiteSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_HOMEPAGE, true);
        info.isSettingSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BROWSER_SETTING, true);
        info.isOnlywifiSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI, true);
        Gson gson = new Gson();
        String json = gson.toJson(info);
        return FileUtils.writeFile(file, ZipUtil.gZip(json.toString().getBytes()));
    }
}
