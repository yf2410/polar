package com.polar.browser.download_refactor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.Downloads;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
//    private static final String DB_NAME = "downloads.db";
    private static final String DB_NAME = "download_db";
    private static final int DB_VERSION = 2;
    private static final String DB_TABLE = "downloads";
    private static final String DB_TABLE_OLD = "table_download_info";
//    private static final String DB_TEMPLE_TABLE = "downloads_v2_temple";
    public DatabaseHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates database the first time we try to open it.
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "populating new database");
        }
        createTables(db);
    }
    
    void createTables(final SQLiteDatabase db) {
        createDownloadsTable(db);
        createHeadersTable(db);
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOW_ROAMING,
                  "INTEGER NOT NULL DEFAULT 0");
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES,
                  "INTEGER NOT NULL DEFAULT 0");
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI,
                "INTEGER NOT NULL DEFAULT 1");
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT,
                "INTEGER NOT NULL DEFAULT 0");
        fillNullValues(db);
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_DELETED,
                "INTEGER NOT NULL DEFAULT 0");
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ERROR_MSG, "TEXT");
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOW_METERED,
                "INTEGER NOT NULL DEFAULT 1");
        addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOW_WRITE,
                "BOOLEAN NOT NULL DEFAULT 0");

        SimpleLog.d("", "debug_数据库创建成功");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Updates the database format when a content provider is used
     * with a database that was created with a different format.
     *
     * Note: to support downgrades, creating a table should always drop it first if it already
     * exists.
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
        SimpleLog.d("SQL", "数据库升级");
        switch (oldV) {
            case 1:
                SimpleLog.d("SQL", "数据库开始升级");
                String strTableCheckSql = String.format("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='%s' ",
                        DB_TABLE_OLD);

                boolean isTempleTableExist = false;
                Cursor tableCursor = null;
                try {
                    tableCursor = db.rawQuery(strTableCheckSql, null);
                    if(tableCursor.moveToNext()){
                        int count = tableCursor.getInt(0);
                        if(count > 0){
                            isTempleTableExist = true;
                        }
                    }
                } catch (Exception e) {
                    String exceptionStr = e.toString();
                    Log.v("SQL", exceptionStr);
                } finally {
                    if (tableCursor != null) {
                        tableCursor.close();
                    }
                }

                if (!isTempleTableExist) {
                    createTables(db);
                    SimpleLog.d("SQL", "如果DB_TABLE_OLD表不存在，执行正常创建");
                } else {
                    // 如果DB_TABLE_OLD表已存在，升级数据库逻辑
                    Cursor cursor = null;
                    try {
                        createTables(db);
                        cursor = db.query(DB_TABLE_OLD, new String[] {
                                        "pageUrl",
                                        "fileName",
                                        "createDate",
                                        "destination",
                                        "status",
                                        "currentBytes",
                                        "url",
                                        "contentDisposition",
                                        "cookies",
                                        "userAgent",
                                        "contentLength",
                                        "mimetype",
                                        "finishDate"
                                },
                                null, null, null, null, null);
                        final List<DownloadItemInfo> infoList = new ArrayList<>();
                        if (cursor.getCount() > 0) {

                            while (cursor.moveToNext()) {

                                String referer = cursor.getString(cursor.getColumnIndex("pageUrl"));
                                String fileName = cursor.getString(cursor.getColumnIndex("fileName"));
                                long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
                                String destination = cursor.getString(cursor.getColumnIndex("destination"));
                                int state = cursor.getInt(cursor.getColumnIndex("status"));
                                if (5 == state){
                                    state = 8;
                                }
                                long currentBytes = cursor.getLong(cursor.getColumnIndex("currentBytes"));
                                String url = cursor.getString(cursor.getColumnIndex("url"));
                                String contentDisposition = cursor.getString(cursor.getColumnIndex("contentDisposition"));
                                String cookie = cursor.getString(cursor.getColumnIndex("cookies"));
                                String useragent = cursor.getString(cursor.getColumnIndex("userAgent"));
                                long totalbytes = cursor.getLong(cursor.getColumnIndex("contentLength"));
                                String mimetype = cursor.getString(cursor.getColumnIndex("mimetype"));
                                long finishDate = cursor.getLong(cursor.getColumnIndex("finishDate"));

                                DownloadItemInfo info = new DownloadItemInfo();
                                info.mUrl = url;
                                info.mReferer = referer;
                                info.mMediaType = mimetype;
                                info.mStatus = state;
                                info.mFilePath = destination;
                                info.mCurrentBytes = currentBytes;
                                info.mTotalBytes = totalbytes;
                                info.mCookie = cookie;
                                info.mUserAgent = useragent;
                                info.mFinishDate = finishDate;
                                infoList.add(info);

                                ContentValues values = new ContentValues();
                                values.put(Downloads.Impl.COLUMN_URI, url);
                                values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, 0);
                                values.put(Downloads.Impl.COLUMN_FILE_PATH, destination);
                                values.put(Downloads.Impl.COLUMN_MIME_TYPE, mimetype);
                                values.put(Downloads.Impl.COLUMN_VIRUSCHECK, -1);
                                values.put(Downloads.Impl.COLUMN_VISIBILITY, 1);
                                values.put(Downloads.Impl.COLUMN_CONTROL, (state == 8 ? Downloads.Impl.CONTROL_RUN : Downloads.Impl.CONTROL_PAUSED));// in v2, 6 means finished
                                values.put(Downloads.Impl.COLUMN_STATUS, (state == 8 ? Downloads.Impl.STATUS_SUCCESS : Downloads.Impl.STATUS_PAUSED_BY_APP));
                                values.put(Downloads.Impl.COLUMN_FAILED_CONNECTIONS, 0);
                                values.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, finishDate);
                                values.put(Downloads.Impl.COLUMN_COOKIE_DATA, cookie);
                                values.put(Downloads.Impl.COLUMN_USER_AGENT, useragent);
                                values.put(Downloads.Impl.COLUMN_REFERER, referer);
                                values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, (state == 8 ? totalbytes : totalbytes));// in v2, 6 means finished
                                values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, (state == 8 ? currentBytes : currentBytes));// in v2, 6 means finished
                                values.put(Downloads.Impl.COLUMN_CONTINUING_STATE, 0);
                                values.put(Downloads.Impl.COLUMN_ALLOW_ROAMING, 1);
                                values.put(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, -1);
                                values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, 1);
                                values.put(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, 0);
                                values.put(Downloads.Impl.COLUMN_DELETED, 0);
                                values.put(Downloads.Impl.COLUMN_ALLOW_METERED, 1);
                                values.put(Downloads.Impl.COLUMN_ALLOW_WRITE, 0);
                                long id = db.insert(DB_TABLE, null, values);

                                if (useragent != null && useragent.length() > 0) {
                                    ContentValues headerValues = new ContentValues();
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, id);
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, "User-Agent");
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, useragent);
                                    db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, values);
                                }

                                if (cookie != null && cookie.length() > 0) {
                                    ContentValues headerValues = new ContentValues();
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, id);
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, "cookie");
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, cookie);
                                    db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, values);
                                }

                                if (referer != null && referer.length() > 0) {
                                    ContentValues headerValues = new ContentValues();
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, id);
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, "Referer");
                                    values.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, referer);
                                    db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, values);
                                }
                            }
                        }
                        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_OLD);

                        ThreadManager.postTaskToIOHandler(new Runnable() {
                            @Override
                            public void run() {
                                if (infoList != null && infoList.size()>0){

                                    for (int i = 0; i < infoList.size(); i++){
                                        DownloadItemInfo info = infoList.get(i);

                                        if (info == null || TextUtils.isEmpty(info.mFilePath)){
                                            return;
                                        }
                                        try {
                                            String name = new File(info.mFilePath).getName();
                                            String downloadDataDirPath = VCStoragerManager.getInstance().getDownloadDataDirPath();
                                            if (name != null && downloadDataDirPath != null) {
                                                File file = new File(downloadDataDirPath);
                                                if (file.exists()) {
                                                    try {
                                                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(downloadDataDirPath + name + ".obj"));
                                                        out.writeObject(info);
                                                        out.close();
                                                    } catch (Exception e) {
                                                        // TODO Auto-generated catch block
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            SimpleLog.e(e);
                                        }
                                    }
                                }
                            }
                        });

                    } catch (Exception ex) {
                        SimpleLog.d("SQL", "升级数据库异常");
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                SimpleLog.d("SQL", "数据库升级完成！！！！");
                break;
            default:
                break;
        }
    }

    /**
     * Upgrade database from (version - 1) to version.
     */
    private void upgradeTo(SQLiteDatabase db, int version) {
        switch (version) {
            default:
                throw new IllegalStateException("Don't know how to upgrade to " + version);
        }
    }

    /**
     * insert() now ensures these four columns are never null for new downloads, so this method
     * makes that true for existing columns, so that code can rely on this assumption.
     */
    private void fillNullValues(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
        fillNullValuesForColumn(db, values);
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, -1);
        fillNullValuesForColumn(db, values);
        fillNullValuesForColumn(db, values);
        fillNullValuesForColumn(db, values);
    }

    private void fillNullValuesForColumn(SQLiteDatabase db, ContentValues values) {
        Set<Entry<String,Object>> valueSet = values.valueSet();
        Iterator<Entry<String,Object>> iterator = valueSet.iterator();
        if(iterator.hasNext()){
            Entry<String, Object> next = iterator.next();
            String column = next.getKey();
            db.update(DB_TABLE, values, column + " is null", null);
            values.clear();
        }
    }

    /**
     * Add a column to a table using ALTER TABLE.
     * @param dbTable name of the table
     * @param columnName name of the column to add
     * @param columnDefinition SQL for the column definition
     */
    private void addColumn(SQLiteDatabase db, String dbTable, String columnName,
                           String columnDefinition) {
        db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + " "
                   + columnDefinition);
    }

    /**
     * Creates the table that'll hold the download information.
     */
    private void createDownloadsTable(SQLiteDatabase db) {
        try {
//            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE + "(" +
                    Downloads.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Downloads.Impl.COLUMN_URI + " TEXT, " +
                    Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                    Downloads.Impl.COLUMN_APP_DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_FILE_PATH + " TEXT, " +
                    Constants.OTA_UPDATE + " BOOLEAN, " +
                    Downloads.Impl.COLUMN_DOWNLOAD_FILE_PATH + " TEXT, " +
                    Downloads.Impl.COLUMN_MIME_TYPE + " TEXT, " +
                    Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                    Downloads.Impl.COLUMN_VIRUSCHECK + " INTEGER, " +
                    Downloads.Impl.COLUMN_VISIBILITY + " INTEGER, " +
                    Downloads.Impl.COLUMN_CONTROL + " INTEGER, " +
                    Downloads.Impl.COLUMN_STATUS + " INTEGER, " +
                    Downloads.Impl.COLUMN_FAILED_CONNECTIONS + " INTEGER, " +
                    Downloads.Impl.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                    Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                    Downloads.Impl.COLUMN_COOKIE_DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_USER_AGENT + " TEXT, " +
                    Downloads.Impl.COLUMN_REFERER + " TEXT, " +
                    Downloads.Impl.COLUMN_TOTAL_BYTES + " INTEGER, " +
                    Downloads.Impl.COLUMN_CURRENT_BYTES + " INTEGER, " +
                    Downloads.Impl.COLUMN_CONTINUING_STATE + " INTEGER, " +
                    Constants.ETAG + " TEXT, " +
                    Constants.MEDIA_SCANNED + " BOOLEAN);");
        } catch (SQLException ex) {
            Log.e(Constants.TAG, "couldn't create table in downloads database");
            throw ex;
        }
    }

    private void createHeadersTable(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE + "(" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                   Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," +
                   Downloads.Impl.RequestHeaders.COLUMN_HEADER + " TEXT NOT NULL," +
                   Downloads.Impl.RequestHeaders.COLUMN_VALUE + " TEXT NOT NULL" +
                   ");");
    }
}
