package com.polar.browser.history;

import android.text.TextUtils;

import com.polar.browser.utils.SimpleLog;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by saifei on 16/9/22.
 */

public class MyUrlUtil {

    private static final String TAG = "MyUrlUtil";
    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrlPage(String strURL) {
        if (TextUtils.isEmpty(strURL)) {
            return null;
        }
        String strAllParam = null;
        strURL = strURL.trim().toLowerCase();
        String[] arrSplit = strURL.split("[?]");

        //有参数
        if (arrSplit.length > 1) {
            if (arrSplit[1] != null) {
                strAllParam = arrSplit[1];
            }
        }

        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> getRequestParamMap(String URL) {
        if (TextUtils.isEmpty(URL)) {
            return null;
        }

        String strUrlParam = truncateUrlPage(URL);//得到参数
        if (TextUtils.isEmpty(strUrlParam)) {
            return null;
        }

        Map<String, String> mapRequest = new HashMap<String, String>();
        //每个键值为一组
        String[] arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            if (strSplit.contains("#")) {
                String[] temp = strSplit.split("#");
                if (temp.length > 1) {
                    String[] keyValueArray = temp[1].split("=");
                    if (keyValueArray.length > 1)
                        mapRequest.put(temp[1].split("=")[0], temp[1].split("=")[1]);
                }
                continue;
            }
            String[] arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if (arrSplitEqual.length > 1) {
                if (!TextUtils.isEmpty(arrSplitEqual[1])) {
                    mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);//正确解析
                } else {
                    mapRequest.put(arrSplitEqual[0], "");//无value
                }
            }
        }
        return mapRequest;
    }

    public static boolean isSameHost(String currUrl, String lastUrl) {

        String currUrlDomain = getDomain(currUrl);
        String lastUrlDomain = getDomain(lastUrl);

        return !(TextUtils.isEmpty(currUrlDomain) || TextUtils.isEmpty(lastUrlDomain))
                && currUrlDomain.equals(lastUrlDomain);

    }


    public static String getDomain(String url) {
        try{
            Pattern p = Pattern.compile("[\\w-]+\\.(com.cn|net.cn|gov.cn|org.cn|com|net|org|gov|cc|biz|info|cn|co)\\b()*");
            Matcher matcher = p.matcher(url);
            matcher.find();
            return matcher.group();
        }catch (Exception e){
            SimpleLog.e(TAG,"url="+url+"-----erro="+e.toString());
        }
        return "";
    }
}
