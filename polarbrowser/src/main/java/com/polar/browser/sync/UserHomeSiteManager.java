package com.polar.browser.sync;


import android.content.Context;
import android.text.TextUtils;
import com.google.gson.reflect.TypeToken;
import com.polar.browser.JuziApp;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.homepage.customlogo.JsonParser;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.ZipUtil;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.UserHomeSite;
import com.polar.browser.vclibrary.bean.events.SyncHomeSiteEvent;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HomeSiteApi;
import com.polar.browser.vclibrary.db.UserHomeSiteApi;
import com.polar.browser.vclibrary.util.AdapterConvertor;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yangfan on 2017/5/24.
 */

public class UserHomeSiteManager {
    private Context context;
    public static final String KEY_SYNC_TYPE = "sync_type";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_ID = "user_id";

    /**
     * 用户登录帐号时
     */
    public static final int SYNC_TYPE_LOGIN = 0;
    /**
     * 用户退出帐号时
     */
    public static final int SYNC_TYPE_LOGOUT = 1;
    /**
     * 点退出按钮退出浏览器时
     */
    public static final int SYNC_TYPE_EXIT = 3;
    /**
     * 按home键退出浏览器
     */
    public static final int SYNC_TYPE_HOME = 4;

    private static final String FILE_SETTING_LOACL = "homesite_local";
    private static final String FILE_SETTING_ONLINE = "homesite_online";

    public static final String KEY_SYNC_SETTING_LOCAL = "sync_homesite_local_time";

    private static UserHomeSiteManager instance;

    private UserHomeSiteManager() {
        this.context = JuziApp.getAppContext();
    }

    public synchronized static UserHomeSiteManager getInstance() {
        if (instance == null) {
            instance = new UserHomeSiteManager();
        }
        return instance;
    }

    public void syncHomeSite(int type) {
        if (!AccountLoginManager.getInstance().isUserLogined()) {
            return;
        }
        String userID = ConfigWrapper.get(ConfigDefine.USER_ID, "");
        String userToken = ConfigManager.getInstance().getUserToken();
        UserHomeSiteService.startSyncService(JuziApp.getInstance().getApplicationContext(), type, userToken, userID);
    }

    /**
     * 本地主页图标文件名
     * @return
     */
    public String getLocalHomesiteFile() {
        return String.format("%s/%s", JuziApp.getAppContext()
                .getFilesDir(), FILE_SETTING_LOACL);
    }

    /**
     * 设置主页图标同步时间
     * @param userID
     * @param time
     */
    public void setHomeSiteSyncTime(String userID, String time) {
        if (TextUtils.isEmpty(userID)) {
            return;
        }
        String key = userID + "_" + KEY_SYNC_SETTING_LOCAL;
        ConfigWrapper.put(key, time);
        ConfigWrapper.apply();
    }

    /**
     * 在线主页图标文件名
     * @return
     */
    public String getOnlineHomesiteFile() {
        if (AccountLoginManager.getInstance().isUserLogined()) {
            String logined = ConfigManager.getInstance().getUserId() + FILE_SETTING_ONLINE;
            return String.format("%s/%s", JuziApp.getAppContext()
                    .getFilesDir(), logined);
        }
        return "";
    }

