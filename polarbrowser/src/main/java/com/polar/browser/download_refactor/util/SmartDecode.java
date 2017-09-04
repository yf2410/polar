
package com.polar.browser.download_refactor.util;

import android.text.TextUtils;

import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * 一些解码类的集合
 * 
 * @author caisenchuan
 */
public class SmartDecode {

    private static final String TAG = SmartDecode.class.getSimpleName();

    public static final String CHARSET_UNKNOWN = "unknown";
    public static final String CHARSET_GB2312 = "GB2312";
    public static final String CHARSET_UTF8 = "UTF-8";

    public enum Encoding {
        /** Base64编码 */
        BASE64,
        /** QuotedPrintable编码 */
        QP
    }

    /**
     * Mime解码的查找表元素
     */
    private static class MimeDecodeLookup {
        /** 字符串索引 */
        public String indexStr;
        /** 对应的编码方式 */
        public Encoding encoding;
        /** 对应的字符集 */
        public String charset;

        public MimeDecodeLookup(String indexStr, Encoding encoding, String charset) {
            this.indexStr = indexStr;
            this.encoding = encoding;
            this.charset = charset;
        }
    }

    /** 查找表 */
    private static ArrayList<MimeDecodeLookup> mMimeDecodeLookupTable = new ArrayList<MimeDecodeLookup>();
    static {
        // 初始化查找表
        mMimeDecodeLookupTable
                .add(new MimeDecodeLookup("=?utf8?b?", Encoding.BASE64, CHARSET_UTF8));
        mMimeDecodeLookupTable.add(new MimeDecodeLookup("=?utf8?q?", Encoding.QP, CHARSET_UTF8));
        mMimeDecodeLookupTable
                .add(new MimeDecodeLookup("=?utf-8?b?", Encoding.BASE64, CHARSET_UTF8));
        mMimeDecodeLookupTable.add(new MimeDecodeLookup("=?utf-8?q?", Encoding.QP, CHARSET_UTF8));
        mMimeDecodeLookupTable.add(new MimeDecodeLookup("=?gb2312?b?", Encoding.BASE64,
                CHARSET_GB2312));
        mMimeDecodeLookupTable
                .add(new MimeDecodeLookup("=?gb2312?q?", Encoding.QP, CHARSET_GB2312));
    }

    /** 网站与对应的编码方式列表 */
    private static String[][] mUrlList = {
            {
                    "preview.mail.163.com", CHARSET_UTF8
            }, // 163预览
            {
                    "preview.mail.126.com", CHARSET_UTF8
            }, // 126预览
            {
                    "mm.mail.163.com", CHARSET_UTF8
            }, // WAP版163
            {
                    "mm.mail.126.com", CHARSET_UTF8
            }, // WAP版126
            {
                    "mail.163.com", CHARSET_GB2312
            }, // 163其他
            {
                    "mail.126.com", CHARSET_GB2312
            }
            // 126其他
    };

