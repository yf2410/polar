package com.polar.browser.download_refactor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.DownloadInfo;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.Downloads;
import com.polar.browser.download_refactor.Downloads.Impl;
import com.polar.browser.download_refactor.SystemFacade;
import com.polar.browser.download_refactor.UiStatusDefine;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.download_refactor.util.ThreadManager;
import com.polar.browser.i.Callback;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class DownloadProvider {
    private DatabaseHelper mDbHelper;
    private SystemFacade mSystemFacade;
    ///>FIXME 最后调整宏定义
    private static final String DB_TABLE = "downloads";
    private static final String HEADERS_DB_TABLE = "request_headers";
    private static final String SQL_GETALL_FINISHED_DOWNLOAD  = String.format(
            "SELECT * FROM '%s' where %s = ?", DB_TABLE, Downloads.Impl.COLUMN_STATUS);
    private static final String SQL_CHECK_NEED_RECOVER  = String.format(
            "SELECT * FROM '%s' where %s = ? or %s = ?", DB_TABLE, Downloads.Impl.COLUMN_STATUS, Downloads.Impl.COLUMN_STATUS);
    private static DownloadProvider sDownloadProvider;
    public static DownloadProvider getInstance(){
        if (sDownloadProvider == null) {
            synchronized (DownloadProvider.class) {
                if (sDownloadProvider == null) {
                    sDownloadProvider = new DownloadProvider();
                }
            }
        }
        return sDownloadProvider;
    }
    
    public void init(Context context){
        mDbHelper = new DatabaseHelper(context);
        mSystemFacade = new SystemFacade(context);
    }
    
    public void unInit(){

    }

    public interface QueryDownloadListsCallback{
        public void onDownloadLists(ArrayList<DownloadItemInfo> lists);
    }

    /**
     * 是否需有因为wifi断开而暂停的下载任务，(有的话，需要再次启动应用是继续下载)
     */
    public void isNeedRecover(final Callback callback) {
        postTask(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = null;
                Cursor cr = null;
                try {
                    db = getReadableDatabase();
                    cr = db.rawQuery(SQL_CHECK_NEED_RECOVER,new String[]{String.valueOf(196), String.valueOf(197)});
                    if(cr != null){
                        callback.callback(cr.getCount() > 0);
                    }
                } catch (Exception e) {
                    SimpleLog.e(e);
                } finally{
                    if(cr!=null)
                        cr.close();
                    if(db!=null)
                        db.close();
                }
            }
        });
    }

    public void getDownloadLists( final QueryDownloadListsCallback callback ){
        getDownloadLists(callback,getHandler());
    }
    
    public void getDownloadLists( final QueryDownloadListsCallback callback,final Handler handler){
        postTask(new Runnable() {
            @Override
            public void run() {
                final ArrayList<DownloadItemInfo> lists = getDownloadLists();
                if( isLoopCurrentThread(handler) ){
                    if(callback!=null)
                        callback.onDownloadLists(lists);
                }else{
                    handler.post(new Runnable() {  
                        @Override
                        public void run() {
                            if(callback!=null)
                                callback.onDownloadLists(lists);
                        }
                    });
                }
            }
        });
    }

    public ArrayList<DownloadItemInfo> getDownloadLists(){
        _assertDBThread();
        ArrayList<DownloadItemInfo> lists = new ArrayList<DownloadItemInfo>();
        SQLiteDatabase db = null;
        Cursor cr = null;
        try {
            db = getReadableDatabase();
            cr = db.rawQuery("select * from " + DB_TABLE, null);
            if( cr != null ){
                while(cr.moveToNext()){ 
                    lists.add(_getDownloadItemInfo(cr));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(cr!=null)
                cr.close();
            if(db!=null)
                db.close();
        }
        
        return lists;
    }

    public ArrayList<DownloadItemInfo> getDownloadListSyn(){
//        _assertDBThread();
        ArrayList<DownloadItemInfo> lists = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cr = null;
        try {
            db = getReadableDatabase();
            cr = db.rawQuery(SQL_GETALL_FINISHED_DOWNLOAD,new String[]{String.valueOf(200)});
            if( cr != null ){
                while(cr.moveToNext()){
                    lists.add(_getDownloadItemInfo(cr));
                }
            }
        } catch (Exception e) {
            SimpleLog.e(e);
        } finally{
            if(cr!=null)
                cr.close();
            if(db!=null)
                db.close();
        }

        return lists;
    }

    /**
     * @desc 获取下载成功列表
     * @author Hacken
     * @time 2016/8/2 19:29
     */
    public ArrayList<DownloadItemInfo> getDownloadFinishListsSyn() {
        _assertDBThread();
        ArrayList<DownloadItemInfo> lists = new ArrayList<DownloadItemInfo>();
        SQLiteDatabase db = null;
        Cursor cr = null;
        try {
            db = getReadableDatabase();
            cr = db.rawQuery("select * from " + DB_TABLE, null);
            if( cr != null ){
                while(cr.moveToNext()){
                    lists.add(_getDownloadItemInfo(cr));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(cr!=null)
                cr.close();
            if(db!=null)
                db.close();
        }

        return lists;
    }
    
    public interface InsertDownloadCallback{
        public void onInsertDownload(long id);
    }
    
    public void insert(final ContentValues values,final InsertDownloadCallback callback){
        insert(values,callback,getHandler());
    }
    
    public void insert(final ContentValues values,final InsertDownloadCallback callback,final Handler handler){
        postTask(new Runnable() {
            @Override
            public void run() {
                final long id = insert(values);
                if(isLoopCurrentThread(handler)){
                    if(callback!=null)
                        callback.onInsertDownload(id);
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            if(callback!=null)
                                callback.onInsertDownload(id);
                        }
                    });
                }
            }
        });
    }
    
    public long insert(final ContentValues values ){
        _assertDBThread();
        SQLiteDatabase db = null;
        long rowID = -1;
        try {
            db = getWritableDatabase();
            // copy some of the input values as it
            ContentValues filteredValues = new ContentValues();
            copyString(Downloads.Impl.COLUMN_URI, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_APP_DATA, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_FILE_PATH, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_MIME_TYPE, values, filteredValues);

            // validate the visibility column
            Integer vis = values.getAsInteger(Downloads.Impl.COLUMN_VISIBILITY);
            if (vis == null) {
                // 默认可见
                filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY,
                        Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            } else {
                filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY, vis);
            }
            // copy the control column as is
            copyInteger(Downloads.Impl.COLUMN_CONTROL, values, filteredValues);

            copyIntegerWithDefault(Downloads.Impl.COLUMN_STATUS, values, filteredValues, Downloads.Impl.STATUS_PENDING);
            copyIntegerWithDefault(Downloads.Impl.COLUMN_VIRUSCHECK, values, filteredValues, Downloads.Impl.VIRUSCHECK_NOT_CHECK);
            copyLongWithDefault(Downloads.Impl.COLUMN_TOTAL_BYTES, values, filteredValues, (long)-1);
            copyLongWithDefault(Downloads.Impl.COLUMN_CURRENT_BYTES, values, filteredValues, (long)0);
            copyString(Downloads.Impl.COLUMN_DOWNLOAD_FILE_PATH, values, filteredValues);
            copyIntegerWithDefault(Downloads.Impl.COLUMN_CONTINUING_STATE, values, filteredValues, 0);

            // set lastupdate to current time
            long lastMod = mSystemFacade.currentTimeMillis();
            filteredValues.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, lastMod);

            // copy some more columns as is
            copyString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_COOKIE_DATA, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_USER_AGENT, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_REFERER, values, filteredValues);

            // is_visible_in_downloads_ui column
            if (values.containsKey(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI)) {
                copyBoolean(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, values, filteredValues);
            } else {
                // 默认UI上可见
                filteredValues.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
            }

            // public api requests and networktypes/roaming columns
            copyInteger(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, values, filteredValues);
            copyBoolean(Downloads.Impl.COLUMN_ALLOW_ROAMING, values, filteredValues);
            copyBoolean(Downloads.Impl.COLUMN_ALLOW_METERED, values, filteredValues);

            rowID = db.insert(DB_TABLE, null, filteredValues);     
            if(rowID != -1)
                insertRequestHeaders(db, rowID, values);
        } catch (Exception e) {

        }finally{
            if(db!=null)
                db.close();
        }
        return rowID;
    }

    public long insertDownloadItemInfo(final DownloadItemInfo downloadItemInfo ){
//        _assertDBThread();
        SQLiteDatabase db = null;
        long rowID = -1;
        int status;
        if (downloadItemInfo == null){
            return rowID;
        }
        try {
            db = getWritableDatabase();

            ContentValues values = new ContentValues();
            status = downloadItemInfo.mStatus;
            if (5 == status){
                status = 8;
            }
            values.put(Downloads.Impl.COLUMN_URI, downloadItemInfo.mUrl);
            values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, 0);
            values.put(Downloads.Impl.COLUMN_FILE_PATH, downloadItemInfo.mFilePath);
            values.put(Downloads.Impl.COLUMN_MIME_TYPE, downloadItemInfo.mMediaType);
            values.put(Downloads.Impl.COLUMN_VIRUSCHECK, -1);
            values.put(Downloads.Impl.COLUMN_VISIBILITY, 1);
            values.put(Downloads.Impl.COLUMN_CONTROL, (status == 8 ? Downloads.Impl.CONTROL_RUN : Downloads.Impl.CONTROL_PAUSED));// in v2, 6 means finished
            values.put(Downloads.Impl.COLUMN_STATUS, (status == 8 ? Downloads.Impl.STATUS_SUCCESS : Downloads.Impl.STATUS_PAUSED_BY_APP));
            values.put(Downloads.Impl.COLUMN_FAILED_CONNECTIONS, 0);
            values.put(Downloads.Impl.COLUMN_LAST_MODIFICATION,downloadItemInfo.mFinishDate);
            values.put(Downloads.Impl.COLUMN_COOKIE_DATA, downloadItemInfo.mCookie);
            values.put(Downloads.Impl.COLUMN_USER_AGENT, downloadItemInfo.mUserAgent);
            values.put(Downloads.Impl.COLUMN_REFERER, downloadItemInfo.mReferer);

            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, (status == 8 ? downloadItemInfo.mTotalBytes : downloadItemInfo.mTotalBytes));// in v2, 6 means finished
            values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, (status == 8 ? downloadItemInfo.mTotalBytes : downloadItemInfo.mCurrentBytes));// in v2, 6 means finished
            values.put(Downloads.Impl.COLUMN_CONTINUING_STATE, 0);
            values.put(Downloads.Impl.COLUMN_ALLOW_ROAMING, 1);
            values.put(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, -1);
            values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, 1);
            values.put(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, 0);
            values.put(Downloads.Impl.COLUMN_DELETED, 0);
            values.put(Downloads.Impl.COLUMN_ALLOW_METERED, 1);
            values.put(Downloads.Impl.COLUMN_ALLOW_WRITE, 0);

            rowID = db.insert(DB_TABLE, null, values);
            if(rowID != -1){
                if (downloadItemInfo.mUserAgent != null && downloadItemInfo.mUserAgent.length() > 0) {
                    ContentValues headerValues = new ContentValues();
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, rowID);
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, "User-Agent");
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, downloadItemInfo.mUserAgent);
                    db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, values);
                }

                if (downloadItemInfo.mCookie != null && downloadItemInfo.mCookie.length() > 0) {
                    ContentValues headerValues = new ContentValues();
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, rowID);
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, "cookie");
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, downloadItemInfo.mCookie);
                    db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, values);
                }

                if (downloadItemInfo.mReferer != null && downloadItemInfo.mReferer.length() > 0) {
                    ContentValues headerValues = new ContentValues();
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, rowID);
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, "Referer");
                    values.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, downloadItemInfo.mReferer);
                    db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, values);
                }
            }

        } catch (Exception e) {
            SimpleLog.d("SQL", "用户下载数据插入数据库异常！");
        }finally{
            if(db!=null)
                db.close();
        }
        return rowID;
    }
    
    public interface DeleteDownloadCallback{
        public void onDeleteDownload(long id, long ret);
    }
    
    public void delete(final long id,final DeleteDownloadCallback callback){
        delete(id,callback,getHandler());
    }
    
    public void delete(final long id,final DeleteDownloadCallback callback,final Handler handler){
        postTask(new Runnable() {
            @Override
            public void run() {
                final long ret = delete(id);
                if(isLoopCurrentThread(handler)){
                    if(callback!=null)
                        callback.onDeleteDownload(id,ret);
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            if(callback!=null)
                                callback.onDeleteDownload(id,ret);
                        }
                    });
                }
            }
        });
    }
    
    public long delete(final long id){
        _assertDBThread();
        long ret = -1;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String[] args = {String.valueOf(id)};
            ret = db.delete(HEADERS_DB_TABLE, Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID+"=?", args);
            if(ret!=-1){
                ret = db.delete(DB_TABLE, Downloads.Impl._ID +"=?", args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(db!=null)
                db.close();    
        }
        return ret;
    }

    public void deleteSyn(){
//        _assertDBThread();
        SQLiteDatabase db = null;
        Cursor cr = null;
        try {
            db = getWritableDatabase();
//            db.delete(HEADERS_DB_TABLE, null, null);
//            db.delete(DB_TABLE, null, null);
            cr = db.rawQuery("select _id from " + DB_TABLE + " where " + Downloads.Impl.COLUMN_STATUS + "="
                    + 200, null);
            if (cr != null) {
                while (cr.moveToNext()) {
                    long crLong = cr.getLong(cr.getColumnIndex(Impl._ID));
                    if (0L != crLong) {
                        SimpleLog.d("SQL", "用户下载成功任务ID="+crLong);
                        String[] args = {String.valueOf(crLong)};
                        db.delete(HEADERS_DB_TABLE, Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID+"=?", args);
                        db.delete(DB_TABLE, Downloads.Impl._ID +"=?", args);
                    }
                }
            }
        } catch (Exception e) {
            SimpleLog.e(e);
        }finally{
            if(cr!=null)
                cr.close();
            if(db!=null)
                db.close();
        }
    }
    
    ///> XXX 需要改进,语义为从数据库读取信息到DonloadInfo中
    public interface UpdateDownloadInfoCallback{
        public void onUpdateDownloadInfo(final boolean ret, final long id, final DownloadInfo info);
    }
    
    public void getDownloadInfo( final long id, final DownloadInfo info,final UpdateDownloadInfoCallback callback){
        getDownloadInfo(id,info,callback,getHandler());
    }
    
    public void getDownloadInfo(final long id,final DownloadInfo info,final UpdateDownloadInfoCallback callback,final Handler handler){
        postTask(new Runnable() {
            @Override
            public void run() {
                final boolean ret = getDownloadInfo(id,info);
                if(isLoopCurrentThread(handler)){
                    if(callback!=null)
                        callback.onUpdateDownloadInfo(ret,id,info);
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            if(callback!=null)
                                callback.onUpdateDownloadInfo(ret,id,info);
                        }
                    });
                }
            }
        });
    }
    
    
    public boolean getDownloadInfo( final long id, final DownloadInfo info){
        _assertDBThread();
        boolean ret = false;
        SQLiteDatabase db = null;
        Cursor cr = null;
        Cursor cr2 = null;
        try {
            db = getReadableDatabase();
            cr = db.rawQuery("select * from " + DB_TABLE + " where " + Downloads.Impl._ID + "="
                    + id, null);
            if (cr != null) {
                cr.moveToFirst();
                updateFromDatabase(cr, info);
                cr2 = db.rawQuery("select * from " + HEADERS_DB_TABLE + " where "
                        + Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + "=" + id, null);
                if (cr2 != null) {
                    cr2.moveToFirst();
                    readRequestHeaders(cr2, info);
                    ret = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cr != null)
                cr.close();
            if (cr2 != null)
                cr2.close();
            if (db != null)
                db.close();
        }
        return ret;
    }

    public interface UpdateDownloadStatusCallback{
        void onUpdateDownloadStatus(boolean ret, long id, int status);
    }
    
    public void updateDownloadStatus( final long id, final int status, final UpdateDownloadStatusCallback callback){
        updateDownloadStatus(id,status,callback,getHandler());
    }
    
    public void updateDownloadStatus(final long id, final int status, final UpdateDownloadStatusCallback callback,final Handler handler){
        postTask(new Runnable() {
            @Override
            public void run() {
                final boolean ret = updateDownloadStatus(id,status);
                if(isLoopCurrentThread(handler)){
                    if(callback!=null)
                        callback.onUpdateDownloadStatus(ret,id,status);
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            if(callback!=null)
                                callback.onUpdateDownloadStatus(ret,id,status);
                        }
                    });
                }
            }
        });
    } 
    
    
    public boolean updateDownloadStatus(final long id, final int status){
        boolean ret = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(Impl.COLUMN_STATUS, status);      
            String[] args = {String.valueOf(id)};
            long row = db.update(DB_TABLE, cv, Downloads.Impl._ID + "=?", args);    
            ret = row!=-1;
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(db!=null)
                db.close();
        }
        return ret;
    }
    
    public interface UpdateDownloadCallback{
        void onUpdateDownload(boolean ret, long id);
    }
      
    
    public boolean resertDownloadPath( long id ){
        _assertDBThread();
        boolean ret = false;
        SQLiteDatabase db = null;
        Cursor cr = null;
        try {
            db = getWritableDatabase(); 
            cr = db.rawQuery("select * from " + DB_TABLE + " where " + Downloads.Impl._ID + "="
                    + id, null);
            if (cr != null) {
                cr.moveToFirst();
                String destFile = getString(cr,Impl.COLUMN_FILE_PATH); 
                File flFile = new File(destFile);
                String name = flFile.getName();
                
                String destinationDir = PathResolver.getDownloadFileDir("");
                final File file = new File(destinationDir);
                String path = Uri.withAppendedPath(Uri.fromFile(file), name).getPath();
        
                ContentValues cv = new ContentValues();
                cv.put(Downloads.Impl.COLUMN_FILE_PATH, path);   
                cv.put(Impl.COLUMN_DOWNLOAD_FILE_PATH, "");
                String[] args = {String.valueOf(id)};
                long row = db.update(DB_TABLE, cv, Downloads.Impl._ID + "=?", args);    
                ret = row !=-1;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(db!=null)
                db.close();
        }
        return ret;
    }
    
    ///> XXX 侵入式更新需要了解具体数据库字段名,没时间改了后续再说 只在 DownloadTask中用
    public void updateDownload(final long id, final ContentValues cv, final UpdateDownloadCallback callback){
        updateDownload(id, cv,callback,getHandler());
    }
    
    public void updateDownload(final long id, final ContentValues cv, final UpdateDownloadCallback callback,final Handler handler){
        postTask(new Runnable() {
            @Override
            public void run() {
                final boolean ret = updateDownload(id,cv);
                if(isLoopCurrentThread(handler)){
                    if(callback!=null)
                        callback.onUpdateDownload(ret,id);
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            if(callback!=null)
                                callback.onUpdateDownload(ret,id);
                        }
                    });
                }
            }
        });
    }
    
    public boolean updateDownload(final long id, final ContentValues cv){
        _assertDBThread();
        boolean ret = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();   
            String[] args = {String.valueOf(id)};
            long row = db.update(DB_TABLE, cv, Downloads.Impl._ID + "=?", args);    
            ret = row!=-1;
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(db!=null)
                db.close();
        }
        return ret;
    }
    
    
    private void updateFromDatabase(Cursor cursor,DownloadInfo info) {
        info.mId = getLong(cursor,Downloads.Impl._ID);
        info.mUri = getString(cursor,Downloads.Impl.COLUMN_URI);
        info.mDestFile = getString(cursor,Impl.COLUMN_FILE_PATH);
        info.mDownlaodFile = getString(cursor,Impl.COLUMN_DOWNLOAD_FILE_PATH);
        info.mMimeType = getString(cursor,Downloads.Impl.COLUMN_MIME_TYPE);
        info.mVirusStatus = getInt(cursor,Downloads.Impl.COLUMN_VIRUSCHECK);
        info.mVisibility = getInt(cursor,Downloads.Impl.COLUMN_VISIBILITY);
        info.mStatus = getInt(cursor,Downloads.Impl.COLUMN_STATUS);
        info.mNumFailed = getInt(cursor,Downloads.Impl.COLUMN_FAILED_CONNECTIONS);
        int retryRedirect = getInt(cursor,Constants.RETRY_AFTER_X_REDIRECT_COUNT);
        info.mRetryAfter = retryRedirect & 0xfffffff;
        info.mLastMod = getLong(cursor,Downloads.Impl.COLUMN_LAST_MODIFICATION);
        info.mExtras = getString(cursor,Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS);
        info.mCookies = getString(cursor,Downloads.Impl.COLUMN_COOKIE_DATA);
        info.mUserAgent = getString(cursor,Downloads.Impl.COLUMN_USER_AGENT);
        info.mReferer = getString(cursor,Downloads.Impl.COLUMN_REFERER);
        info.mTotalBytes = getLong(cursor,Downloads.Impl.COLUMN_TOTAL_BYTES);
        info.mCurrentBytes = getLong(cursor,Downloads.Impl.COLUMN_CURRENT_BYTES);
        info.mETag = getString(cursor,Constants.ETAG);
        info.mMediaScanned = getInt(cursor,Constants.MEDIA_SCANNED);
        info.mDeleted = getInt(cursor,Downloads.Impl.COLUMN_DELETED);
        info.setAllowedNetworkTypes(getInt(cursor,Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES));
        info.mAllowRoaming = getInt(cursor,Downloads.Impl.COLUMN_ALLOW_ROAMING) != 0;
        info.mAllowMetered = getInt(cursor,Downloads.Impl.COLUMN_ALLOW_METERED) != 0;
        info.mBypassRecommendedSizeLimit =
                getInt(cursor,Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT);
        info.mControl = getInt(cursor,Downloads.Impl.COLUMN_CONTROL);
        info.mContinuingState = getInt(cursor,Downloads.Impl.COLUMN_CONTINUING_STATE);
    }

    
    private void readRequestHeaders(Cursor cursor,DownloadInfo info) {
        info.mRequestHeaders.clear();
        try {
            int headerIndex =
                    cursor.getColumnIndexOrThrow(Downloads.Impl.RequestHeaders.COLUMN_HEADER);
            int valueIndex =
                    cursor.getColumnIndexOrThrow(Downloads.Impl.RequestHeaders.COLUMN_VALUE);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                addHeader(info, cursor.getString(headerIndex), cursor.getString(valueIndex));
            }
        } finally {

        }

        if (info.mCookies != null) {
            addHeader(info, "Cookie", info.mCookies);
        }
        if (info.mReferer != null) {
            addHeader(info, "Referer", info.mReferer);
        }
    }
    
    
    private void addHeader(DownloadInfo info, String header, String value) {
        info.mRequestHeaders.add(Pair.create(header, value));
    }

    private String getString(Cursor cursor,String column) {
        int index = cursor.getColumnIndexOrThrow(column);
        String s = cursor.getString(index);
        return (TextUtils.isEmpty(s)) ? null : s;
    }

    private Integer getInt(Cursor cursor,String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    private Long getLong(Cursor cursor,String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }
    
    private void insertRequestHeaders(SQLiteDatabase db, long downloadId, ContentValues values) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, downloadId);
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            String key = entry.getKey();
            if (key.startsWith(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX)) {
                String headerLine = entry.getValue().toString();
                if (!headerLine.contains(":")) {
                    throw new IllegalArgumentException("Invalid HTTP header line: " + headerLine);
                }
                String[] parts = headerLine.split(":", 2);
                rowValues.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, parts[0].trim());
                rowValues.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, parts[1].trim());
                db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, rowValues);
            }
        }
    }
    
    private DownloadItemInfo _getDownloadItemInfo( Cursor cursor ){
        DownloadItemInfo info = new DownloadItemInfo();

        int colId = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);
        int colUrl = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_URI);
        int colReferer = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_REFERER);
        int colMediaType = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_MIME_TYPE);
        int colDate = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_LAST_MODIFICATION);
        int colVirus = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_VIRUSCHECK);
        int colStatus = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_STATUS);
        int colFilePath = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_FILE_PATH);
        int colCurrentBytes = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_CURRENT_BYTES);
        int colTotalBytes = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_TOTAL_BYTES);
        int colToCookie = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_COOKIE_DATA);
        int colToUserAgent = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_USER_AGENT);
        int colToFinishDate = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_LAST_MODIFICATION);

        info.mId = cursor.getLong(colId);
        info.mUrl = cursor.getString(colUrl);
        info.mReferer = cursor.getString(colReferer);
        info.mMediaType = cursor.getString(colMediaType);
        info.mDate = new Date(cursor.getLong(colDate));
        info.mVirusStatus = cursor.getInt(colVirus);
        int status = cursor.getInt(colStatus);
        info.mStatus = UiStatusDefine.translateStatus(status);
        info.mReason = UiStatusDefine.getReason(status);
        info.mFilePath = cursor.getString(colFilePath);
        info.mCurrentBytes = cursor.getLong(colCurrentBytes);
        info.mTotalBytes = cursor.getLong(colTotalBytes);
        info.mCookie = cursor.getString(colToCookie);
        info.mUserAgent = cursor.getString(colToUserAgent);
        info.mFinishDate = cursor.getLong(colToFinishDate);
        
        return info;
    }
    
    private SQLiteDatabase getReadableDatabase(){
        return mDbHelper.getReadableDatabase();
    }
    
    private SQLiteDatabase getWritableDatabase(){
        return mDbHelper.getWritableDatabase();
    }
    
    public void postTask( Runnable r ){
        ThreadManager.post(ThreadManager.THREAD_DB, r);
    } 
    
    private boolean isLoopCurrentThread(Handler handler){
        return handler.getLooper().getThread().getId() == Thread.currentThread().getId();
    }
    
    private Handler getHandler(){
        return ThreadManager.getHandler(ThreadManager.THREAD_DB);
    }
    
    private void _assertDBThread(){
        _assert(ThreadManager.getHandler(ThreadManager.THREAD_DB).getLooper().getThread().getId() == Thread.currentThread().getId());
    }
    
    private void _assert(boolean check){
        if(!check)
            throw new RuntimeException();
    }
    
    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }

    private static final void copyIntegerWithDefault(String key, ContentValues from,
                                                  ContentValues to, Integer defaultValue) {
        copyInteger(key, from, to);
        if (!to.containsKey(key)) {
            to.put(key, defaultValue);
        }
    }

    private static final void copyLong(String key, ContentValues from, ContentValues to) {
        Long l = from.getAsLong(key);
        if (l != null) {
            to.put(key, l);
        }
    }

    private static final void copyLongWithDefault(String key, ContentValues from,
                                                    ContentValues to, Long defaultValue) {
        copyLong(key, from, to);
        if (!to.containsKey(key)) {
            to.put(key, defaultValue);
        }
    }

    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

    private static final void copyStringWithDefault(String key, ContentValues from,
            ContentValues to, String defaultValue) {
        copyString(key, from, to);
        if (!to.containsKey(key)) {
            to.put(key, defaultValue);
        }
    }
}
