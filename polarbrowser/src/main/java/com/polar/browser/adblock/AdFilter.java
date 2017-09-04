package com.polar.browser.adblock;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Pair;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.utils.ahocorasick.Emit;
import com.polar.browser.utils.ahocorasick.Trie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdFilter {

    private static final String TAG = "AdFilter";
    private static final boolean DEBUG = false;
    // Get the rule from sdcard, set it with true.
    public static final boolean DEBUG_RULE = false;
    public static final String ADBLOCK_URL_RULE = "rawAdblock.dat";
    public static final String ADBLOCK_CSS_RULE = "winAdblock.dat";
    //    public static final int LIB_NONE = 0;
    public static final int LIB_URL = 1;
    public static final int LIB_CSS_COOKIE = 1 << 1;

    public static final byte RES_TYPE_UNDEF = 0;
    public static final byte RES_TYPE_HTML = 1;
    public static final byte RES_TYPE_PIC = 1 << 1;
    public static final byte RES_TYPE_JS = 1 << 2;
    public static final byte RES_TYPE_CSS = 1 << 3;
    public static final byte RES_TYPE_XML = 1 << 4;
    public static final byte RES_TYPE_NOT_HTML = 1;
    public static final byte RES_TYPE_NOT_PIC = 1 << 1;
    public static final byte RES_TYPE_NOT_JS = 1 << 2;
    public static final byte RES_TYPE_NOT_CSS = 1 << 3;

    public static final byte OPTION_UNDEF = 0;
    public static final byte OPTION_THIRD_PARTY_VALID = 1;
    public static final byte OPTION_FIRST_PARTY_VALID = 1 << 1;
    public static final byte OPTION_THIRD_PARTY_INVALID = 1;
    public static final byte OPTION_FIRST_PARTY_INVALID = 1 << 1;
    // There aren't options below in our libadurlrules. But it is in AdBlock's options' list.
    //public static final byte OPTION_MATCH_CASE_VALID = 1 << 2;
    //public static final byte OPTION_COLLAPSSE_VALID = 1 << 3;
    //public static final byte OPTION_DO_NOT_TRACK_VALID = 1 << 3;

    public static final byte URL_OPTION_HTTP_PREFIX = 1; // url rule: ||
    public static final byte URL_OPTION_START_WITH = 1 << 1; // url rule: |www.baidu.com
    public static final byte URL_OPTION_END_WITH = 1 << 2; // url rule: www.baidu.com|
    public static final byte URL_OPTION_WILD_CARD = 1 << 3; // url rule: www.baidu.com^

    private static final Pattern RE_JS = Pattern.compile("\\.js$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_CSS = Pattern.compile("\\.css$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_XML = Pattern.compile("\\.xml$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_IMAGE = Pattern.compile("\\.(?:gif|png|jpe?g|bmp|ico)$", Pattern.CASE_INSENSITIVE);
    //private static final Pattern RE_FONT = Pattern.compile("\\.(?:ttf|woff)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_HTML = Pattern.compile("\\.html?$", Pattern.CASE_INSENSITIVE);

    private static AdFilter mInstance = null;
    private boolean mAdBlockEnabled = ConfigManager.getInstance().isAdBlock();
    private static File mFilesPath;

    private class AdRawRuleSet {
        public String mRawRule;
        public byte mRuleOption = 0;
        public Vector<String> mValidDomain = new Vector<String>();
        public Vector<String> mInvalidDomain = new Vector<String>();
        public byte mValidType = RES_TYPE_UNDEF;
        public byte mInvalidType = RES_TYPE_UNDEF;
        public byte mValidOption = OPTION_UNDEF;
        public byte mInvalidOption = OPTION_UNDEF;
        public AdRawRuleSet() {}
    };

    private Trie mUrlFinder = null;
    private Trie mUrlFinderB = null;
    private Map<String, AdRawRuleSet> mAdRawRuleList = null;
    private Map<String, AdRawRuleSet> mAdRawRuleListB = null;
    private boolean mUrlRulesUpdated = false;
    private byte[] mUrlRulesLock = new byte[0];

    private Trie mCssFinder = null;
    private Trie mCssFinderB = null;
    private Map<String, String> mCssMap = null;
    private Map<String, String> mCssMapB = null;
    private Map<String, Vector<Pair<String, String> > > mLocalStorageMap = null;
    private Map<String, Vector<Pair<String, String> > > mLocalStorageMapB = null;
    private boolean mCssCookieUpdated = false;
    private byte[] mCssCookieLock = new byte[0];

    public static byte PicData[] = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, (byte) 0xbf, (byte) 0x8a, (byte) 0xac,
            0x67, 0x00, 0x00, 0x00, 0x19, 0x74, 0x45, 0x58, 0x74, 0x53, 0x6f, 0x66, 0x74, 0x77, 0x61, 0x72,
            0x65, 0x00, 0x41, 0x64, 0x6f, 0x62, 0x65, 0x20, 0x49, 0x6d, 0x61, 0x67, 0x65, 0x52, 0x65, 0x61,
            0x64, 0x79, 0x71, (byte) 0xc9, 0x65, 0x3c, 0x00, 0x00, 0x00, 0x12, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0xda,
            0x62, (byte) 0xfc, (byte) 0xff, (byte) 0xff, 0x3f, (byte) 0xc3, 0x40, 0x02, (byte) 0x80, 0x00, 0x03, 0x00, 0x7c, (byte) 0x94, 0x02, (byte) 0xff,
            (byte) 0x9f, 0x5c, (byte) 0xfc, (byte) 0xf0, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4e, 0x44, (byte) 0xae, 0x42, 0x60, (byte) 0x82
    };
    public static String HtmData = "<HTML><HEAD></HEAD><BODY></BODY></HTML>";
    public static String JsData = "(function(){});";
    public static String XmlData = "<?xml version=\"1.0\"?><root></root>";
    private static final String addLocalStorageFunction = " var addLocalStorage = function(key, value) {" +
            "if (localStorage[key] != value) {" +
            "if (value.indexOf('$gmttime$') >= 0) {" +
            "var now = new Date();" +
            "now.setDate(now.getDate() + 365);" +
            "value = value.replace(/\\$gmttime\\$/gim, now.getTime());" +
            "}" +
            "localStorage.setItem(key, value);" +
            "}" +
            "};";
    private static final String addStyleFunction = "var addNewStyle = function(styleId, newStyle) {"
            +"var styleElement = document.getElementById(styleId);"
            +"if (!styleElement) {"
            +   "styleElement = document.createElement('style');"
            +   "styleElement.type = 'text/css';"
            +   "styleElement.id = styleId;"
            +   "try {"
            +       "styleElement.appendChild(document.createTextNode(newStyle));"
            +   "}"
            +   "catch (ex) {"
            +       "styleElement.styleSheet.cssText = newStyle;"
            +   "}"
            +   "var head = document.getElementsByTagName('head');"
            +   "if (!head || !head[0]) {"
            +       "var headElement = document.createElement('head');"
            +       "document.documentElement.insertBefore(headElement, document.body);"
            +       "head = document.getElementsByTagName('head');"
            +    "}"
            +    "if (head && head[0]) {"
            +        "head[0].appendChild(styleElement);"
            +    "}"
            +"}"
            +"};";

    public static AdFilter getInstance() {
        if(null == mInstance) {
            synchronized (AdFilter.class) {
                if(null == mInstance) {
                    mInstance = new AdFilter();
                }
            }
        }
        return mInstance;
    }

    public static void injectScriptIntoWebview(final WebView webview, final String pluginScript) {
        if (!TextUtils.isEmpty(pluginScript) && webview != null) {
            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            webview.evaluateJavascript(pluginScript, new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object arg0) {
                                    if (DEBUG)
                                        SimpleLog.e(TAG, "onReceiveValue:" + arg0.toString());
                                }
                            });
                        } else {
                            webview.loadUrl(CommonData.EXEC_JAVASCRIPT + pluginScript);
                        }
                    } catch (Throwable e) {
                    }
                }
            });
        }
    }

    private AdFilter() {
        if (DEBUG_RULE)
            mFilesPath = Environment.getExternalStorageDirectory();
        else
            mFilesPath = JuziApp.getAppContext().getFilesDir();
    }

    public File getFilesPath() {
        return mFilesPath;
    }

    public void init() {
        updateAdBlockRules(LIB_URL);
        updateAdBlockRules(LIB_CSS_COOKIE);
        SimpleLog.d(TAG, "init");
    }

    public void updateAdBlockRules(int type) {
        SimpleLog.e(TAG, "updateAdBlockRules type:" + type);
        try {
            if (type == LIB_URL)
                readUrlRules();
            if (type == LIB_CSS_COOKIE)
                readCssCookieRules();
        } catch (Throwable e) {
            SimpleLog.e(TAG, "updateAdBlockRules type:" + type + ",Throwable:" + e.getMessage());
        }

//test url rules
/*
if (type == LIB_CSS_COOKIE)
    return;
/duilian.
/advertise/*
||37cs.com^
||gs307*.com^
||cpro.bai^du.com
||u.u*n^ionli.com
||*images.sohu.com
||^cnxad.com
|sina.com.cn/litong/
|sina.com.cn/litong/|
|^sina.com.cn/litong/
|*sina.com.cn/litong/
*/
/*        boolean a;
        a = shouldBlockUrlSync("a","www.b/duilian.com" );
        a = shouldBlockUrlSync("a","www.b/duilian");
        a = shouldBlockUrlSync("a", "/duilian.");
        a = shouldBlockUrlSync("a","ww./advertise/");
        a = shouldBlockUrlSync("a","sfkfjkf./advertise/133");
        a = shouldBlockUrlSync("a", "http://37cs.com");
        a = shouldBlockUrlSync("a","http://37cs.com1");
        a = shouldBlockUrlSync("a","http://37cs.coma");
        a = shouldBlockUrlSync("a","http://37cs.com_");
        a = shouldBlockUrlSync("a","http://37cs.com-");
        a = shouldBlockUrlSync("a","http://37cs.com.");
        a = shouldBlockUrlSync("a","http://37cs.com%");
        a = shouldBlockUrlSync("a","http://37cs.com/");
        a = shouldBlockUrlSync("a","http://37cs.com:");
        a = shouldBlockUrlSync("a","http://37cs.com?");
        a = shouldBlockUrlSync("a","https://37cs.com&");
        a = shouldBlockUrlSync("a","https://www.37cs.com&");
        a = shouldBlockUrlSync("a","https://www.afsf37cs.com&http://37cs.com=");
        a = shouldBlockUrlSync("a","http://gs307.com");
        a = shouldBlockUrlSync("a","http://gs307.com=");
        a = shouldBlockUrlSync("a","http://gs307afsfsf.com");
        a = shouldBlockUrlSync("a","http://gs307afsfsf.com:");
        a = shouldBlockUrlSync("a","http://cpro.bai&du.com");
        a = shouldBlockUrlSync("a","http://cpro.bai1du.coma");
        a = shouldBlockUrlSync("a","http://u.un?ionli.coma");
        a = shouldBlockUrlSync("a","http://images.sohu.coma");
        a = shouldBlockUrlSync("a","http://safkimages.sohu.com");
        a = shouldBlockUrlSync("a", "http://?cnxad.coma");
        a = shouldBlockUrlSync("a","http://acnxad.coma");
        a = shouldBlockUrlSync("a","www.sina.com/litong/skf");
        a = shouldBlockUrlSync("a","sina.com/litong/");
        a = shouldBlockUrlSync("a","?sina.com.cn/litong/skf");
        a = shouldBlockUrlSync("a","1sina.com.cn/litong/skf");
        */
    }

    public String getCssAndLocalStorageScript(String host) {
        String script = null;
        try {
            script = getAdBlockLocalStorageScript(host) + getAdBlockCssScript(host);
        } catch (Throwable e) {
            script = null;
        }
        if (DEBUG) {
            SimpleLog.e(TAG, "getCssAndLocalStorageScript:" + script);
        }
        return script;
    }

    private String getCurrentTimeByType(String type) {
        String res = new String();
        Time time = new Time("GMT+8");
        time.setToNow();
        String year = Integer.toString(time.year);
        String month = String.format("%02d", time.month+1);
        String day = String.format("%02d", time.monthDay);
        if (type.equals("minus_y_m_d"))
            res = year + "-" + month + "-" + day;
        else if (type.equals("minus_m_d_y"))
            res = month + "-" + day + "-" + year;
        else if (type.equals("minus_d_m_y"))
            res = day + "-" + month + "-" + year;
        else if (type.equals("ymd"))
            res = year + month + day;
        else if (type.equals("mdy"))
            res = month + day + year;
        else if (type.equals("dmy"))
            res = day + month + year;
        else if (type.equals("virgule_y_m_d"))
            res = year + "/" + month + "/" + day;
        else if (type.equals("virgule_m_d_y"))
            res = month + "/" + day + "/" + year;
        else if (type.equals("virgule_d_m_y"))
            res = day + "/" + month + "/" + year;
        else if (type.equals("dot_y_m_d"))
            res = year + "." + month + "." + day;
        else if (type.equals("dot_m_d_y"))
            res = month + "." + day + "." + year;
        else if (type.equals("dot_d_m_y"))
            res = day + "." + month + "." + year;
        return res;
    }

    private String fillLocalStorageScriptTimeOption(String value) {
        String prefix = "$time_type_";
        int pos = value.indexOf(prefix);
        int end = -1;
        while (pos >= 0) {
            end = value.indexOf("$", pos + prefix.length());
            if (end <= 0)
                break;
            else {
                String type = value.substring(pos + prefix.length(), end);
                String time = getCurrentTimeByType(type);
                String reg = "\\$time_type_" + type + "\\$";
                value = value.replaceAll(reg, time);
            }
            pos = value.indexOf("$time_type_", pos);
        }
        return value;
    }

    private String getAdBlockLocalStorageScript(String host)
    {
        String callFunctionScript = "";
        swapLibIfNeeded(LIB_CSS_COOKIE);
        if (mLocalStorageMap == null)
            return "";
        Iterator iter = mLocalStorageMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (host.contains((CharSequence) entry.getKey())) {
                Vector<Pair<String, String>> values = (Vector<Pair<String, String>>) entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    String value = fillLocalStorageScriptTimeOption(values.elementAt(i).second);
                    callFunctionScript += "addLocalStorage(" + values.elementAt(i).first + "," + value + ");";
                }
            }
        }
        if (callFunctionScript.isEmpty())
            return "";
        else {
            return addLocalStorageFunction + callFunctionScript;
        }
    }

    private String getAdBlockCssScript(String host)
    {
        String cssRules = getCssRules(host);
        if (TextUtils.isEmpty(cssRules))
            return "";
        return addStyleFunction + "addNewStyle('VC_ADBLOCK_SPECIAL', '" + cssRules + "{display:none !important;}');";
    }

    private String getCssRules(String host)
    {
        swapLibIfNeeded(LIB_CSS_COOKIE);
        if (TextUtils.isEmpty(host) || mCssFinder == null || mCssMap == null)
            return null;
        StringBuilder res = new StringBuilder();
        try {
            Collection<Emit> emits = mCssFinder.parseText(host);
            Iterator<Emit> it = emits.iterator();
            if (it != null) {
                while (it.hasNext()) {
                    if (res.length() == 0)
                        res.append(mCssMap.get(it.next().getKeyword()));
                    else {
                        res.append(',');
                        res.append(mCssMap.get(it.next().getKeyword()));
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG)
                SimpleLog.e(TAG, "getCssRules error:" + e.toString());
            return null;
        }
        return res.toString();
    }

    private void addCssRule(String domain, String rule)
    {
        String value = mCssMapB.get(domain);
        if (value == null) {
            mCssMapB.put(domain, rule);
            mCssFinderB.addKeyword(domain);
        }
        else {
            String newRule = value + "," + rule;
            mCssMapB.put(domain, newRule);
        }
    }

    private void swapLibIfNeeded(int type) {
        switch (type) {
            case  LIB_URL:
                if (mAdRawRuleListB != null) {
                    synchronized(mUrlRulesLock) {
                        if (mUrlRulesUpdated) {
                            mAdRawRuleList = mAdRawRuleListB;
                            mUrlFinder = mUrlFinderB;
                            mUrlFinderB = null;
                            mAdRawRuleListB = null;
                            mUrlRulesUpdated = false;
                        }
                    }
                }
                break;
            case LIB_CSS_COOKIE:
                if (mLocalStorageMapB != null) {
                    synchronized (mCssCookieLock) {
                        if (mCssCookieUpdated) {
                            mCssFinder = mCssFinderB;
                            mCssFinderB = null;
                            mCssMap = mCssMapB;
                            mCssMapB = null;
                            mLocalStorageMap = mLocalStorageMapB;
                            mLocalStorageMapB = null;
                            mCssCookieUpdated = false;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    private void readCssCookieRules() {
        File file = new File(mFilesPath, ADBLOCK_CSS_RULE);
        if(!file.exists()) {
            return;
        }
        synchronized (mCssCookieLock) {
            mCssCookieUpdated = false;
        }
        mCssFinderB = new Trie();
        mCssMapB = new HashMap<String, String>();
        mLocalStorageMapB = new HashMap<String, Vector<Pair<String, String> > >();
        BufferedReader bufferReader = null;
        try {
            bufferReader = new BufferedReader(new FileReader(file));
            String str;
            Outerloop:
            while (!TextUtils.isEmpty((str = bufferReader.readLine()))) {
                if (str.length() <= 2)
                    continue;
                String vecRule[] = new String[2];
                vecRule = str.split("##");
                if (vecRule.length != 2)
                    continue;
                if (vecRule[1].charAt(0) == '$') {
                    int begin = vecRule[1].indexOf("$type=");
                    int end = vecRule[1].indexOf(",");
                    if (begin >= 0 && begin < end) {
                        String type = vecRule[1].substring("$type=".length(), end + 1);
                        if (type.charAt(0) == 'l') {
                            begin = vecRule[1].indexOf("={");
                            end = vecRule[1].lastIndexOf("}");
                            if (begin > 0 && end > (begin +2)) {
                                String data = vecRule[1].substring(begin + "={".length(), end);
                                if ((begin = data.indexOf(',')) > 2) {
                                    String key = "";
                                    String value = "";
                                    if (data.charAt(0) == '\"' && data.charAt(begin -1) == '\"')
                                        key = data.substring(0, begin);
                                    if (data.charAt(begin + 1) == '\"' && data.charAt(data.length() - 1) == '\"') {
                                        value = data.substring(begin + 1, data.length());
                                    }
                                    if (!key.isEmpty() && !value.isEmpty()) {
                                        Vector<Pair<String, String>> res = mLocalStorageMapB.get(vecRule[0]);
                                        if (res != null) {
                                            res.add(new Pair<String, String>(key, value));
                                            mLocalStorageMapB.put(vecRule[0], res);
                                        } else {
                                            Vector<Pair<String, String>> item = new Vector<Pair<String, String>>();
                                            item.add(new Pair<String, String>(key, value));
                                            mLocalStorageMapB.put(vecRule[0], item);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    addCssRule(vecRule[0], vecRule[1]);
                }
            }
        } catch (Exception e) {
            mCssMapB = null;
            mLocalStorageMapB = null;
            mCssFinderB = null;
        } finally {
            try {
                if (bufferReader != null)
                    bufferReader.close();
            } catch (Exception ex) { }
        }

        if (mCssFinderB != null) {
            try {
                mCssFinderB.prepare();
            } catch (Throwable e) {
                mCssFinderB = null;
            }
            synchronized (mCssCookieLock) {
                mCssCookieUpdated = true;
            }
        }
    }

    private void readUrlRules() {
        File file = new File(mFilesPath,ADBLOCK_URL_RULE);
        if(!file.exists()) {
            return;
        }
        synchronized(mUrlRulesLock) {
            mUrlRulesUpdated = false;
        }
        mUrlFinderB = new Trie();
        mAdRawRuleListB = new HashMap<String, AdRawRuleSet>();
        BufferedReader bufferReader = null;
        try {
            bufferReader = new BufferedReader(new FileReader(file));
            String str;
            while (!TextUtils.isEmpty((str = bufferReader.readLine()))) {
                String vecRule[];
                if (str.charAt(0) == '!' || str.length() <= 2)
                    continue;
                AdRawRuleSet rawRuleSet = new AdRawRuleSet();
                vecRule = str.split("\\$");
                if (TextUtils.isEmpty(vecRule[0]))
                    continue;
                if (vecRule[0].charAt(0) == '*')
                    vecRule[0] = vecRule[0].substring(1);
                if (vecRule[0].charAt(vecRule[0].length() - 1) == '*')
                    vecRule[0] = vecRule[0].substring(0, vecRule[0].length() -1);
                rawRuleSet.mRawRule = vecRule[0];
                if (rawRuleSet.mRawRule.startsWith("||")) {
                    rawRuleSet.mRuleOption |= URL_OPTION_HTTP_PREFIX;
                    rawRuleSet.mRawRule = rawRuleSet.mRawRule.substring(2);
                } else if (rawRuleSet.mRawRule.startsWith("|")) {
                    rawRuleSet.mRuleOption |= URL_OPTION_START_WITH;
                    rawRuleSet.mRawRule = rawRuleSet.mRawRule.substring(1);
                }
                if (rawRuleSet.mRawRule.endsWith("|")) {
                    rawRuleSet.mRuleOption |= URL_OPTION_END_WITH;
                    rawRuleSet.mRawRule = rawRuleSet.mRawRule.substring(0, rawRuleSet.mRawRule.length() -1);
                }
                if (rawRuleSet.mRawRule.endsWith("^")) {
                    rawRuleSet.mRuleOption |= URL_OPTION_WILD_CARD;
                    rawRuleSet.mRawRule = rawRuleSet.mRawRule.substring(0, rawRuleSet.mRawRule.length() -1);
                }
                if (DEBUG)
                    SimpleLog.e(TAG,"raw rule:" + rawRuleSet.mRawRule);
                if (vecRule.length == 2 ) {
                    String secondVec[] = vecRule[1].split(",");
                    for (int i = 0; i < secondVec.length; i++) {
                        if (secondVec[i].length() < 4)
                            continue;
                        if (secondVec[i].charAt(0) == '~') {
                            if (secondVec[i].equals("~script"))
                                rawRuleSet.mInvalidType = (byte)(rawRuleSet.mInvalidType + RES_TYPE_NOT_JS);
                            else if (secondVec[i].equals("~image"))
                                rawRuleSet.mInvalidType = (byte)(rawRuleSet.mInvalidType + RES_TYPE_NOT_PIC);
                            else if (secondVec[i].equals("~stylesheet"))
                                rawRuleSet.mInvalidType = (byte)(rawRuleSet.mInvalidType + RES_TYPE_NOT_CSS);
                            else if (secondVec[i].equals("~subdocument") || secondVec[i].equals("~document"))
                                rawRuleSet.mInvalidType = (byte)(rawRuleSet.mInvalidType + RES_TYPE_NOT_HTML);
                            else if (secondVec[i].equals("~third-party"))
                                rawRuleSet.mInvalidOption = (byte)(rawRuleSet.mInvalidOption + OPTION_THIRD_PARTY_INVALID);
                            else if (secondVec[i].equals("~first-party"))
                                rawRuleSet.mInvalidOption = (byte)(rawRuleSet.mInvalidOption + OPTION_FIRST_PARTY_INVALID);
                        } else {
                            if (secondVec[i].startsWith("domain=")) {
                                int index = secondVec[i].indexOf("=");
                                if (index > 0) {
                                    String domainVec[] = secondVec[i].substring(index + 1).split("[|]");
                                    for (int j = 0; j < domainVec.length; j++) {
                                        if (domainVec[j].charAt(0) == '~')
                                            rawRuleSet.mInvalidDomain.add(domainVec[j].substring(1));
                                        else
                                            rawRuleSet.mValidDomain.add(domainVec[j]);
                                    }
                                }
                            } else if (secondVec[i].equals("script"))
                                rawRuleSet.mValidType = (byte)(rawRuleSet.mValidType + RES_TYPE_JS);
                            else if (secondVec[i].equals("stylesheet"))
                                rawRuleSet.mValidType = (byte)(rawRuleSet.mValidType + RES_TYPE_CSS);
                            else if (secondVec[i].equals("subdocument") || secondVec[i].equals("document"))
                                rawRuleSet.mValidType = (byte)(rawRuleSet.mValidType + RES_TYPE_HTML);
                            else if (secondVec[i].equals("image"))
                                rawRuleSet.mValidType = (byte)(rawRuleSet.mValidType + RES_TYPE_PIC);
                            else if (secondVec[i].equals("third-party"))
                                rawRuleSet.mValidOption = (byte)(rawRuleSet.mValidOption + OPTION_THIRD_PARTY_VALID);
                            else if (secondVec[i].equals("first-party"))
                                rawRuleSet.mValidOption = (byte)(rawRuleSet.mValidOption + OPTION_FIRST_PARTY_VALID);
                        }
                    }
                }
                mAdRawRuleListB.put(rawRuleSet.mRawRule, rawRuleSet);
                mUrlFinderB.addKeyword(rawRuleSet.mRawRule);
            }
        } catch (Exception e) {
            mAdRawRuleListB = null;
            mUrlFinderB = null;
        } finally {
            try {
                if (bufferReader != null)
                    bufferReader.close();
            } catch (Exception ex) { }
        }
        if (mUrlFinderB != null) {
            try {
                mUrlFinderB.prepare();
            } catch (Throwable e) {
                mUrlFinderB = null;
            }
            synchronized (mUrlRulesLock) {
                mUrlRulesUpdated = true;
            }
        }
    }

    public static byte getResourceType(String url) {
        if (RE_JS.matcher(url).find())
            return RES_TYPE_JS;
        else if (RE_CSS.matcher(url).find())
            return RES_TYPE_CSS;
        else if (RE_IMAGE.matcher(url).find())
            return RES_TYPE_PIC;
        else if (RE_HTML.matcher(url).find())
            return RES_TYPE_HTML;
        else if (RE_XML.matcher(url).find())
            return RES_TYPE_XML;
        else
            return RES_TYPE_UNDEF;
    }

    private boolean shouldPatternCheck(String data) {
        int index = data.indexOf('*');
        int index2 = data.indexOf('^');
        if (index >= 0 || index2 >= 0 || data.charAt(data.length() -1) == '|') {
            return true;
        }
        return false;
    }

    private boolean patternCheck(String rawrule, String url) {
        String rule;
        rule = rawrule.replaceAll("\\.", "\\\\.").replaceAll("\\-", "\\\\\\-").replaceAll("\\?", "\\\\\\?").replaceAll("\\*", "\\.\\*").replaceAll("\\^", "[^A-Za-z0-9\\\\-_\\\\\\.%]").replaceFirst("\\|\\|", "^https?://([^/]+\\\\.)?");
        if (rule.charAt(0) == '|' && rule.charAt(rule.length() - 1) == '|') {
            rule = rule.replaceFirst("\\|", "\\^\\(");
            rule = rule.replaceFirst("\\|", "\\$)");
        }
        if (rule.charAt(0) == '|') {
            rule = rule.replaceFirst("\\|", "\\^\\(") + ")";
        }
        if (rule.charAt(rule.length() - 1) == '|')
            rule = "(" + rule.replaceFirst("\\|", "\\$)");
        if (DEBUG)
            SimpleLog.e(TAG,"patternCheck rule:" + rule + ",url:" + url);
        Pattern pat = Pattern.compile(rule);
        Matcher mat = pat.matcher(url);
        if (mat.find()) {
            return true;
        }
        return false;
    }

    public boolean shouldBlockUrlSync(String mainUrl, String url) {
        SimpleLog.e(TAG, "shouldBlockUrlSync url:" + mainUrl + ",suburl;" + url);
        if (!mAdBlockEnabled)
            return false;
        swapLibIfNeeded(LIB_URL);
        if (mAdRawRuleList == null)
            return false;
        if (url.equalsIgnoreCase(mainUrl)) {
            // Shouldn't block main resource.
            return false;
        }
        try {
            String mainHost = UrlUtils.getHost(mainUrl);
            String subHost = UrlUtils.getHost(url);
            boolean urlMatched = false;
            AdRawRuleSet ruleSet = null;
            try {
                Collection<Emit> emits = mUrlFinder.parseText(url);
                Iterator<Emit> it = emits.iterator();
                if (it != null) {
                    while (it.hasNext()) {
                        urlMatched = true;
                        ruleSet = mAdRawRuleList.get(it.next().getKeyword());
                        if ((ruleSet.mRuleOption & URL_OPTION_HTTP_PREFIX) == URL_OPTION_HTTP_PREFIX) {
                            if (!url.startsWith("http://www." + ruleSet.mRawRule) && !url.startsWith("http://" + ruleSet.mRawRule)
                                                                                  && !url.startsWith("https://" + ruleSet.mRawRule)) {
                                return false;
                            }
                        }
                        if ((ruleSet.mRuleOption & URL_OPTION_START_WITH) == URL_OPTION_START_WITH) {
                            if (!url.startsWith(ruleSet.mRawRule))
                                return false;
                        }
                        if ((ruleSet.mRuleOption & URL_OPTION_END_WITH) == URL_OPTION_END_WITH) {
                            if (!url.endsWith(ruleSet.mRawRule))
                                return false;
                        }
                        if ((ruleSet.mRuleOption & URL_OPTION_WILD_CARD) == URL_OPTION_WILD_CARD) {
                            int index = url.indexOf(ruleSet.mRawRule);
                            if (index > 0) {
                                char option = url.charAt(index + ruleSet.mRawRule.length());
                                if (Character.isLetterOrDigit(option) || option == '_' || option == '-' || option == '.' || option == '%')
                                    return false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (DEBUG)
                    SimpleLog.e(TAG, "!!!!!!!!!!!!!error:" + e.toString());
                return false;
            }


            if (DEBUG)
                SimpleLog.e(TAG, "match==" + urlMatched + ",url==" + url + ",rawrule==" + ruleSet.mRawRule);
            if (urlMatched) {
                for (int j = 0; j < ruleSet.mValidDomain.size(); j++) {
                    if (mainHost.endsWith(ruleSet.mValidDomain.elementAt(j)))
                        break;
                    if ((j + 1) == ruleSet.mValidDomain.size())
                        return false;
                }
                for (int j = 0; j < ruleSet.mInvalidDomain.size(); j++) {
                    if (mainHost.endsWith(ruleSet.mInvalidDomain.elementAt(j)))
                        return false;
                }

                if (ruleSet.mValidOption > 0) {
                    if ((ruleSet.mValidOption & OPTION_THIRD_PARTY_VALID) == OPTION_THIRD_PARTY_VALID
                            && mainHost.equals(subHost))
                        return false;
                    if ((ruleSet.mValidOption & OPTION_FIRST_PARTY_VALID) == OPTION_FIRST_PARTY_VALID
                            && !mainHost.equals(subHost))
                        return false;
                } else if(ruleSet.mInvalidOption > 0) {
                    if ((ruleSet.mInvalidOption & OPTION_THIRD_PARTY_INVALID) == OPTION_THIRD_PARTY_INVALID
                            && !mainHost.equals(subHost))
                        return false;
                    if ((ruleSet.mValidOption & OPTION_FIRST_PARTY_INVALID) == OPTION_FIRST_PARTY_INVALID
                            && mainHost.equals(subHost))
                        return false;
                }

                byte type = getResourceType(url);
                if (type > 0) {
                    if (ruleSet.mValidType > 0 && (ruleSet.mValidType & type) != type)
                        return false;
                    if (ruleSet.mInvalidType > 0 && (ruleSet.mInvalidType & type) == type)
                        return false;
                }
                if (DEBUG) {
                    SimpleLog.e(TAG, "will block mainurl:" + mainUrl);
                    SimpleLog.e(TAG, "will block suburl:" + url);
                    SimpleLog.e(TAG, "mRawRule:" + ruleSet.mRawRule.toString());
                }
                return true;
            }
        } catch (Throwable e) {
            return false;
        }
        return false;
    }
}