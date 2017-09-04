package com.polar.browser.manager;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by FKQ on 2016/9/9.
 */

public class VCStoragerManager {

    private static VCStoragerManager mInstance;
    private static final String APP_ROOT_DIR = "Polar";            //项目文件保存路径根目录为Polar
    private static final String APP_ROOT_DIR_DOWNLOAD = "Polar_download";
    private static final String APP_ROOT_DIR_IMAGE = "Polar_image";
    private static final String APP_ROOT_DIR_UPDATE = "Polar_update";
    private static final String APP_ROOT_DIR_CONFIG = "Polar_config";
    private static final String APP_ROOT_DIR_TMP = "tmp";
    private static final String DOWNLOAD_DATA = "data";              //下载数据持久化文件目录
    private static final String PLUG_DATA = "plug_data";
    private static final String HOME_CARD = "home_card";

    private static final String ICON_DIR_NAME = "icon";
    private static final String LOGO_DIR_NAME = "logo_dir";
    private static final String SUB_IMAGE_PATH = "sub_image";

    //项目第一次运行创建文件名
    private static final String FIRST_RUN_MARKER = "first_run";
    public static final String PLUG_DATA_HASOFFER = "hasoffer.js";
    public static final String HOME_CARD_HTML = "html/homecard.html";


    private String mSaveDirPath;            //项目文件保存路径根目录为Polar
    private String mDownloadDirPath;        //下载目录
    private String mDefaultDownloadDirPath; //默认下载目录
    private String mImageDirPath;           //图片存储路径
    private String mUpdateDirPath;          //版本更新文件存储路径
    private String mDataDirPath;            //各种数据文件存储路径
    private String mDownloadDataDirPath;    //默认下载路径下保存下载文件对象信息（状态、进度-持久化.obj文件）
    private String mPlugDataDirPath;
    private String mHomeCardDirPath;

    private VCStoragerManager(){

    }
    public static VCStoragerManager getInstance() {
        if (mInstance == null) {
            synchronized (VCStoragerManager.class) {
                if (mInstance == null) {
                    mInstance = new VCStoragerManager();
                }
            }
        }
        return mInstance;
    }

    public void init() {
        createAppDir();
    }

    /**
     * 判断SDCard是否可用
     * @return
     */
    private static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 判断存储卡是否是可以卸载的
     * @return
     */
    private static boolean isSDCardRemovable() {
        return Environment.isExternalStorageRemovable();
    }

