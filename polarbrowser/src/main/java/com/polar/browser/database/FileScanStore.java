package com.polar.browser.database;


import android.net.Uri;

import static com.polar.browser.database.FileScanDatabaseHelper.TABLE_NAME_APK;


/**
 * Created by yd_lzk on 2016/11/9.
 */

public final class FileScanStore {

    public static final class Apk {

        public static final Uri CONTENT_URI = Uri.parse("content://" + FileScanContentProvider.AUTHORITIES + "/" + TABLE_NAME_APK);

        public interface ApkColumes {
//            String COLUME_ID = "_id";
            String COLUME_APK_ID = "_id_apk";
            String COLUME_ICON = "icon";
            String COLUME_NAME = "name";
            String COLUME_PATH = "path";
            String COLUME_DATE_ADD = "date_add";
            String COLUME_SIZE = "size";  // 文件大小
            String COLUME_INSTALLED = "installed";  // 文件大小
        }
    }

}
