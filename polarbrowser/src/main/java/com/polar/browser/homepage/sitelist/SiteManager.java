package com.polar.browser.homepage.sitelist;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.polar.browser.JuziApp;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.homepage.customlogo.JsonParser;
import com.polar.browser.homepage.sitelist.recommand.exception.HomeSiteExistException;
import com.polar.browser.homepage.sitelist.recommand.exception.OutOfMaxNumberException;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.push.api.IAllSiteListCallback;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.UserHomeSiteManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.SiteList;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.SiteInfo;
import com.polar.browser.vclibrary.bean.db.SiteListVersion;
import com.polar.browser.vclibrary.bean.db.UserHomeSite;
import com.polar.browser.vclibrary.bean.events.SyncDatabaseEvent;
import com.polar.browser.vclibrary.bean.events.SyncHomeSiteEvent;
import com.polar.browser.vclibrary.common.CommonCallback;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.db.HomeSiteApi;
import com.polar.browser.vclibrary.db.SiteInfoApi;
import com.polar.browser.vclibrary.db.SiteListVersionApi;
import com.polar.browser.vclibrary.db.UserHomeSiteApi;
import com.polar.browser.vclibrary.network.ResultCallback;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.AdapterConvertor;
import com.polar.browser.vclibrary.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;

import static com.polar.browser.homepage.HomeSiteUtil.SITE_ID_ADD;

/**
 * Created by James on 2016/9/19.
 * 保存首页图标
 */

public class SiteManager {
    public static final String TAG = "SiteManager";
    public static final int MAX_LOGO_SIZE = 20;
    private static SiteManager instance;
    private Context context;

    private SiteManager(){
        this.context = JuziApp.getAppContext();
    }

    public static SiteManager getInstance(){
        if (instance == null) {
            synchronized (SiteManager.class) {
                if (instance == null) {
                    instance = new SiteManager();
                }
            }
        }
        return instance;
    }

//    public void init(Context context) {
//        this.context = context;
//    }

