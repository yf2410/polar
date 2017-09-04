package com.polar.browser.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by yd_lzk on 2016/11/9.
 */

public class FileScanDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "files_scan_db";
    private static final int DB_VERSION = 2;
    public static final String TABLE_NAME_APK = "apk_list";

    public FileScanDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    public void createTables(SQLiteDatabase db){
        createApkTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME_APK);
            createTables(db);
    }

    public void createApkTable(SQLiteDatabase db){
        try {
//            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_APK + "(" +
//                            FileScanStore.Apk.ApkColumes.COLUME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            FileScanStore.Apk.ApkColumes.COLUME_APK_ID + " INTEGER PRIMARY KEY, " +
                            FileScanStore.Apk.ApkColumes.COLUME_ICON + " TEXT, " +
                            FileScanStore.Apk.ApkColumes.COLUME_NAME + " TEXT, " +
                            FileScanStore.Apk.ApkColumes.COLUME_PATH + " TEXT, " +
                            FileScanStore.Apk.ApkColumes.COLUME_DATE_ADD + " TEXT, " +
                            FileScanStore.Apk.ApkColumes.COLUME_INSTALLED + " INTEGER, " +
                            FileScanStore.Apk.ApkColumes.COLUME_SIZE + " TEXT )"
                    );
        } catch (SQLException ex) {
            throw ex;
        }
    }
}