    public String[] getStorageDirectorys() {
        StorageManager sm = (StorageManager) JuziApp.getInstance().getSystemService(Context.STORAGE_SERVICE);
        // 获取sdcard的路径：外置和内置
        try {
            //3.0以上可以通过反射获取
            String[] paths = (String[]) sm.getClass()
                    .getMethod("getVolumePaths", new Class<?>[]{}).invoke(sm, new Object[]{});
            return paths;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取手机存储
     * @return
     */
    public String getPhoneStorage() {
        String phoneStorage = null;
        if (isSDCardEnable()) {
            phoneStorage = Environment.getExternalStorageDirectory().toString();
        } else {
            phoneStorage = JuziApp.getInstance().getFilesDir().toString();
        }
        if (!isSDCardRemovable()) {
            return phoneStorage;
        } else { // SDCard能被移除，根据SDCard获取手机存储
            String[] paths = getStorageDirectorys();
            if (paths != null) {
                for (int i = 0; i < paths.length; i++) {
                    boolean canWrite = new File(paths[i]).canWrite();
                    SimpleLog.e("", "paths[" + i + "] == " + paths[i]);
                    if (canWrite && !TextUtils.equals(phoneStorage, paths[i])) {
                        phoneStorage = paths[i];
                        break;
                    }
                }
            }
        }
        return phoneStorage;
    }

    /**
     * 获取SDCard存储
     *
     * @return 可能为null
     */
    public String getSDCardStorage() {
        String sDCardStorage = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { // We can read and write
            sDCardStorage = Environment.getExternalStorageDirectory()
                    .toString();
        }
        boolean canRemove = Environment.isExternalStorageRemovable();
        if (canRemove) {
            // SDCard存储能被移除
            SimpleLog.d("Juzi", "SDCard存储 == = " + sDCardStorage);
            return sDCardStorage;
        } else {
            String phoneStory = sDCardStorage;
            sDCardStorage = null;
            // 手机存储不能被移除，根据手机存储获取SDCard
            String[] paths = getStorageDirectorys();
            if (paths != null) {
                for (int i = 0; i < paths.length; i++) {
                    boolean canWrite = new File(paths[i]).canWrite();
                    SimpleLog.e("", "paths[" + i + "] == " + paths[i]);
                    if (canWrite && !TextUtils.equals(phoneStory, paths[i])) {
                        sDCardStorage = paths[i];
                        break;
                    }
                }
            }
        }
        SimpleLog.d("Juzi", "SDCard存储 == = " + sDCardStorage);
        return sDCardStorage;
    }

    /**
     * @Description: 检查目是否存在,不存在则创建
     * @param filePath
     * @return
     */
    private static boolean checkDir(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) {
            return f.mkdirs();
        }
        return true;
    }



    /**
     * 创建文件目录
     */
    private void createAppDir() {
        getAppRootDirPath();
        getDefaultDownloadDirPath();
        getDownloadDirPath();
        getImageDirPath();
        getUpdatePath();
        getDataPath();
        getDownloadDataDirPath();
//        getHomeCardPath();
        getPlugDataPath();
    }

    /**
     * 应用是否首次运行
     *
     * @return
     */
    public boolean firstRunCheck() {
        StringBuffer sb = new StringBuffer();
        String firstRunMarker = sb.append(JuziApp.getInstance().getFilesDir().toString())
                .append(File.separator).append(FIRST_RUN_MARKER).toString();
        File file = new File(firstRunMarker);
        if (file.exists()) {
            return false;
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
            return true;
        }
    }

    /**
     * 文件保存路径根目录为Polar
     * @return
     */
    public String getAppRootDirPath() {
        StringBuffer sb = new StringBuffer();
        mSaveDirPath = sb.append(getPhoneStorage()).append(File.separator).append(APP_ROOT_DIR).toString();
        checkDir(mSaveDirPath);
        return mSaveDirPath;
    }

    /**
     * 默认下载路径
     * @return
    */
    public String getDefaultDownloadDirPath() {
        StringBuffer sb = new StringBuffer();
        mDefaultDownloadDirPath = sb.append(getPhoneStorage()).append(File.separator)
                .append(APP_ROOT_DIR).append(File.separator)
                .append(APP_ROOT_DIR_DOWNLOAD).append(File.separator)
                .toString();
        checkDir(mDefaultDownloadDirPath);
        return mDefaultDownloadDirPath;
    }

    /**
     * 下载路径
     * @return
     */
    public String getDownloadDirPath() {
        StringBuffer sb = new StringBuffer();
        mDownloadDirPath = ConfigWrapper.get(CommonData.KEY_DOWN_ROOT, null);
        if (mDownloadDirPath == null) {
            mDownloadDirPath = sb.append(getPhoneStorage()).append(File.separator)
                    .append(APP_ROOT_DIR).append(File.separator)
                    .append(APP_ROOT_DIR_DOWNLOAD).append(File.separator)
                    .toString();
        }
        checkDir(mDownloadDirPath);
        return mDownloadDirPath;
    }

    /**
     * 设置下载路径
     * @return
     */
    public void setDownloadDirPath(String downloadDirath) {
        mDownloadDirPath = downloadDirath;
    }

    /**
     * 图片存储路径
     * @return
     */
    public String getImageDirPath() {
        StringBuffer sb = new StringBuffer();
        mImageDirPath = sb.append(getPhoneStorage()).append(File.separator)
                .append(APP_ROOT_DIR).append(File.separator)
                .append(APP_ROOT_DIR_IMAGE).append(File.separator)
                .toString();
        checkDir(mImageDirPath);
        return mImageDirPath;
    }

    /**
     * 版本更新文件存储路径
     * @return
     */
    public String getUpdatePath() {
        StringBuffer sb = new StringBuffer();
        mUpdateDirPath = sb.append(getPhoneStorage()).append(File.separator)
                .append(APP_ROOT_DIR).append(File.separator)
                .append(APP_ROOT_DIR_UPDATE).append(File.separator)
                .toString();
        checkDir(mUpdateDirPath);
        return mUpdateDirPath;
    }

    /**
     * 各种数据文件存储路径
     * @return
     */
    public String getDataPath() {
        StringBuffer sb = new StringBuffer();
        mDataDirPath = sb.append(getPhoneStorage()).append(File.separator)
                .append(APP_ROOT_DIR).append(File.separator)
                .append(APP_ROOT_DIR_CONFIG).append(File.separator)
                .toString();
        checkDir(mDataDirPath);
        return mDataDirPath;
    }

    /**
     * 默认下载路径下保存下载文件对象信息（状态、进度-持久化.obj文件）
     * @return
     */
    public String getDownloadDataDirPath() {
        StringBuffer sb = new StringBuffer();
        mDownloadDataDirPath = sb.append(getPhoneStorage()).append(File.separator)
                .append(APP_ROOT_DIR).append(File.separator)
                .append(APP_ROOT_DIR_DOWNLOAD).append(File.separator)
                .append(DOWNLOAD_DATA).append(File.separator).toString();
        checkDir(mDownloadDataDirPath);
        return mDownloadDataDirPath;
    }

    /**
     * 插件js保存路径
     *
     * @return
     */
    public String getPlugDataPath() {
        StringBuffer sb = new StringBuffer();
        mPlugDataDirPath = sb.append(JuziApp.getInstance().getFilesDir().toString())
                .append(File.separator).append(PLUG_DATA).toString();
        checkDir(mPlugDataDirPath);
        return mPlugDataDirPath;
    }

    /**
     * 首页卡片html保存路径
     *
     * @return
     */
    public String getHomeCardPath() {
        StringBuffer sb = new StringBuffer();
        mHomeCardDirPath = sb.append(JuziApp.getInstance().getFilesDir().toString())
                .append(File.separator).append(HOME_CARD).toString();
        checkDir(mHomeCardDirPath);
        return mHomeCardDirPath;
    }

    /**
     * 各种数据文件存储路径
     * @return
     */
    public String getTmpPath() {
        StringBuffer sb = new StringBuffer();
        mDataDirPath = sb.append(getPhoneStorage()).append(File.separator)
                .append(APP_ROOT_DIR).append(File.separator)
                .append(APP_ROOT_DIR_TMP).append(File.separator)
                .toString();
        checkDir(mDataDirPath);
        return mDataDirPath;
    }

    public String getHasOfferJsPath() {
        StringBuffer sb = new StringBuffer();
        String hasOfferJsDirPath = sb.append(JuziApp.getInstance().getFilesDir().toString())
                .append(File.separator).append(ParseConfig.HASOFFER).toString();
        checkDir(hasOfferJsDirPath);
        return hasOfferJsDirPath;
    }
}