    /**
     * 若输入字符串是Mime格式（Multipurpose Internet Mail Extensions）编码，则返回解码后的字符串
     * 
     * @param str mime编码的字符串，格式定义如下:<br />
     *            =?charset?encoding?data?possibly repeated?=
     * @return
     */
    public static String mimeDecode(String str) {
        String ret = str;

        if (!TextUtils.isEmpty(str)) {
            String lowercaseString = str.toLowerCase();

            // 判断是否以?=结尾
            int end_pos = lowercaseString.indexOf("?=");
            if (end_pos > 0) {
                // 遍历查找表，匹配字符串索引
                for (MimeDecodeLookup lookup : mMimeDecodeLookupTable) {
                    String indexStr = lookup.indexStr;
//                    if(KLog.DEBUG){
//                        KLog.d(TAG, "mimeDecode(), try : %s", indexStr);
//                    }

                    int start_pos = lowercaseString.indexOf(indexStr);
                    // 若能匹配上，则进行解码
                    if ((start_pos >= 0) &&
                            (start_pos + indexStr.length() < end_pos)) {
                        try {
                            // 截取出正文部分
                            String content_str = str.substring(start_pos + indexStr.length(),
                                    end_pos);
//                            if(KLog.DEBUG){
//                                KLog.d(TAG, "mimeDecode(), content_str : %s", content_str);
//                            }
                            // 进行解码
                            content_str = decode(content_str, lookup.encoding, lookup.charset);
//                            if(KLog.DEBUG){
//                                KLog.d(TAG, "mimeDecode(), decode content_str : %s", content_str);
//                            }
                            // 将解码后的字符串拼接上编码以外的字符部分返回
                            ret = str.substring(0, start_pos) + content_str
                                    + str.substring(end_pos + 2);
                            break;
                        } catch (Exception e) {
//                            if(KLog.DEBUG){
//                                KLog.w(TAG, "mimeDecode(), Exception", e);
//                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    /**
     * 根据字符集以及编码方式进行解码
     * 
     * @param str
     * @param enc
     * @param charset
     * @return
     */
    public static String decode(String str, Encoding enc, String charset) {
        String ret = str;

        if (enc == Encoding.BASE64) {
            ret = base64Decode(str, charset);
        } else if (enc == Encoding.QP) {
        	// TODO Fix me
//            ret = QuotedPrintable.decode(str, charset);
        } else {
            // ...
        }

        return ret;
    }
    
    /**
     * 进行Base64解码
     * 
     * @param str
     * @param charset
     * @return
     */
    public static String base64Decode(String str, String charset) {

        String ret = str;

        if (str != null) {
            try {
                byte[] tmp = Base64.decode(str.getBytes());
                ret = new String(tmp, charset);
            } catch (Exception e) {
//                if(KLog.DEBUG){
//                    KLog.w(TAG, "Exception", e);
//                }
            }
        }

        return ret;
    }
    

    /**
     * 根据网站的url判断其下载文件名的编码方式
     * 
     * @param url
     * @return 对应的编码方式,如果找不到则为CHARSET_UNKNOWN
     * @author caisenchuan
     */
    public static String getCharsetByUrl(String url) {
        String charset = CHARSET_UNKNOWN;
        if (TextUtils.isEmpty(url)) {
            return charset;
        }

        for (int i = 0; i < mUrlList.length; i++) {
            if (url.indexOf(mUrlList[i][0]) >= 0) {
                charset = mUrlList[i][1];
                break;
            }
        }

//        if(KLog.DEBUG){
//            KLog.d(TAG, "getCharsetByUrl(), url : " + url + ", charset : " + charset);
//        }

        return charset;
    }

    /**
     * 某个网站的下载文件名是否是GB2312编码
     * 
     * @param url
     * @return true - 是, false - 不是
     * @author caisenchuan
     */
    public static boolean isGB2312Url(String url) {
        String charset = getCharsetByUrl(url);
        if (charset != null && charset.equals(CHARSET_GB2312)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将GB2312或者UTF-8的原始编码字符串进行解码
     * 
     * @param srcString 编码字符串
     * @param Gb2312 指定编码格式，true代表GB2312，false代表UTF-8
     * @return
     */
    public static String rawDecode(String srcString, boolean Gb2312) {
        String ret = srcString;

        if (srcString != null) {
            byte[] b = srcString.getBytes();
            if (b == null || b.length < 1) {
                ret = srcString;
            } else {
                byte[] c = new byte[b.length];
                int pos = 0;
                for (int i = 0; i < b.length; i++) {
                    if (b[i] >= 0) {
                        c[pos] = b[i];
                        pos++;
                    } else if ((i + 1) < b.length) {
                        c[pos] = (byte) ((b[i] & 0x03) << 6 | (b[i + 1] & 0x3f));
                        pos++;
                        i++;
                    }
                }

                byte[] d = new byte[pos];
                for (int i = 0; i < pos; i++) {
                    d[i] = c[i];
                }

                try {
                    if (Gb2312) {
                        ret = new String(d, CHARSET_GB2312);
                    } else {
                        ret = new String(d, CHARSET_UTF8);
                    }
                } catch (Exception e) {
//                    if(KLog.DEBUG){
//                        KLog.e(TAG, "Exception", e);
//                    }
                    ret = "";
                }
            }
        }

        return ret;
    }

    /**
     * 从disposition中恢复出解码后的字符串
     * 
     * @param srcString 编码字符串
     * @param Gb2312 指定编码格式，true代表GB2312，false代表UTF-8
     * @param rawDecode 是否使用rawDecode
     * @return 解码后的字符串
     * @author zhouchenguang, YuYun, caisenchuan
     */
    public static String recoverString(String srcString, boolean Gb2312, boolean rawDecode) {
        String ret = srcString;
        if (TextUtils.isEmpty(srcString)) {
            return ret;
        }

//        if(KLog.DEBUG){
//            KLog.d(TAG, "srcString : %s , gb2312 : %s , rawDecode : %s", srcString, Gb2312, rawDecode);
//         }
        // 1. mime decode
        ret = SmartDecode.mimeDecode(srcString);
//        if(KLog.DEBUG){
//            KLog.d(TAG, "mimeDecode ret : %s", ret);
//        }
        if (!ret.equals(srcString)) {
            // 若解码后的字符串与解码前不同，则认为解码成功
            return ret;
        }

        // 2. URL decode
        try {
            if (Gb2312) {
                ret = URLDecoder.decode(srcString, CHARSET_GB2312);
            } else {
                ret = URLDecoder.decode(srcString, CHARSET_UTF8);
            }
        } catch (Exception e) {
//            if(KLog.DEBUG){
//                KLog.w(TAG, "URLDecoder.decode error", e);
//            }
        }
//        if(KLog.DEBUG){
//            KLog.d(TAG, "urldecode ret : %s", ret);
//        }
        if (!ret.equals(srcString)) {
            // 若解码后的字符串与解码前不同，则认为解码成功
            return ret;
        }

        // 3. raw decode
        if (rawDecode) {
            ret = rawDecode(srcString, Gb2312);
//            if(KLog.DEBUG){
//                KLog.d(TAG, "rawDecode ret : %s", ret);
//            }
        }

        return ret;
    }
}
