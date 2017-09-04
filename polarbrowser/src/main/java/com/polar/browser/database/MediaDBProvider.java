package com.polar.browser.database;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.polar.browser.R;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;

/**
 * Created by yd_lzk on 2016/12/14.
 */

public class MediaDBProvider extends AsyncQueryHandler{

    private static final String TAG = "MediaDBRefreshHelper";
    private Context mContext;

    //token type
    public static final int TOKEN_QUERY = 0;
    public static final int TOKEN_UPDATE = 1;
    public static final int TOKEN_DELETE = 2;
    public static final int TOKEN_INSERT = 3;

    //file type
    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_AUDIO = 2;
    private static final int TYPE_FILE = 3;

    /**
     * 获取Video的字段
     */
    private final String[] PROJECTION_VIDEOS = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
    };

    /**
     * 获取Image目录的字段
     */
    private final String[] PROJECTION_IMAGE_BUCKETS = new String[] {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            "COUNT("+MediaStore.Images.Media._ID+") AS Size",
            MediaStore.Images.Media.DATE_ADDED
    };
    public static final int IMAGES_COUNT_IN_BUCKET = 4;  //Note：index要与PROJECTION_IMAGES保持一致
    /**
     * 获取Image的查询条件
     */
    private static final String SELECTION_IMAGES = " 0==0) group by bucket_id --(";
    /**
     * 获取Image的字段
     */
    private static final String[] PROJECTION_IMAGES = new String[] {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
    };

    /**
     * 获取APK的字段
     */
    private final String[] PROJECTION_APKS = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
    };
    /**
     * 获取APK的查询条件
     */
    private static final String SELECTION_APKS =  MediaStore.Files.FileColumns.DATA + " like ?";
    private final String[] SELECTION_ARGS_APKS = new String[]{"%.apk"};

    /**
     * 获取audio的字段
     */
    private final String[] PROJECTION_AUDIOS = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
    };

    /**
     * 获取压缩文件的字段
     */
    private static final String[] PROJECTION_COMPRESS = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
    };
    /**
     * 获取压缩文件的条件
     */
    private static final String WHERE_COMPRESS = QueryUtils.ZIP_WHERE;

    /**
     * 获取文档文件的字段
     */
    private static final String[] PROJECTION_DOCUMENTS = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
    };
    /**
     * 获取文档文件的条件
     */
    private static final String WHERE_DOCUMENTS = QueryUtils.DOC_WHERE;
    /**
     * 获取未知文件的字段
     */
    private static final String[] PROJECTION_UNKNOWN = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
    };
    /**
     * 获取未知文件的条件
     */
    private static final String WHERE_UNKNOWN = QueryUtils.OTHER_WHERE;



    /**
     * 获取网页文件的字段
     */
    private static final String[] PROJECTION_WEB_PAGE = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
    };

    /**
     * 获取网页文件的条件
     */
    private static final String WHERE_WEB_PAGE = QueryUtils.WEB_PAGE_WHERE;



    public MediaDBProvider(Context context) {
        super(context.getContentResolver());
        this.mContext = context;
    }

    /**
     * 将文件插入媒体数据库
     * @param filePath  新文件路径
     */
    public void insertFile(String filePath){
        if(filePath == null) return;
        ContentValues values = null;
        String fileName = FileUtils.getFileNameByPath(filePath);
        switch (getTypeByName(fileName)){
            case TYPE_IMAGE:
                SimpleLog.d(TAG,"insertFile image -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Images.Media.DATA, filePath);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                this.startInsert(TOKEN_INSERT, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case TYPE_AUDIO:
                SimpleLog.d(TAG,"insertFile audio -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Audio.Media.DATA, filePath);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                this.startInsert(TOKEN_INSERT, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case TYPE_VIDEO:
                SimpleLog.d(TAG,"insertFile video -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Video.Media.DATA, filePath);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                this.startInsert(TOKEN_INSERT, null, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case TYPE_FILE:
                SimpleLog.d(TAG,"insertFile file -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Files.FileColumns.DATA, filePath);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                this.startInsert(TOKEN_INSERT, null, MediaStore.Files.getContentUri("external"), values);
                break;
        }

    }

    /**
     *
     * @param destDir  需要刷新的目录
     */
    public void insertDir(String destDir){
        File destFile = new File(destDir);
        if(destFile.isDirectory()){
            File fileArr[] = destFile.listFiles();
            int size = fileArr.length;
            for(int i=0; i<size; i++){
                if(fileArr[i].isDirectory()){  //目录
                    insertDir(fileArr[i].getAbsolutePath());
                }else{ //文件
                    insertFile(fileArr[i].getAbsolutePath());
                }
            }
        }else{
            insertFile(destFile.getPath());
        }
    }

    /**
     * 通过MediaStore.Files可以删除任意类型的文件 TODO 待验证
     * @param filePath 文件路径
     */
    public void deleteFile(String filePath){
        filePath = filePath.replace("'", "''");
        this.startDelete(TOKEN_DELETE,
                null,
                MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns.DATA +" =?",
                new String[]{filePath});
    }

    /**
     *
     * @param directory
     */
    public void deleteDir(String directory){
        this.startDelete(TOKEN_DELETE,
                null,
                MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns.DATA + " LIKE ?" ,
                new String[]{directory + "/%"});
    }




    /**
     * 更新媒体数据库：修改保存路径
     * 要求：文件需要在targetPath中已创建
     * @param fileName
     * @param oldPath
     * @param targetPath
     */
    public void updateFilePath(String fileName, String oldPath, String targetPath){
        ContentValues values = null;
        String where = null;
        switch (getTypeByName(fileName)){
            case TYPE_IMAGE:
                SimpleLog.d(TAG,"update image -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Images.Media.DATA, targetPath);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                where = MediaStore.Images.Media.DATA +" =?";
                this.startUpdate(TOKEN_UPDATE, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, where, new String[]{oldPath});
                break;
            case TYPE_AUDIO:
                SimpleLog.d(TAG,"update audio -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Audio.Media.DATA, targetPath);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                where = MediaStore.Audio.Media.DATA +" =?";
                this.startUpdate(TOKEN_UPDATE, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, where, new String[]{oldPath});
                break;
            case TYPE_VIDEO:
                SimpleLog.d(TAG,"update video -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Video.Media.DATA, targetPath);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                where = MediaStore.Video.Media.DATA  +" =?";
                this.startUpdate(TOKEN_UPDATE, null, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values, where, new String[]{oldPath});
                break;
            case TYPE_FILE:
                SimpleLog.d(TAG,"update file -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Files.FileColumns.DATA, targetPath);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                where = MediaStore.Files.FileColumns.DATA  +" =?";
                this.startUpdate(TOKEN_UPDATE, null, MediaStore.Files.getContentUri("external"), values, where, new String[]{oldPath});
                break;
        }

    }

    /**
     * 更新文件名：two steps : one: 增加到对应数据表， two 将原有数据从原有表中删除
     * @param fileName
     * @param oldPath
     * @param targetPath
     */
    public void updateFileName(String fileName, String oldPath, String targetPath){
        insertFile(targetPath);
        deleteFile(oldPath);
        onUpdateComplete(TOKEN_UPDATE,null,0);  //TODO 结果是否同步？
    }

    /**
     * 获取Video列表
     */
    public void startQueryVideos(){
        super.startQuery(TOKEN_QUERY, null,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_VIDEOS,
                null, null,
                MediaStore.Video.Media.DATE_ADDED+ " DESC");
    }
    /**
     * 获取Image目录列表
     */
    public void startQueryImageBuckets(){
        super.startQuery(TOKEN_QUERY,null,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_IMAGE_BUCKETS,
                SELECTION_IMAGES,
                null,
                MediaStore.Images.Media.DATE_ADDED+ " DESC");
    }
    /**
     * 获取Image列表
     */
    public void startQueryImages(long id) {
        super.startQuery(TOKEN_QUERY,null,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_IMAGES,
                " "+MediaStore.Images.Media.BUCKET_ID + "=" + id,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC");
    }
    /**
     * 获取audio列表
     */
    public void startQueryAudios() {
        super.startQuery(TOKEN_QUERY, null,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_AUDIOS,
                null, null,
                MediaStore.Audio.Media.DATE_ADDED+ " DESC");
    }

    /**
     * 获取APK列表
     */
    public void startQueryAPKs() {
        super.startQuery(TOKEN_QUERY, null,
                MediaStore.Files.getContentUri("external"),
                PROJECTION_APKS,
                SELECTION_APKS,
                SELECTION_ARGS_APKS,
                MediaStore.Files.FileColumns.DATE_ADDED+" DESC");
    }

    /**
     * 获取压缩文件列表
     */
    public void startQueryCompressFiles() {
        super.startQuery(TOKEN_QUERY,
                null,
                MediaStore.Files.getContentUri("external"),
                PROJECTION_COMPRESS,
                WHERE_COMPRESS,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED+" DESC");
    }

    /**
     * 获取文档文件列表
     */
    public void startQueryDocuments() {
        super.startQuery(TOKEN_QUERY,
                null,
                MediaStore.Files.getContentUri("external"),
                PROJECTION_DOCUMENTS,
                WHERE_DOCUMENTS,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED+" DESC");
    }

    /**
     * 获取文档文件列表
     */
    public void startQueryWebPages() {
        super.startQuery(TOKEN_QUERY,
                null,
                MediaStore.Files.getContentUri("external"),
                PROJECTION_WEB_PAGE,
                WHERE_WEB_PAGE,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED+" DESC");
    }

    public void startQueryUnknownFiles() {
        super.startQuery(TOKEN_QUERY,
                null,
                MediaStore.Files.getContentUri("external"),
                PROJECTION_UNKNOWN,
                WHERE_UNKNOWN,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED+" DESC");
    }

    /**
     *
     * @param file 文件名称 或 文件路径
     * @return
     */
    private int getTypeByName(String file){
        int type = TYPE_FILE;
        if (OpenFileUtils.checkEndsWithInStringArray(file, mContext.getResources()
                .getStringArray(R.array.fileEndingImage))) {   //照片
            type = TYPE_IMAGE;
        } else if (OpenFileUtils.checkEndsWithInStringArray(file,
                mContext.getResources().getStringArray(R.array.fileEndingAudio))) {  //音频
            type = TYPE_AUDIO;
        } else if (OpenFileUtils.checkEndsWithInStringArray(file,
                mContext.getResources().getStringArray(R.array.fileEndingVideo))) {  //视频
            type = TYPE_VIDEO;
        } else{  //文件
            type = TYPE_FILE;
        }
        return type;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
    }

}
