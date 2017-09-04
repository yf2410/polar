package com.polar.browser.utils;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.polar.browser.bean.ApkInfo;
import com.polar.browser.bean.DocInfo;
import com.polar.browser.bean.ImageFolderInfo;
import com.polar.browser.bean.ImageItemInfo;
import com.polar.browser.bean.MusicInfo;
import com.polar.browser.bean.UnknownInfo;
import com.polar.browser.bean.VideoInfo;
import com.polar.browser.bean.ZipInfo;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.manager.ThreadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yd_lzk on 2016/12/14.
 */

public class CursorDataParserUtils {

    /**
     * 从Cursor解析List<VideoInfo>
     * @param cursor
     * @param queryHandler
     * @return
     */
    public static List<VideoInfo> parseVideosFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<VideoInfo> list = new ArrayList<VideoInfo>();
        try {
            while (cursor.moveToNext()) {
                try {
                    VideoInfo info = new VideoInfo();
                    info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                    info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)));
                    info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                    if (info.getPath() == null) continue;
                    if (!new File(info.getPath()).exists()) {  //数据库有数据，但文件已不存在，将数据从数据库中删除
                        final String id = info.getId() + "";
                        ThreadManager.postTaskToIOHandler(new Runnable() {
                            @Override
                            public void run() {
                                queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                        null,
                                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Video.Media._ID + " = ? ",
                                        new String[]{id});
                            }
                        });
                        continue;
                    }
                    if (info.getName() == null || info.getName().isEmpty()) {
                        info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                    }
                    list.add(info);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    /**
     * 从Cursor解析List<ImageFolderInfo>
     * @param cursor
     * @param queryHandler
     * @return
     */
    public static List<ImageFolderInfo> parseImageBucketsFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<ImageFolderInfo> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                try{
                    ImageFolderInfo info = new ImageFolderInfo();
                    info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
                    info.setImageID(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                    info.setImagePath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                    if(info.getImagePath() == null) continue;
                    File parentFile = new File(info.getImagePath()).getParentFile();
                    if (parentFile == null || !parentFile.exists() ||
                            parentFile.listFiles() == null || parentFile.listFiles().length < 1) {  //数据库有数据，但图片目录已不存在，或目录存在但图片不存在。
                        final String id = info.getId() + "";
                        ThreadManager.postTaskToIOHandler(new Runnable() {
                            @Override
                            public void run() {
                                queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                        null,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Images.Media.BUCKET_ID + " = ? ",
                                        new String[]{id});
                            }
                        });
                        continue;
                    }
                    info.setFolderPath(parentFile.getPath());
                    info.setImageCount(cursor.getInt(MediaDBProvider.IMAGES_COUNT_IN_BUCKET));
                    info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)));
                    list.add(info);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    /**
     * 从Cursor解析List<ImageItemInfo>
     * @param cursor
     * @param queryHandler
     * @return
     */
    public static List<ImageItemInfo> parseImagesFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<ImageItemInfo> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                try {
                    ImageItemInfo info = new ImageItemInfo();
                    info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                    info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)));
                    info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)));
                    if (!new File(info.getPath()).exists()) {  //数据库有数据，但文件已不存在，将数据从数据库中删除
                        final String id = info.getId() + "";
                        ThreadManager.postTaskToIOHandler(new Runnable() {
                            @Override
                            public void run() {
                                queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                        null,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Images.Media._ID + " = ? ",
                                        new String[]{id});
                            }
                        });
                        continue;
                    }
                    if(info.getName() == null || info.getName().isEmpty()){
                        info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                    }
                    info.setBucketId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)));
                    info.setBucketName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
                    list.add(info);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    /**
     * 从Cursor解析List<ApkInfo>
     * @param cursor
     * @param context
     * @return
     */
    public static List<ApkInfo> parseAPKsFromCursor(Cursor cursor, Context context) {
        ArrayList<ApkInfo> list = new ArrayList<ApkInfo>();
        try {
            while (cursor.moveToNext()) {
                try{
                    ApkInfo info = new ApkInfo();
                    info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                    info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
                    info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                    if(info.getPath() == null) continue;
                    if(!new File(info.getPath()).exists()){  //数据库有数据，但文件已不存在，将数据从数据库中删除
                        context.getContentResolver().delete(
                                MediaStore.Files.getContentUri("external"),
                                MediaStore.Files.FileColumns._ID + " = ? ",
                                new String[]{ info.getId()+""});
                        continue;
                    }
                    ApkInfo parseInfo = ApkUtils.getAppNameAndIsInstall(context,info.getPath());
                    info.setName(parseInfo.getName());  //app的文件名和应用名不同
                    if (info.getName() == null || info.getName().isEmpty()) {
                        info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                    }
                    info.setInstalled(parseInfo.isInstalled());
                    list.add(info);
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    /**
     * 从Cursor解析List<MusicInfo>
     * @param cursor
     * @param queryHandler
     * @return
     */
    public static List<MusicInfo> parseAudiosFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
        try {
            while (cursor.moveToNext()) {
                try{
                    MusicInfo info = new MusicInfo();
                    info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                    info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                    info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    if(info.getPath() == null) continue;
                    if(!new File(info.getPath()).exists()){  //数据库有数据，但文件已不存在，将数据从数据库中删除
                        final String id = info.getId()+"";
                        ThreadManager.postTaskToIOHandler(new Runnable() {
                            @Override
                            public void run() {
                                queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                        null,
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Audio.Media._ID + " = ? ",
                                        new String[]{id});
                            }
                        });
                        continue;
                    }
                    if (info.getName() == null || info.getName().isEmpty()) {
                        info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                    }
                    list.add(info);
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    /**
     * 从Cursor解析List<DocInfo>
     * @param cursor
     * @param queryHandler
     * @return
     */
    public static List<DocInfo> parseDocumentsFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<DocInfo> list = new ArrayList<DocInfo>();
        try {
            while (cursor.moveToNext()) {
                DocInfo info = new DocInfo();
                info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
                info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                if(!new File(info.getPath()).exists()){  //数据库有数据，但文件已不存在，将数据从数据库中删除
                    final String id = info.getId()+"";
                    ThreadManager.postTaskToIOHandler(new Runnable() {
                        @Override
                        public void run() {
                            queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                    null,
                                    MediaStore.Files.getContentUri("external"),
                                    MediaStore.Files.FileColumns._ID + " = ? ",
                                    new String[]{ id });
                        }
                    });
                    continue;
                }
                if (info.getName() == null || info.getName().isEmpty()) {
                    info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                }
                list.add(info);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    /**
     * 从Cursor解析List<ZipInfo>
     * @param cursor
     * @param queryHandler
     * @return
     */
    public static List<ZipInfo> parseCompressFilesFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<ZipInfo> list = new ArrayList<ZipInfo>();
        try {
            while (cursor.moveToNext()) {
                ZipInfo info = new ZipInfo();
                info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
                info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                if(!new File(info.getPath()).exists()){  //数据库有数据，但文件已不存在，将数据从数据库中删除
                    final String id = info.getId()+"";
                    ThreadManager.postTaskToIOHandler(new Runnable() {
                        @Override
                        public void run() {
                            queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                    null,
                                    MediaStore.Files.getContentUri("external"),
                                    MediaStore.Files.FileColumns._ID + " = ? ",
                                    new String[]{ id });
                        }
                    });
                    continue;
                }
                if (info.getName() == null || info.getName().isEmpty()) {
                    info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                }
                list.add(info);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }

    public static List<UnknownInfo> parseUnknownFilesFromCursor(Cursor cursor, final AsyncQueryHandler queryHandler) {
        ArrayList<UnknownInfo> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                UnknownInfo info = new UnknownInfo();
                info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
                info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                if(!new File(info.getPath()).exists()){  //数据库有数据，但文件已不存在，将数据从数据库中删除
                    final String id = info.getId()+"";
                    ThreadManager.postTaskToIOHandler(new Runnable() {
                        @Override
                        public void run() {
                            queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                                    null,
                                    MediaStore.Files.getContentUri("external"),
                                    MediaStore.Files.FileColumns._ID + " = ? ",
                                    new String[]{ id });
                        }
                    });
                    continue;
                }
                if (info.getName() == null || info.getName().isEmpty()) {
                    info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                }
                list.add(info);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
        }
        return list;
    }


}
