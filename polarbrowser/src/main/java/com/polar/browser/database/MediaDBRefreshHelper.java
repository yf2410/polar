package com.polar.browser.database;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.polar.browser.R;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.OpenFileUtils;

import java.io.File;

/**
 * Created by yd_lzk on 2016/11/29.
 */

public class MediaDBRefreshHelper {

    private static final String TAG = "MediaDBRefreshHelper";
    private Context mContext;
    private static MediaDBRefreshHelper instance;
    private QueryHandler queryHandler;

    //token type
    private static final int TOKEN_QUERY = 0;
    private static final int TOKEN_UPDATE = 1;
    private static final int TOKEN_DELETE = 2;
    private static final int TOKEN_INSERT = 3;

    //file type
    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_AUDIO = 2;
    private static final int TYPE_FILE = 3;


    private MediaDBRefreshHelper(Context context) {
        this.mContext = context;
        this.queryHandler = new QueryHandler(mContext);
    }

    //Singleton
    public static MediaDBRefreshHelper getInstance(Context context){
        if(context == null) return null;
        if(instance == null){
            synchronized (MediaDBRefreshHelper.class){
                if(instance == null){
                    instance = new MediaDBRefreshHelper(context);
                }
            }
        }
        return instance;
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
                Log.d(TAG,"insertFile image -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Images.Media.DATA, filePath);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                queryHandler.startInsert(TOKEN_INSERT, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case TYPE_AUDIO:
                Log.d(TAG,"insertFile audio -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Audio.Media.DATA, filePath);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                queryHandler.startInsert(TOKEN_INSERT, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case TYPE_VIDEO:
                Log.d(TAG,"insertFile video -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Video.Media.DATA, filePath);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                queryHandler.startInsert(TOKEN_INSERT, null, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                break;
            case TYPE_FILE:
                Log.d(TAG,"insertFile file -- fileName = "+fileName+" filePath = "+filePath);
                values = new ContentValues(3);
                values.put(MediaStore.Files.FileColumns.DATA, filePath);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                queryHandler.startInsert(TOKEN_INSERT, null, MediaStore.Files.getContentUri("external"), values);
                break;
        }

    }

    /**
     * 通过MediaStore.Files可以删除任意类型的文件 TODO 待验证
     * @param filePath 文件路径
     */
    public void deleteFile(String filePath){
        filePath = filePath.replace("'", "''");
        queryHandler.startDelete(TOKEN_DELETE,
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
        queryHandler.startDelete(TOKEN_DELETE,
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
                Log.d(TAG,"update image -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Images.Media.DATA, targetPath);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                where = MediaStore.Images.Media.DATA +" =?";
                queryHandler.startUpdate(TOKEN_UPDATE, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, where, new String[]{oldPath});
                break;
            case TYPE_AUDIO:
                Log.d(TAG,"update audio -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Audio.Media.DATA, targetPath);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                where = MediaStore.Audio.Media.DATA +" =?";
                queryHandler.startUpdate(TOKEN_UPDATE, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, where, new String[]{oldPath});
                break;
            case TYPE_VIDEO:
                Log.d(TAG,"update video -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Video.Media.DATA, targetPath);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                where = MediaStore.Video.Media.DATA  +" =?";
                queryHandler.startUpdate(TOKEN_UPDATE, null, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values, where, new String[]{oldPath});
                break;
            case TYPE_FILE:
                Log.d(TAG,"update file -- fileName = "+fileName+" oldPath = "+oldPath+" targetPath = "+targetPath);
                values = new ContentValues(3);
                values.put(MediaStore.Files.FileColumns.DATA, targetPath);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                where = MediaStore.Files.FileColumns.DATA  +" =?";
                queryHandler.startUpdate(TOKEN_UPDATE, null, MediaStore.Files.getContentUri("external"), values, where, new String[]{oldPath});
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

    private static class QueryHandler extends AsyncQueryHandler {

        private Context context;
        public QueryHandler(Context context) {
            super(context.getContentResolver());
            this.context = context;
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

}