    private List<HomeSite> localHomeSites = null;
    private List<HistoryRecord> filterHistory = null;
    /**
     * 初始化数据
     */
    public void initHomeData(String mcc, String area, String language) throws IOException, SQLException {
        String config = AdapterConvertor.getConfig(mcc, area, language);
        localHomeSites = getConfigFile(config);
        int localSize = localHomeSites.size();
        localHomeSites.add(HomeSiteUtil.getMoreHomeSite(localSize));
        localHomeSites.add(HomeSiteUtil.getAddHomeSite(localSize+1));

        Collections.sort(localHomeSites, new Comparator<HomeSite>() {
            @Override
            public int compare(HomeSite lhs, HomeSite rhs) {
                return (int) (lhs.getOrder() - rhs.getOrder());
            }
        });
        final HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
        homeSiteApi.transaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                homeSiteApi.clear();
                SiteListVersionApi.getInstance(CustomOpenHelper.getInstance(context)).clear();
                SiteInfoApi.getInstance(CustomOpenHelper.getInstance(context)).clear();
                //先插入预置网址,再插入自定义网址
                for (HomeSite homeSite : localHomeSites) {
                    homeSiteApi.insert(homeSite);
                }
                return null;
            }
        });
        ConfigManager.getInstance().setHomeSiteInited();
    }

    public List<HomeSite> getLocalHomeSites() {
        if (ListUtils.isEmpty(localHomeSites)) {
            try {
                localHomeSites = getAllHomeSite();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return localHomeSites;
    }

    public List<HistoryRecord> getHistoryRecords() {

        if (ListUtils.isEmpty(filterHistory)) {
            try {
                List<HistoryRecord> historyRecords = HistoryRecordApi.getInstance(
                        CustomOpenHelper.getInstance(JuziApp.getAppContext())).queryHistoryVisitedByCount(5);
//                List<HistoryRecord> historyRecords = HistoryRecordApi.getInstance(
//                        CustomOpenHelper.getInstance(JuziApp.getAppContext())).queryAllHistoryRecordByTS(5);
                filterHistory = new ArrayList<>();
                for (int i = 0; i < historyRecords.size(); i++) {
                    HistoryRecord historyRecord = historyRecords.get(i);
                    boolean historyRecordExistHome = historyRecordExistHome(historyRecord);
                    if (!historyRecordExistHome) {
                        filterHistory.add(historyRecord);
                    }
                }
                if (filterHistory.size() == 5 && ConfigManager.getInstance().isHistoryRecordCountEngine()) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY_VISITED, GoogleConfigDefine.VISITED_SITE_COUNT);
                    ConfigManager.getInstance().setHistoryRecordCountEngine();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return filterHistory;
    }

    public List<HistoryRecord> getPreHistoryRecords() {
        return filterHistory;
    }

    public void updateHistoryRecords(List<HistoryRecord> historyRecordList) {
        filterHistory = historyRecordList;
        if (historyRecordList == null) {
            EventBus.getDefault().post(new SyncDatabaseEvent(SyncDatabaseEvent.TYPE_VISITED_RECORD));
        }
    }

    /**
     * 同步首页LOGO
     */
    private String mSiteListVersion = null;
    public void syncHomeSiteByService(final int type) {
        try {
            SiteListVersion siteListVersion = SiteListVersionApi.getInstance(CustomOpenHelper.getInstance(context)).query(type);
            if (siteListVersion != null) {
                mSiteListVersion = siteListVersion.getVersion();
            }
            Api.getInstance().siteList(mSiteListVersion,type).enqueue(new ResultCallback<SiteList>() {
                @Override
                public void success(final SiteList data, Call<Result<SiteList>> call, Response<Result<SiteList>> response) throws Exception {
                    ThreadManager.postTaskToIOHandler(new Runnable() {
                        @Override
                        public void run() {
                            String serviceSiteListVersion= data.getSiteListVersion();
                            if (!TextUtils.equals(serviceSiteListVersion,mSiteListVersion)) {
                                SiteListVersion siteListVersion = new SiteListVersion();
                                siteListVersion.setType(type);
                                siteListVersion.setVersion(serviceSiteListVersion);

                                saveSiteList2Local(type, siteListVersion, data, new CommonCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) throws Exception {

                                    }
                                });
                            }
                        }
                    });
                }

                @Override
                public void error(Call<Result<SiteList>> call, Throwable t) {
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<HomeSite> getConfigFile(String config) throws IOException {
        String fileName = "homesite_" + config + ".txt";
        InputStream inputStream = context.getAssets().open(fileName);
        String json = Okio.buffer(Okio.source(inputStream)).readUtf8();
        return JsonParser.fromJson(json, new TypeToken<List<HomeSite>>() {
        }.getType());

    }

    /**
     * 重新请求首页数据
     *
     * @return
     */
    public List<HomeSite> getAllHomeSite() throws SQLException {
        if(AccountLoginManager.getInstance().isUserLogined()){
            List<UserHomeSite> userHomeSites = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context)).queryAllOrderByOrder();
            if(ListUtils.isEmpty(userHomeSites)){
                return UserHomeSiteManager.getInstance().ListFormatUserHomeSite();
            } else {
                return AdapterConvertor.listFormatHomeSite(userHomeSites);
            }

        }else {
            HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            return homeSiteApi.queryAllOrderByOrder();
        }
    }

    /**
     * site是否存在于首页
     *
     * @param site
     * @return
     */
    public boolean exist(Site site) throws SQLException {
        List<HomeSite> homeSites = getAllHomeSite();
        if (Util.isCollectionEmpty(homeSites)) {
            return false;
        }
        //优先比较siteId,其次比较siteAddr
        String siteId = site.getSiteId();
        String siteAddr = site.getSiteAddr();
        for (HomeSite homeSite : homeSites) {
            if (!TextUtils.isEmpty(homeSite.getSiteId()) && Util.equals(homeSite.getSiteId(), siteId)) {
                return true;
            }
            if (UrlUtils.checkUrlIsSame(homeSite.getSiteAddr(), siteAddr)) {
                return true;
            }
        }
        return false;
    }

    public boolean historyRecordExistHome(HistoryRecord historyRecord) throws SQLException {
        List<HomeSite> homeSites = getAllHomeSite();
        if (Util.isCollectionEmpty(homeSites)) {
            return false;
        }
        String historyAddr = historyRecord.getHistoryAddr();
        for (HomeSite homeSite : homeSites) {
            if (!TextUtils.isEmpty(historyAddr) && UrlUtils.checkUrlIsSame(homeSite.getSiteAddr(), historyAddr)) {
                return true;
            }
        }
        return false;
    }

    public List<HistoryRecord> filterHistoryVisited(List<HistoryRecord> historyRecords) throws SQLException{
        List<HistoryRecord> filterHistory = new ArrayList<>();
        List<HomeSite> homeSites = getAllHomeSite();

        if (ListUtils.isEmpty(homeSites)) {
            return historyRecords;
        }

        for (int i = 0; i < historyRecords.size(); i++) {
            HistoryRecord historyRecord = historyRecords.get(i);
            for (int i1 = 0; i1 < homeSites.size(); i1++) {
                if (!TextUtils.equals(historyRecord.getHistoryAddr(),homeSites.get(i1).getSiteAddr())) {
                    filterHistory.add(historyRecord);
                }
            }
        }
        return filterHistory;
    }


    /**
     * 增加site到首页
     *
     * @param site
     * @param custom 是否是用户自定义
     * @throws SQLException
     */
    public boolean add2Home(Site site, boolean custom) throws SQLException, OutOfMaxNumberException, HomeSiteExistException {
        if(AccountLoginManager.getInstance().isUserLogined()){//登录状态下
            UserHomeSiteApi userHomeSiteApi = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            if (site.getSiteAddr() == null || site.getSiteName() == null) {
                return false;
            }
            UserHomeSite addHomeSite = userHomeSiteApi.queryBySiteId(SITE_ID_ADD);
            if (addHomeSite == null) {
                return false;
//            long count = homeSiteApi.count();
//            homeSiteApi.insert(HomeSiteUtil.getAddHomeSite(count + 1));
            }
            long order = addHomeSite.getOrder();
            if (order >= MAX_LOGO_SIZE - 1) {
                throw new OutOfMaxNumberException();
            }
            if (exist(site)) {
                throw new HomeSiteExistException();
            }
            userHomeSiteApi.insert(new UserHomeSite(site, order, custom));
            userHomeSiteApi.updateHomeSiteOrder(addHomeSite, order + 1);
            //检查首页logo是否有改动
            EventBus.getDefault().post(new SyncHomeSiteEvent());
            return true;
        }

        HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
        if (site.getSiteAddr() == null || site.getSiteName() == null) {
            return false;
        }

        HomeSite addHomeSite = homeSiteApi.queryBySiteId(SITE_ID_ADD);

        if (addHomeSite == null) {
            return false;
//            long count = homeSiteApi.count();
//            homeSiteApi.insert(HomeSiteUtil.getAddHomeSite(count + 1));
        }
        long order = addHomeSite.getOrder();
        if (order >= MAX_LOGO_SIZE - 1) {
            throw new OutOfMaxNumberException();
        }
        if (exist(site)) {
            throw new HomeSiteExistException();
        }
        homeSiteApi.insert(new HomeSite(site, order, custom));
        homeSiteApi.updateHomeSiteOrder(addHomeSite, order + 1);
        //检查首页logo是否有改动
        ConfigManager.getInstance().setCheckModifiedHomeSite(true);
        EventBus.getDefault().post(new SyncHomeSiteEvent());
        return true;
    }

    /**
     * 批量更新数据, 作为事务进行处理
     *
     * @param homeSites
     * @param <T>
     * @throws SQLException
     */
    /*public <T> void update(final List<HomeSite> homeSites) throws SQLException {
        final HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
        homeSiteApi.transaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (HomeSite homeSite : homeSites) {
                    homeSiteApi.update(homeSite);
                }
                return null;
            }
        });
    }*/


    /**
     * 同步siteInfo里的数据到homeSite数据库
     *
     * @throws SQLException
     */
    public void syncHomeSitesFromSiteInfo() throws SQLException {
        HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
        List<HomeSite> homeSites = homeSiteApi.queryAllOrderByOrder();
        if (Util.isCollectionEmpty(homeSites)) {
            return;
        }
        SiteInfoApi siteInfoApi = SiteInfoApi.getInstance(CustomOpenHelper.getInstance(context));
        for (HomeSite homeSite : homeSites) {
            SiteInfo siteInfo = siteInfoApi.query(homeSite);
            if (siteInfo != null) {
                HomeSite newHomeSite = new HomeSite(siteInfo, homeSite.getId(), homeSite.getOrder(), false);
                homeSiteApi.update(newHomeSite);
            }
        }
    }

    /**
     * 保存网络请求的数据到本地数据库
     *
     * @param type
     * @param siteListVersion
     * @param sites
     * @param callback
     */
    public void saveSiteList2Local(final int type, SiteListVersion siteListVersion, final SiteList sites, CommonCallback<Void> callback) {
        SimpleLog.d(TAG, "saveSiteList2Local");

        try {
            final SiteInfoApi siteInfoApi = SiteInfoApi.getInstance(CustomOpenHelper.getInstance(context));
            SiteListVersionApi siteListVersionApi = SiteListVersionApi.getInstance(CustomOpenHelper.getInstance(context));
            siteInfoApi.transaction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    siteInfoApi.delete(type);
                    siteInfoApi.insert(sites.getSiteList(), type);
                    return null;
                }
            });
            if (siteListVersion.getId() == 0) {
                siteListVersionApi.insert(siteListVersion);
            } else {
                siteListVersionApi.update(siteListVersion);
            }
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    /**
     * 保存首页Logo网络请求的数据到本地数据库
     * @param homeSites
     */
    public void saveHomeSiteList2Local(final List<HomeSite> homeSites) {
        SimpleLog.d(TAG, "saveHomeSiteList2Local");
        try {
            final HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            homeSiteApi.transaction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    homeSiteApi.clear();
                    if (!ListUtils.isEmpty(homeSites)) {
                        for (int i = 0; i < homeSites.size(); i++) {
                            homeSiteApi.insert(homeSites);
                        }
                    }
                    return null;
                }

            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载本地数据库里的内容
     *
     * @param type
     * @param callback
     */
    public void loadSiteListFromLocal(int type, CommonCallback<List<Site>> callback) {
        SimpleLog.d(TAG, "loadSiteListFromLocal");

        try {
            SiteInfoApi siteInfoApi = SiteInfoApi.getInstance(CustomOpenHelper.getInstance(context));
            List<Site> sites = siteInfoApi.queryAllSite(type);
            List<Site> filterSites = new ArrayList<>();
            for (Site site : sites) {
               if (!exist(site)) {
                   filterSites.add(site);
                }
            }
            callback.onSuccess(filterSites);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    /**
     * 从site表加载首页logo数据
     * @param type
     * @param callback
     */
    public void loadHomeSiteFromLocad(final int type, final IAllSiteListCallback callback) {


        try {
            SiteInfoApi siteInfoApi = SiteInfoApi.getInstance(CustomOpenHelper.getInstance(context));
            List<Site> sites = siteInfoApi.queryAllSite(type);
            if (ListUtils.isEmpty(sites)) {
                if (callback != null) {
                    callback.listIsNull();
                }
            } else {
                if (callback != null) {
                    callback.notifyQueryResult(sites);
                }
            }
        } catch (SQLException e) {
            if (callback != null) {
                callback.error(e);
            }
        }

//
//        ThreadManager.postTaskToIOHandler(new Runnable() {
//            @Override
//            public void run() {
//                SiteInfoApi siteInfoApi = SiteInfoApi.getInstance();
//                try {
//                    List<Site> sites = siteInfoApi.queryAllSite(type);
//                    if (ListUtils.isEmpty(sites)) {
//                        if (callback != null) {
//                            callback.listIsNull();
//                        }
//                    } else {
//                        if (callback != null) {
//                            callback.notifyQueryResult(sites);
//                        }
//                    }
//                } catch (SQLException e) {
//                    if (callback != null) {
//                        callback.error(e);
//                    }
//                }
//            }
//        });
    }

    /**
     * 清空本地数据库的内容
     *
     * @param type
     * @param callback
     */
    public void clearLocalSiteList(final int type, final CommonCallback<Void> callback) {
        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {

                try {
                    SiteInfoApi siteInfoApi = SiteInfoApi.getInstance(CustomOpenHelper.getInstance(context));
                    siteInfoApi.delete(type);
                    callback.onSuccess(null);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * 删除homeSite, 先判断siteId,再判断siteAddr
     *
     * @param homeSite
     * @param callback
     */
    public synchronized void delete(HomeSite homeSite, CommonCallback<Void> callback) {

        try {
            HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            String siteId = homeSite.getSiteId();
            long order = homeSite.getOrder();
            List<HomeSite> homeSites = homeSiteApi.queryOrderGreaterThan(order);
            homeSiteApi.delete(homeSite);
            if (!Util.isCollectionEmpty(homeSites)) {
                for (int i = 0; i < homeSites.size(); i++) {
                    HomeSite site = homeSites.get(i);
                    homeSiteApi.updateHomeSiteOrder(site, site.getOrder() - 1);
                }
            }
            callback.onSuccess(null);
            EventBus.getDefault().post(new SyncHomeSiteEvent());
            SimpleLog.d(TAG, order + "is deleted success sync home site");
        } catch (SQLException e) {
            callback.onError(e);
            e.printStackTrace();
        } catch (Exception e) {
            callback.onError(e);
            e.printStackTrace();
        }

    }

    public <T> void changePosition(int dragPostion, int dropPostion, CommonCallback<T> callback) {

        try {
            HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            homeSiteApi.changePosition(dragPostion, dropPostion, callback);
            EventBus.getDefault().post(new SyncHomeSiteEvent());
        } catch (SQLException e) {
            callback.onError(e);
            e.printStackTrace();
        }
    }


    public void reset(List<HomeSite> homeSites) throws SQLException {
        if (AccountLoginManager.getInstance().isUserLogined()) {
            UserHomeSiteApi userHomeSiteApi = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            userHomeSiteApi.clear();
            List<UserHomeSite> userHomeSites = new ArrayList<>();
            for (HomeSite homeSite : homeSites) {
                UserHomeSite userHomeSite = new UserHomeSite();
                userHomeSite.setSiteId(homeSite.getSiteId());
                userHomeSite.setSiteName(homeSite.getSiteName());
                userHomeSite.setSiteAddr(homeSite.getSiteAddr());
                userHomeSite.setSitePic(homeSite.getSitePic());
                userHomeSite.setOrder(homeSite.getOrder());
                userHomeSite.setCustom(homeSite.isCustom());
                userHomeSite.setId(homeSite.getId());
                userHomeSites.add(userHomeSite);
            }
            userHomeSiteApi.insert(userHomeSites);
        } else {
            HomeSiteApi homeSiteApi = HomeSiteApi.getInstance(CustomOpenHelper.getInstance(context));
            homeSiteApi.clear();
            homeSiteApi.insert(homeSites);
        }
        EventBus.getDefault().post(new SyncHomeSiteEvent());
    }

    public void resetHomeSite() {
        if (localHomeSites != null) {
            localHomeSites = null;
        }
    }

    /**
     * 判断获取到的用户数据是否包含add图标
     * @param userHomeSites
     * @return
     */
    public boolean isIncludeAddHomeSite(List<UserHomeSite> userHomeSites) {
        if (ListUtils.isEmpty(userHomeSites)) {
            return false;
        }
        for (UserHomeSite userHomeSite : userHomeSites) {
            if (TextUtils.equals(HomeSiteUtil.SITE_ID_ADD,userHomeSite.getSiteId())) {
                return true;
            }
        }
        return false;
    }

}
