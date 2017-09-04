package com.polar.browser.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;


/**
 * Created by yd_lzk on 2016/11/9.
 * 用于存储APK列表的扫描结果
 */

public class FileScanContentProvider extends ContentProvider {

    private static final String TAG = "FileScanContentProvider";

    //URI的指定，此处的字符串必须和声明的authorities一致
    public static final String AUTHORITIES = "com.go.downloader.file.db.filescancontentprovider";
    private FileScanDatabaseHelper dbHelper;
    private static final String[] APK_QUERY_COLUMES = new String[]{
            FileScanStore.Apk.ApkColumes.COLUME_APK_ID,
            FileScanStore.Apk.ApkColumes.COLUME_ICON,
            FileScanStore.Apk.ApkColumes.COLUME_NAME,
            FileScanStore.Apk.ApkColumes.COLUME_PATH,
            FileScanStore.Apk.ApkColumes.COLUME_DATE_ADD,
            FileScanStore.Apk.ApkColumes.COLUME_SIZE,
            FileScanStore.Apk.ApkColumes.COLUME_INSTALLED
    };

    private static final UriMatcher uriMatcher;
    private static final int APK_LIST_TABLE = 1;
    private static final int APK_LIST_TABLE_ITEM = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES,"apk_list",APK_LIST_TABLE);
        uriMatcher.addURI(AUTHORITIES,"apk_list/#",APK_LIST_TABLE_ITEM);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FileScanDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try{
            switch (uriMatcher.match(uri)) {
                case APK_LIST_TABLE:
                    Log.d(TAG, "query  --  uri match apk table");
                    cursor = db.query(true,FileScanDatabaseHelper.TABLE_NAME_APK,APK_QUERY_COLUMES,selection,selectionArgs,null,null,sortOrder,null);
                    break;
                case APK_LIST_TABLE_ITEM:
                    Log.d(TAG, "query  --  uri match apk table item");
                    long apkId = ContentUris.parseId(uri);
                    String where = FileScanStore.Apk.ApkColumes.COLUME_APK_ID+" = "+apkId;
                    where += !TextUtils.isEmpty(selection) ? " and ( "+ selection +" ) " : "";
                    cursor = db.query(true,FileScanDatabaseHelper.TABLE_NAME_APK,APK_QUERY_COLUMES,where,selectionArgs,null,null,sortOrder,null);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            if(cursor!=null){ cursor.close(); }
//            if(db!=null){ db.close(); }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //得到一个可写的数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            switch (uriMatcher.match(uri)){
                case APK_LIST_TABLE:
                case APK_LIST_TABLE_ITEM:
                    Log.d(TAG,"insertFile  --  uri match apk table");
                    //向指定的表插入数据，得到返回的Id
                    long rowId = db.insert(FileScanDatabaseHelper.TABLE_NAME_APK, null, values);
                    if(rowId > 0){ // 判断插入是否执行成功
                        //如果添加成功，利用新添加的Id和
                        Uri insertedUserUri = ContentUris.withAppendedId(FileScanStore.Apk.CONTENT_URI, rowId);
                        //通知监听器，数据已经改变
                        getContext().getContentResolver().notifyChange(insertedUserUri, null);
                        Log.d(TAG,"insertFile  --  uri match apk table success");
                        return insertedUserUri;
                    }else{
                        Log.d(TAG,"insertFile  --  uri match apk table false");
                        return null;
                    }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            if(db!=null){ db.close(); }
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        try{
            switch (uriMatcher.match(uri)) {
                case APK_LIST_TABLE:
                    Log.d(TAG, "delete  --  uri match apk table");
                    count = db.delete(FileScanDatabaseHelper.TABLE_NAME_APK,selection,selectionArgs);
                    break;
                case APK_LIST_TABLE_ITEM:
                    Log.d(TAG, "delete  --  uri match apk table item");
                    long apkId = ContentUris.parseId(uri);
                    String where = FileScanStore.Apk.ApkColumes.COLUME_APK_ID+" = "+apkId;
                    where += !TextUtils.isEmpty(selection) ? " and ( "+ selection +" ) " : "";
                    count = db.delete(FileScanDatabaseHelper.TABLE_NAME_APK,where,selectionArgs);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            if(db!=null){ db.close(); }
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        try {
            switch (uriMatcher.match(uri)) {
                case APK_LIST_TABLE:
                    Log.d(TAG, "update  --  uri match apk table");
                    count = db.update(FileScanDatabaseHelper.TABLE_NAME_APK,values,selection,selectionArgs);
                    break;
                case APK_LIST_TABLE_ITEM:
                    Log.d(TAG, "update  --  uri match apk table item");
                    long apkId = ContentUris.parseId(uri);
                    String where = FileScanStore.Apk.ApkColumes.COLUME_APK_ID+" = "+apkId;
                    where += !TextUtils.isEmpty(selection) ? " and ( "+ selection +" ) " : "";
                    count = db.update(FileScanDatabaseHelper.TABLE_NAME_APK,values,where,selectionArgs);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            if(db!=null){ db.close(); }
        }
        return count;
    }

}