    /**
     * 将获取到的主页图标数据插入用户数据表中并刷新ui
     */
    public void applyOnlineSetting() {
        try {
            String path = getOnlineHomesiteFile();
            if (TextUtils.isEmpty(path)) {
                return;
            }
            File file = new File(path);
            byte[] jsonBytes = ZipUtil.unGZip(FileUtils.readFile(file));//解压
            if (jsonBytes != null && jsonBytes.length > 0) {
                String json = new String(jsonBytes);
                List<UserHomeSite> userHomeSites = JsonParser.fromJson(json, new TypeToken<List<UserHomeSite>>(){}.getType());
                UserHomeSiteApi userHomeSiteApi = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
                userHomeSiteApi.clear();
                if (ListUtils.isEmpty(userHomeSites)) {
                    List<HomeSite> localHomeSites = SiteManager.getInstance().getLocalHomeSites();
                    synHomeSiteInsertUserHomeSite(localHomeSites,userHomeSiteApi);

                } else {
                    SiteManager.getInstance().resetHomeSite();
                    userHomeSiteApi.insert(userHomeSites);
                    if (!SiteManager.getInstance().isIncludeAddHomeSite(userHomeSites)) {
                        UserHomeSite addUserHomeSite = HomeSiteUtil.getAddUserHomeSite(userHomeSites.size() + 1);
                        userHomeSiteApi.insert(addUserHomeSite);
                    }

                }
                EventBus.getDefault().post(new SyncHomeSiteEvent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将本地主页图标数据插入到用户数据表中
     * @param localHomeSites
     * @param userHomeSiteApi
     * @throws SQLException
     */
    private void synHomeSiteInsertUserHomeSite(List<HomeSite> localHomeSites,UserHomeSiteApi userHomeSiteApi) throws SQLException{
        for (HomeSite localHomeSite : localHomeSites) {
            UserHomeSite userHomeSite = null;
            if(TextUtils.equals(localHomeSite.getSiteId(), HomeSiteUtil.SITE_ID_ADD)){
                userHomeSite = HomeSiteUtil.getAddUserHomeSite(localHomeSites.size());
            }else {
                 userHomeSite = new UserHomeSite();
                userHomeSite.setSiteId(localHomeSite.getSiteId());
                userHomeSite.setSiteName(localHomeSite.getSiteName());
                userHomeSite.setSiteAddr(localHomeSite.getSiteAddr());
                userHomeSite.setSitePic(localHomeSite.getSitePic());
                userHomeSite.setOrder(localHomeSite.getOrder());
                userHomeSite.setCustom(localHomeSite.isCustom());
                userHomeSite.setId(localHomeSite.getId());
            }
            userHomeSiteApi.insert(userHomeSite);
        }
       List<UserHomeSite> userHomeSites = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context)).queryAllOrderByOrder();
        if (!SiteManager.getInstance().isIncludeAddHomeSite(userHomeSites)) {
            UserHomeSite addUserHomeSite = HomeSiteUtil.getAddUserHomeSite(userHomeSites.size() + 1);
            userHomeSiteApi.insert(addUserHomeSite);
        }
    }

    /**
     * 判断本地主页数据是否包含add图标
     * @param homeSites
     * @return
     */
    public boolean isIncludeAddHomeSite(List<HomeSite> homeSites) {
        if (ListUtils.isEmpty(homeSites)) {
            return false;
        }
        for (HomeSite homeSite : homeSites) {
            if (TextUtils.equals(HomeSiteUtil.SITE_ID_ADD,homeSite.getSiteId())) {
                return true;
            }
        }
        return false;
    }

    public String getHomeSiteSyncTime(String userID) {
        if (TextUtils.isEmpty(userID)) {
            return "";
        }
        String key = userID + "_" + KEY_SYNC_SETTING_LOCAL;
        return ConfigWrapper.get(key, "");
    }

    /**
     * 将用户主页图标数据写入文件
     * @return
     */
    public boolean createLocalHomeSite() {
        List<UserHomeSite> userHomeSites = null;
        String json = "";
        File file = null;
        try {
            file = new File(getLocalHomesiteFile());
            userHomeSites = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context)).queryAllOrderByOrder();
            if (ListUtils.isEmpty(userHomeSites)) {
                List<HomeSite> localHomeSites = SiteManager.getInstance().getLocalHomeSites();
                userHomeSites = AdapterConvertor.listFormatUserHomeSite(localHomeSites);
            }
            json = JsonParser.toJson(userHomeSites);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
        return FileUtils.writeFile(file, ZipUtil.gZip(json.toString().getBytes()));
    }

    /**
     * 将本地主页图标数据返回并插入到用户数据表中
     * @return
     * @throws SQLException
     */
    public List<HomeSite> ListFormatUserHomeSite() throws SQLException {
        HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
        List<HomeSite> localHomeSites= homeSiteApi.queryAllOrderByOrder();
        UserHomeSiteApi userHomeSiteApi = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
        userHomeSiteApi.clear();
        synHomeSiteInsertUserHomeSite(localHomeSites, userHomeSiteApi);
        return localHomeSites;
    }

    /**
     * 用户数据表中没有数据，将本地数据表中数据移植到用户数据表中
     * @throws Exception
     */
    public void LocalToUserHomesite() throws Exception {
        if (AccountLoginManager.getInstance().isUserLogined()) {
            UserHomeSiteApi userHomeSiteApi = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            userHomeSiteApi.clear();
                HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
                List<HomeSite> localHomeSites= homeSiteApi.queryAllOrderByOrder();
                synHomeSiteInsertUserHomeSite(localHomeSites, userHomeSiteApi);
        }
    }
}
