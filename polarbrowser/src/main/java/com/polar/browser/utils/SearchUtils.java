package com.polar.browser.utils;

import android.content.Context;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.history.MyUrlUtil;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.SearchEngineList;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchUtils {
    private static final String DEFAULT = "1";
    private static final String NO_DEFAULT = "0";

    public static String buildSearchUrl(String content, Context c) {
        try {
            content = URLEncoder.encode(content, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            SimpleLog.e(e);
        }
        return String.format("%s%s", getSearchEngine(c), content);
    }

    public static String getSearchEngine(Context c) {
        String searchEngine = null;
        String json = ConfigManager.getInstance().getLastEngineList();
        SearchEngineList engineList = new Gson().fromJson(json, SearchEngineList.class);
        int pos = ConfigManager.getInstance().getSearchEngine();
        if (engineList != null && !ListUtils.isEmpty(engineList.getDataList())) {
            SearchEngineList.DataListBean engineBean = engineList.getDataList().get(pos);
            searchEngine = engineBean.getUrl();
            if(ListUtils.isEmpty(engineBean.getParamMap())){
                searchEngine = Statistics.appendFirstArg(searchEngine, engineBean.getEngineKeyWord(), "");
            }else{
                List<SearchEngineList.DataListBean.ParamMapBean> paramMap = engineBean.getParamMap();
                SearchEngineList.DataListBean.ParamMapBean firstParam =paramMap.remove(0);
                searchEngine = Statistics.appendFirstArg(searchEngine, firstParam.getName(), firstParam.getValue());
                for (SearchEngineList.DataListBean.ParamMapBean paramMapBean : paramMap) {
                    if (paramMapBean.getName().contains("#")) {
                        String paramKey = paramMapBean.getName();
                        paramKey = paramKey.replace("#", "");
                        searchEngine = Statistics.appendSpecialArg(searchEngine, paramKey, paramMapBean.getValue());
                    } else {
                        searchEngine = Statistics.appendArg(searchEngine, paramMapBean.getName(), paramMapBean.getValue());
                    }

                }

                if (engineBean.getEngineKeyWord().contains("#")) {
                    String paramKey = engineBean.getEngineKeyWord();
                    paramKey = paramKey.replace("#", "");
                    searchEngine = Statistics.appendSpecialArg(searchEngine, paramKey, "");
                } else {
                    searchEngine = Statistics.appendArg(searchEngine, engineBean.getEngineKeyWord(), "");
                }

            }
        }

   /*     else {

            switch (pos) {
                case ConfigDefine.SEARCH_ENGINE_GOOGLE:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_google);
                    break;
                case ConfigDefine.SEARCH_ENGINE_BING:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_bing);
                    break;
                case ConfigDefine.SEARCH_ENGINE_YAHOO:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_Yahoo);
                    break;
                case ConfigDefine.SEARCH_ENGINE_YANDEX:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_yandex);
                    searchEngine = Statistics.appendFirstArg(searchEngine, "clid", "2278773");
                    searchEngine = Statistics.appendArg(searchEngine, "text", "");   //engineKeyWord = "text"
                    break;
                case ConfigDefine.SEARCH_ENGINE_DUCKGO:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_duckgo);
                    break;
                case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_youtube);
                    break;
                case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_google_quick);
                    searchEngine = Statistics.appendFirstArg(searchEngine, "ie", "UTF-8");
                    searchEngine = Statistics.appendArg(searchEngine, "source", "browser");
                    searchEngine = Statistics.appendSpecialArg(searchEngine, "gsc.q", "");   //engineKeyWord = "#gsc.q"

                    break;
                default:
                    searchEngine = c.getResources().getString(R.string.search_engine_url_google_quick);
                    searchEngine = Statistics.appendFirstArg(searchEngine, "ie", "UTF-8");
                    searchEngine = Statistics.appendArg(searchEngine, "source", "browser");
                    searchEngine = Statistics.appendSpecialArg(searchEngine, "gsc.q", "");
                    break;
            }
        }
*/
        return searchEngine;
    }

    public static String getEngineNameByEngineType() {
        String searchEngine = "";
        String json = ConfigManager.getInstance().getLastEngineList();
        SearchEngineList engineList = new Gson().fromJson(json, SearchEngineList.class);
        int pos = ConfigManager.getInstance().getSearchEngine();
        if(engineList!=null && !ListUtils.isEmpty(engineList.getDataList())){
            try{
                return engineList.getDataList().get(pos).getEngineName();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
  /*    else{

            switch (pos) {
                case ConfigDefine.SEARCH_ENGINE_GOOGLE:
                    searchEngine = GoogleConfigDefine.ENGINE_GOOGLE;
                    break;
                case ConfigDefine.SEARCH_ENGINE_BING:
                    searchEngine = GoogleConfigDefine.ENGINE_BING;
                    break;
                case ConfigDefine.SEARCH_ENGINE_YAHOO:
                    searchEngine = GoogleConfigDefine.ENGINE_YAHOO;
                    break;
                case ConfigDefine.SEARCH_ENGINE_YANDEX:
                    searchEngine = GoogleConfigDefine.ENGINE_YANDEX;
                    break;
                case ConfigDefine.SEARCH_ENGINE_DUCKGO:
                    searchEngine = GoogleConfigDefine.ENGINE_DUCKDUCKGO;
                    break;
                case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
                    searchEngine = GoogleConfigDefine.ENGINE_YOUTUBE;
                    break;
                case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
                    searchEngine = GoogleConfigDefine.ENGINE_GOOGLE_QUICKSEARCH;
                    break;
                default:
                    break;
            }
        }*/
        return searchEngine;
    }




    public static String getSearchKey(String url) {
        String json = ConfigManager.getInstance().getLastEngineList();
        SearchEngineList engineList = new Gson().fromJson(json, SearchEngineList.class);

        int pos = getEnginFromUrl(url, engineList);
        Map<String, String> requestParamsMap = MyUrlUtil.getRequestParamMap(url);
        if (requestParamsMap == null) return "";
        String searchKey = null;
        if (engineList != null && !ListUtils.isEmpty(engineList.getDataList())) {
            String keyWord = engineList.getDataList().get(pos).getEngineKeyWord();
            if(keyWord.contains("#")) keyWord = keyWord.replace("#", "");
            searchKey = requestParamsMap.get(keyWord);
        }
/*        else {
            switch (pos) {

                case ConfigDefine.SEARCH_ENGINE_GOOGLE:
                    searchKey = requestParamsMap.get("q");
                    break;
                case ConfigDefine.SEARCH_ENGINE_BING:
                    searchKey = requestParamsMap.get("q");
                    break;

                case ConfigDefine.SEARCH_ENGINE_YAHOO:
                    searchKey = requestParamsMap.get("p");
                    break;
                case ConfigDefine.SEARCH_ENGINE_YANDEX:
                    searchKey = requestParamsMap.get("text");
                    break;
                case ConfigDefine.SEARCH_ENGINE_DUCKGO:
                    searchKey = requestParamsMap.get("q");
                    break;
                case ConfigDefine.SEARCH_ENGINE_YOUTUBE:

                    searchKey = requestParamsMap.get("search_query");
                    break;
                case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
                    searchKey = requestParamsMap.get("gsc.q");

                    break;
                default:
                    break;
            }
        }*/

        return searchKey;
    }

    private static int getEnginFromUrl(String url, SearchEngineList engineList) { //TODO
        int position = 0;
        try {
//            String host = new URL(url).getHost();

            if (engineList != null && !ListUtils.isEmpty(engineList.getDataList())) {
                Collections.sort(engineList.getDataList());
                for (int i = 0; i < engineList.getDataList().size(); i++) {
                    if (isSameHost(engineList.getDataList().get(i).getUrl(), url)) {
                        position = i;
                    }
                }
            }
/*            else {
                if (host.contains("google")) {
                    position = ConfigDefine.SEARCH_ENGINE_GOOGLE;
                } else if (host.contains("yahoo")) {
                    position = ConfigDefine.SEARCH_ENGINE_YAHOO;
                } else if (host.contains("yandex")) {
                    position = ConfigDefine.SEARCH_ENGINE_YANDEX;
                } else if (host.contains("duckduckgo")) {
                    position = ConfigDefine.SEARCH_ENGINE_DUCKGO;
                } else if (host.contains("youtube")) {
                    position = ConfigDefine.SEARCH_ENGINE_YOUTUBE;
                } else if (host.contains("bing")) {

                    position = ConfigDefine.SEARCH_ENGINE_BING;
                } else {
                    position = ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK;
                }
            }*/
        }
/*      catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        catch (Exception e){
            e.printStackTrace();
        }
        return position;
    }

    /**
     * 检查url是不是搜索出来的
     */
    public static boolean checkIsSearch(String itemUrl) {
        String engineListJson = ConfigManager.getInstance().getLastEngineList();
        Gson gson = new Gson();
        SearchEngineList engineList = gson.fromJson(engineListJson, SearchEngineList.class);
        boolean isSearch = false;
        if (engineList != null && !ListUtils.isEmpty(engineList.getDataList())) {
            for (SearchEngineList.DataListBean dataListBean : engineList.getDataList()) {
                if ((isSameHost(dataListBean.getRedirectUrl(),itemUrl)||(isSameHost(dataListBean.getUrl(), itemUrl)&&
                        itemUrl.contains(dataListBean.getEngineKeyWord())))) {
                    isSearch = true;
                }
            }
        }
 /*       else if (itemUrl.contains("google") && itemUrl.contains("search?q=")) {
            isSearch = true;
        } else if (itemUrl.contains("search.yahoo.com/mobile/s?p=")) {
            isSearch = true;
        } else if (itemUrl.contains("yandex.ru/search")) {
            isSearch = true;
        } else if (itemUrl.contains("duckduckgo.com/?q=")) {
            isSearch = true;
        } else if (itemUrl.contains("m.youtube.com/results?search_query")) {
            isSearch = true;
        } else if (itemUrl.contains("bing.com/search?q=")) {
            isSearch = true;
        } else if (itemUrl.contains("quicksearch.start.fyi/search?")) {
            isSearch = true;
        }*/
        return isSearch;
    }

    private static void updateDefaultEnginePosition(SearchEngineList searchEngineList) {

        List<SearchEngineList.DataListBean> srcList = searchEngineList.getDataList();
        List<SearchEngineList.DataListBean> copyList = new ArrayList<>(srcList);
        //本地默认搜索引擎 ，服务端是否已经删掉。
        boolean isLocalDefaultEngineExist = false;
        int defaultEngineIndex = getDefaultEngineIndex(searchEngineList);
        String currEngineUrl = getSearchEngine(JuziApp.getInstance());
        try {
            for (int i = 0; i < srcList.size(); i++) {
                SearchEngineList.DataListBean bean = srcList.get(i);
                if (isSameHost(bean.getUrl(),currEngineUrl)||bean.getEngineName().equals(getEngineNameByEngineType())) {// 本地默认引擎，服务端同样存在。
                    isLocalDefaultEngineExist = true;
                    if (bean.getIsDefault().equals(NO_DEFAULT)) {
                        copyList.get(i).setIsDefault(DEFAULT);
                        ConfigManager.getInstance().setSearchEngine(i,true);
                    }
                }else{
                    copyList.get(i).setIsDefault(NO_DEFAULT);
                }
            }



            if(!isLocalDefaultEngineExist){ //本地搜索引擎 已经被服务端删掉，更新本地存储 默认搜索引擎。
                copyList.get(defaultEngineIndex).setIsDefault(DEFAULT);
                ConfigManager.getInstance().setSearchEngine(defaultEngineIndex,true);
            }else{ //本地 默认搜索引擎 服务端存在，才 更改。
                searchEngineList.setDataList(copyList);
            }

            Gson gson = new Gson();
            ConfigManager.getInstance().saveSearchEngineList(gson.toJson(searchEngineList));

        } catch (Exception ignored) {

        }

    }

    private static void saveDefaultEngine(SearchEngineList searchEngineList) {
        List<SearchEngineList.DataListBean> dataList = searchEngineList.getDataList();
        for (int i = 0; i < dataList.size(); i++) {
            SearchEngineList.DataListBean dataListBean = dataList.get(i);
            if (DEFAULT.equals(dataListBean.getIsDefault())) {
                ConfigManager.getInstance().setSearchEngine(i,true);
            }
        }

    }


    static boolean isSameDomain(String url,String url2){
        boolean isSame = false;
        URI uri1;
        URI uri2;
        try {
            if(TextUtils.isEmpty(url)||TextUtils.isEmpty(url2))return false;
            uri1 = new URI(url);
            uri2 = new URI(url2);
            if (uri1.getHost() == null || uri2.getHost() == null) {
                return false;
            }
            isSame = uri1.getHost().equals(uri2.getHost());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return isSame;
    }

    public static boolean isSameHost(String url, String url2) {
        return MyUrlUtil.isSameHost(url, url2);

    }

    @MainThread
    private static void downloadEngineIcon(final List<SearchEngineList.DataListBean> dataList) {
        ThreadManager.postTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                final ImageView imageView = new ImageView(JuziApp.getAppContext());
                for (SearchEngineList.DataListBean dataListBean : dataList) {
                    ImageLoadUtils.loadImage(JuziApp.getAppContext(),dataListBean.getEnginePic(),imageView);
                }
            }
        });

    }

    private static final String TAG = SearchUtils.class.getSimpleName();

    public static void initEngine(final ConfigManager mConfigManager) {
        final Gson gson = new Gson();
        final String searchEngineStr = (mConfigManager.getSearchEngineList() != null
                && !mConfigManager.getSearchEngineList().isEmpty()) ? mConfigManager.getSearchEngineList() : DEFAULT_SEARCH_ENGINE_CONFIG;
        final SearchEngineList localEngine = gson.fromJson(searchEngineStr, SearchEngineList.class);

        SimpleLog.d(TAG,"searchEngineList -- getSearchEngineList =  "+mConfigManager.getSearchEngineList()+" searchEngineStr = "+searchEngineStr);

        //开发时必须保证localEngine!=null
        if(AppEnv.DEBUG && localEngine == null) {
            CustomToastUtils.getInstance().showTextToast("search engine init config parser error");
        }

        //使用默认配置时，SearchEngineVersion为空字符
        if(localEngine==null || "".equals(mConfigManager.getSearchEngineVersion())){
            if ("ru".equalsIgnoreCase(SystemUtils.getLan()) && ConfigDefine.SEARCH_ENGINE_YANDEX == mConfigManager.getSearchEngine()) {
                mConfigManager.setDefaultSearchEngineModified();
            }else if(mConfigManager.getSearchEngine()!=ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK){
                mConfigManager.setDefaultSearchEngineModified();
            }
        }

        if (localEngine != null && !ListUtils.isEmpty(localEngine.getDataList())) {//本地 存有上次下载的 搜索引擎数据
//            downloadEngineIcon(localEngine.getDataList());//下载搜索引擎图标
            ThreadManager.getIOHandler().post(new Runnable() {
                @Override
                public void run() {

                    if(!mConfigManager.isModifiedDefaultSearchEngine()){ //用户没修改过 默认引擎，使用 下载的数据 覆盖本地 默认引擎。

                        saveDefaultEngine(localEngine);
                    } else{//修改过，更新默认搜索引擎为用户修改的。
                        updateDefaultEnginePosition(localEngine);
                    }
                    SimpleLog.d(TAG,"searchEngineList -- setLastEngineList =  "+searchEngineStr);
                    mConfigManager.setLastEngineList(searchEngineStr);
                }
            });
        }



        Api.getInstance().searchEngineList(mConfigManager.getSearchEngineVersion()).enqueue(new Callback<Result<SearchEngineList>>() {
            @Override
            public void onResponse(Call<Result<SearchEngineList>> call, Response<Result<SearchEngineList>> response) {
                if (response == null||response.body()==null)
                    return;
                Result<SearchEngineList> body = response.body();
                SearchEngineList bean = body.getData();

                SimpleLog.d(TAG,"searchEngineList -- saveSearchEngineList =  "+gson.toJson(bean));

                if(bean!=null){
                    mConfigManager.setSearchEngineVersion(body.getData().getVersion());
                    if(!ListUtils.isEmpty(bean.getDataList())){
                        mConfigManager.saveSearchEngineList(gson.toJson(bean));
//                        downloadEngineIcon(bean.getDataList());//下载搜索引擎图标
                    }

                }

            }

            @Override
            public void onFailure(Call<Result<SearchEngineList>> call, Throwable t) {
                SimpleLog.d(TAG,"searchEngineList -- onFailure ");
                mConfigManager.setSearchEngineVersion("");
            }
        });
    }

    private static int getDefaultEngineIndex(SearchEngineList engineList) {
        int defaultIndex = 0;
        if(!ListUtils.isEmpty(engineList.getDataList())){
            for (int i = 0; i < engineList.getDataList().size(); i++) {
                if(DEFAULT.equals(engineList.getDataList().get(i).getIsDefault())){
                    defaultIndex=i;
                    break;
                }
            }
        }
        return  defaultIndex;
    }

    public static int getDefaultEngineIconByName(String engineName){  //TODO optimize

        int resourceId = R.drawable.engin_default_bg;
        if(engineName == null) return resourceId;

        if(ConfigDefine.SEARCH_ENGINE_NAME_MAP.containsKey(engineName.toLowerCase())){
            resourceId =  ConfigDefine.SEARCH_ENGINE_NAME_MAP.get(engineName.toLowerCase())[0];
        }

        return resourceId;
    }

    /**
     * 根据服务端下发的英文获取对应多语言
     * @param englishName
     * @return
     */
    public static String getMultiLanByEn(String englishName){
        try{
           if(ConfigDefine.SEARCH_ENGINE_NAME_MAP.containsKey(englishName.toLowerCase())){
               return JuziApp.getAppContext().getResources().getString(ConfigDefine.SEARCH_ENGINE_NAME_MAP.get(englishName.toLowerCase())[1]);
           }
        }catch (Exception e){
           e.printStackTrace();
        }
        return englishName != null ? englishName : "";
    }

    private static final String DEFAULT_SEARCH_ENGINE_CONFIG =
            "{\"dataList\":" +
                    "[{\"engineKeyWord\":\"q\",\"engineName\":\"Google\",\"enginePic\":\"http://vc-file-bucket.s3-accelerate.amazonaws.com/5ea106afe04c4456a44c65a0278fa096{D653D256-8827-7BFF-8CBA-64803B4CFD1C}.png\",\"enginePosition\":\"1\",\"id\":1,\"isDefault\":\"0\",\"redirectUrl\":\"\",\"url\":\"https://www.google.com/search?\"}," +
                    "{\"engineKeyWord\":\"q\",\"engineName\":\"Bing\",\"enginePic\":\"http://vc-india.s3-accelerate.amazonaws.com/5974c2c14f5049e495641c8f13bf8eb6bing (2).png\",\"enginePosition\":\"2\",\"id\":2,\"isDefault\":\"0\",\"redirectUrl\":\"\",\"url\":\"http://cn.bing.com/search?\"}," +
                    "{\"engineKeyWord\":\"q\",\"engineName\":\"Yahoo\",\"enginePic\":\"http://vc-file-bucket.s3-accelerate.amazonaws.com/f9c8c9fed0fa45039a25d370f68d240e{91CF4E2E-A200-570D-D322-08C8FC1E565D}.png\",\"enginePosition\":\"3\",\"id\":3,\"isDefault\":\"0\",\"paramMap\":[{\"name\":\"c\",\"value\":\"733\"},{\"name\":\"o\",\"value\":\"144668\"},{\"name\":\"s\",\"value\":\"733\"}],\"redirectUrl\":\"https://search.yahoo.com/yhs/mobile/search?\",\"url\":\"http://s.joymedia.mobi/?\"}," +
                    "{\"engineKeyWord\":\"text\",\"engineName\":\"Yandex\",\"enginePic\":\"http://vc-file-bucket.s3-accelerate.amazonaws.com/d64e43e1de2c4fca8918106bee9dfaf8{EF06FCF8-8924-27EB-715A-395CB0547F56}.png\",\"enginePosition\":\"4\",\"id\":4,\"isDefault\":\"0\",\"paramMap\":[{\"name\":\"clid\",\"value\":\"2278773\"}],\"redirectUrl\":\"\",\"url\":\"https://yandex.ru/search/?\"}," +
                    "{\"engineKeyWord\":\"q\",\"engineName\":\"DuckDuckGo\",\"enginePic\":\"http://vc-file-bucket.s3-accelerate.amazonaws.com/0e777a5aa5dd485b9346840be727e08e{1DB636C6-79A6-E5B8-8E6A-A23AF45F9740}.png\",\"enginePosition\":\"5\",\"id\":5,\"isDefault\":\"0\",\"redirectUrl\":\"\",\"url\":\"https://duckduckgo.com/?\"}," +
                    "{\"engineKeyWord\":\"search_query\",\"engineName\":\"Youtube\",\"enginePic\":\"http://vc-file-bucket.s3-accelerate.amazonaws.com/e4c64fdf1fec46f6bc68a3cc4c8c98a2{D85E720F-7BE6-94ED-28DD-9E7D5498EFE1}.png\",\"enginePosition\":\"6\",\"id\":6,\"isDefault\":\"0\",\"redirectUrl\":\"\",\"url\":\"https://www.youtube.com/results?\"}," +
                    "{\"engineKeyWord\":\"#gsc.q\",\"engineName\":\"Google Quicksearch\",\"enginePic\":\"http://vc-india.s3-accelerate.amazonaws.com/ba8568e923bd467594dcb785f94dcafa{89755171-677E-F4FA-F8C4-C4D85D4EBD89}.png\",\"enginePosition\":\"7\",\"id\":7,\"isDefault\":\"1\",\"paramMap\":[{\"name\":\"ie\",\"value\":\"UTF-8\"},{\"name\":\"source\",\"value\":\"browser\"}],\"redirectUrl\":\"\",\"url\":\"https://quicksearch.start.fyi/search?\"}]," +
                    "\"version\":\"1487227038452\"}";



}
