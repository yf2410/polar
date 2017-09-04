
package com.polar.browser.download_refactor.util;

public class UserAgent {
    /**
     * 系统默认UA, Mozilla/5.0 (Linux; Android 4.1.1; MI 2 Build/JRO03L)
     * AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile
     * Safari/535.19
     * 
     * @author caisenchuan
     */
    public static final String DEFAULT_UA = String
            .format("Mozilla/5.0 (Linux; Android %s; %s Build/%s) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19",
                    android.os.Build.VERSION.RELEASE, android.os.Build.MODEL, android.os.Build.ID);
}
