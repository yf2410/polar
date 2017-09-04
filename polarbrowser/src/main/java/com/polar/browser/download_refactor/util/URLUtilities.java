// Copyright (c) 2010 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.polar.browser.download_refactor.util;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtilities {
    /**
     * URLs that can be handled by ChromeView
     */
    private static final String DOMAIN_END = ".com|.edu|.gov|.int|.mil|.net|.org|.biz|.info|.tv|.pro|.name|.museum|.coop|.aero|.xxx|.idv|";
    private static final Pattern FILTER_IMAGE_WEBSITE_SCHEMA = Pattern.compile("\\S*(www\\.pinterest\\.com|"
                                                                                  + "www\\.tumblr\\.com|"
                                                                                  + "imgur\\.com|"
                                                                                  + "instagram\\.com)\\S*"); // filtered_urls_split_by_vertical_line should be replaced by urls like  google\\.com|baidu\\.com

    /** 下载文件的默认名字 */
    public static final String DEFAULT_FILENAME = "downloadfile";

    /**
     * This function will get a simplified version of the url for pulling up
     * existing ChromeViews for a certain url. Currently it simply returns the
     * host.
     */
    public static String getBaseUrl(String fullUrl) {
        URL url;
        try {
            if (fullUrl == null || fullUrl.length() == 0)
                return null;
            fullUrl = fixUrl(fullUrl);
            url = new URL(fullUrl);
        } catch (MalformedURLException e) {
            return null;
        }
        return url.getHost();
    }

    public static String getBaseHost(String fulUrl)
    {
        if (fulUrl == null || fulUrl.length() == 0)
            return null;

        fulUrl = fulUrl.toLowerCase();
        String host = getBaseUrl(fulUrl);
        if (host == null || host.length() == 0)
            return null;

        String prefix;
        String middle;
        String end;
        int pos = host.indexOf("://");
        if (pos > 0)
            host = host.substring(pos + 3);
        pos = host.indexOf(":");
        if (pos > 0)
            host = host.substring(0, pos);

        pos = host.lastIndexOf(".");
        if (pos <= 0)
            return null;

        end = host.substring(pos + 1);
        host = host.substring(0, pos);
        pos = host.lastIndexOf(".");
        if (pos < 0)// only two sections
            return host + "." + end;

        middle = host.substring(pos + 1);
        if (DOMAIN_END.indexOf(end) >= 0)
        {
            return middle + "." + end;
        }
        else
        {
            host = host.substring(0, pos);
            pos = host.lastIndexOf(".");
            if (pos < 0)
                prefix = host;
            else
                prefix = host.substring(pos + 1);

            if (DOMAIN_END.indexOf("." + middle + "|") >= 0 && !prefix.equalsIgnoreCase("www"))
                return prefix + "." + middle + "." + end;
            else
                return middle + "." + end;
        }
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * Determine if the two URLs are for the same page. Currently it strips the
//      * hash location and then does a string comparison.
//      */
//     public static boolean samePage(String urlA, String urlB) {
//         if (urlA == null || urlB == null)
//             return false;
//         String base1 = urlA.split("#")[0];
//         String base2 = urlB.split("#")[0];
//         return (base1.equals(base2));
//     }

    /**
     * This function will fix up the url typed to try to make it a valid url for
     * loading in a ChromeView.
     * 
     * @param url
     */
    public static String fixUrl(String url) {
        // todo(feldstein): This needs to be beefed up and
        // ensure it still supports about:crash et al.
        if (url == null || url.trim().length() == 0)
            return url;

        if (url.indexOf("://") == -1 && !url.startsWith("about:") && !url.startsWith("data:"))
            url = "http://" + url;
        return url;
    }

    public static boolean isUrlStart(String str)
    {
        if (str == null || str.length() == 0)
            return false;

        if (str.indexOf("://") > 0 || str.startsWith("www.") || str.startsWith("3g.")
                || str.startsWith("m.") || str.startsWith("wap."))
            return true;

        return false;
    }

    public static boolean isMobile(String url)
    {
        if (url.startsWith("3g.") || url.startsWith("m.") || url.startsWith("wap.") ||
                url.indexOf("://3g.") > 0 || url.indexOf("://m.") > 0 || url.indexOf("://wap.") > 0)
            return true;
        else
            return false;
    }

    public static String getNakedUrl(String url) {
        if (url == null || url.length() == 0)
            return null;

        String lowcase = url.toLowerCase();
        if (lowcase.startsWith("http://"))
            url = url.substring(7);
        if (lowcase.startsWith("https://"))
            url = url.substring(8);
        if (lowcase.endsWith("/") && lowcase.length() >= 2 && url.length() > 0)
            url = url.substring(0, url.length() - 1);
        return url;
    }

    /**
     * Guesses canonical filename that a download would have, using the URL and
     * contentDisposition. File extension, if not defined, is added based on the
     * mimetype
     * 
     * @param url Url to the content
     * @param contentDisposition Content-Disposition HTTP header or null
     * @param mimeType Mime-type of the content or null
     * @param appendExtIfNeed append extension when the original filename
     *            doesn't have one, by caisenchuan
     * @return suggested filename
     */
    /**
     * http://blog.csdn.net/mociml/article/details/5326442
     * 限制文件名长度
     */
    public static final int FILENAME_MAX_LEN = 120;
    public static String guessFileName(
            String url,
            String contentDisposition,
            String mimeType,
            boolean appendExtIfNeed) {
        String filename = null;
        String extension = null;

        // If we couldn't do anything with the hint, move toward the content
        // disposition
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null && url != null) {
            String decodedUrl;
            try {
                decodedUrl = URLDecoder.decode(url, "UTF-8");
            } catch (Exception e) {
                decodedUrl = url;
            }

            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf('?');
                // If there is a query string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                
                queryIndex = decodedUrl.indexOf('#');
                // If there is a fragment string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                
                if (!decodedUrl.endsWith("/")) {
                    int index = decodedUrl.lastIndexOf('/') + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                        //文件名后面添加时间戳、避免文件重名
                        int lastDotIndex = filename.lastIndexOf(".");
                        String prefix = filename;
                        String suffix = "";
                        if (lastDotIndex > 0) {  //截取扩展名
                            prefix = filename.substring(0,lastDotIndex);
                            suffix = filename.substring(lastDotIndex);
                        }
                        filename = prefix + "_" + System.currentTimeMillis() + suffix;
                    }
                }
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = DEFAULT_FILENAME +"_" + System.currentTimeMillis();
        }

        // Split filename between base and extension
        // Add an extension if filename does not have one
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0 && appendExtIfNeed) {
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    extension = "." + extension;
                }
            }
            if (extension == null) {
                if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
                    if (mimeType.equalsIgnoreCase("text/html")) {
                        extension = ".html";
                    } else {
                        extension = ".txt";
                    }
                } else {
                    extension = ".bin";
                }
            }
        } else {
            if (dotIndex > 0 && appendExtIfNeed) {
                // 20170303 处理 百度新闻中保存图片 u=***&***h=480&img.JPEG&access=215967317 样式的
                String endStr = filename.substring(dotIndex);
                if (endStr.contains("&")) {
                    filename = filename.replace(endStr.substring(endStr.indexOf('&')), "");
                }
            }
            extension = "";
        }

        return replaceInvalidVfatCharacters(filenameMaxLengthLim(filename,FILENAME_MAX_LEN) + extension);
    }
    
    private static String filenameMaxLengthLim(String filename,int maxlength){
        return filename.length() > maxlength ? filename.substring(0, maxlength-1):filename;
    }
    
    /**
     * Replace invalid filename characters according to
     * specifications of the VFAT.
     * @note Package-private due to testing.
     */
    private static String replaceInvalidVfatCharacters(String filename) {
        final char START_CTRLCODE = 0x00;
        final char END_CTRLCODE = 0x1f;
        final char QUOTEDBL = 0x22;
        final char ASTERISK = 0x2A;
        final char SLASH = 0x2F;
        final char COLON = 0x3A;
        final char LESS = 0x3C;
        final char GREATER = 0x3E;
        final char QUESTION = 0x3F;
        final char BACKSLASH = 0x5C;
        final char BAR = 0x7C;
        final char DEL = 0x7F;
        final char UNDERSCORE = 0x5F;

        StringBuffer sb = new StringBuffer();
        char ch;
        boolean isRepetition = false;
        for (int i = 0; i < filename.length(); i++) {
            ch = filename.charAt(i);
            if ((START_CTRLCODE <= ch &&
                ch <= END_CTRLCODE) ||
                ch == QUOTEDBL ||
                ch == ASTERISK ||
                ch == SLASH ||
                ch == COLON ||
                ch == LESS ||
                ch == GREATER ||
                ch == QUESTION ||
                ch == BACKSLASH ||
                ch == BAR ||
                ch == DEL){
                if (!isRepetition) {
                    sb.append(UNDERSCORE);
                    isRepetition = true;
                }
            } else {
                sb.append(ch);
                isRepetition = false;
            }
        }
        return sb.toString();
    }

    public static String parseContentDisposition(String contentDisposition) {
        String[] tokens = contentDisposition.split(";");
        String filename = null;

        for (String token : tokens) {
            String[] kv = token.split("=");
            if (kv.length != 2) {
                continue;
            }

            if (kv[0].trim().toLowerCase().startsWith("filename")) {
                filename = kv[1].trim().replaceFirst("UTF-8", "").replaceAll("\"", "");
            }
        }

        return filename;
    }

    // 处理 URL 中的特殊字符
    public static String fixURL3(String urlString) {
        if (urlString == null) {
            return null;
        }
        int loop = 100; 
        do {
            try {
                URL url = new URI(urlString).toURL();
                return url.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                int index = e.getIndex();
                String r = String.valueOf(urlString.charAt(index));
                try {
                    r = URLEncoder.encode(r, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    StringBuilder builder = new StringBuilder(urlString);
                    builder.replace(index, index + 1, r);
                    urlString = builder.toString();
                } catch (Throwable e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (IllegalArgumentException e) {
                break;
            }
        } while (--loop != 0);
        
        return null;
    }

    public static boolean isFilteredImageWebsite(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Matcher m = FILTER_IMAGE_WEBSITE_SCHEMA.matcher(url);
        if (m.find()) {
            return true;
        }

        return false;
    }
}
