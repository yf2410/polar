package com.polar.browser.bookmark;

import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.jni.NativeManager;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DnsUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.common.Constants;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 用于管理收藏 TODO: 将changePosition函数做成异步执行
 * <p>
 * edit by saifei
 * 2017-3-31 加上书签同步功能。
 * <p>
 * 本地书签与服务器保存的书签需要分开保存在本地。
 * 同步书签时 也只是更新本地保存的服务器书签。
 * 当登出时，显示本地书签，登陆后判断是否需要更新本地保存的服务器书签然后显示。
 *
 * @author dpk
 */
public class BookmarkManager {

    private static final String TAG = "BookmarkManager";

    private static final String BOOKMARK_FILE_NAME_LOCAL = "bookmark";
    private static final String BOOKMARK_BACKUP_FILE_NAME_LOCAL = "bookmark_backup";


	private static BookmarkManager sInstance;
	private BookmarkStorage mStorage;
	private File mLocalFile;
	private File mLocalBackupFile;
	private File mOnlineFile; //保存服务端书签
	private File mOnlineBackupFile;
	private List<IBookmarkObserver> mObserverList = new ArrayList<>();
	public boolean isSyncing;
    public boolean isBookMarkChanged;
    //用来存储 切换账号时，书签文件没切换过来时用户添加书签 时的数据。
    private Map<String, String> tempMap = new ConcurrentHashMap<>();

    private BookmarkManager() {
		mStorage = new BookmarkStorage();
	}

	public static BookmarkManager getInstance() {
		if (null == sInstance) {
			synchronized (BookmarkManager.class) {
				if (null == sInstance) {
					sInstance = new BookmarkManager();
				}
			}
		}
		return sInstance;
	}

	//TODO  定义一个到收藏界面的方法。
	public static void goFavPage() {
		ConfigWrapper.put(ConfigDefine.DEFAULT_BOOKMARK_HISTORY, 1);
		ConfigWrapper.apply();
	}

	public String getPath() {
		String path = "";
		if (bookmarkFile() != null) {
			path = bookmarkFile().getAbsolutePath();
		}
		return path;
	}

	File bookmarkFile() {
		return AccountLoginManager.getInstance().isUserLogined()?mOnlineFile:mLocalFile;
	}

	String fileName() {
		return AccountLoginManager.getInstance().isUserLogined() ? onlineBookmarkFileName()
				: BOOKMARK_FILE_NAME_LOCAL;
	}

	private String onlineBookmarkFileName(){
		return ConfigManager.getInstance().getUserId()+"bookmark_online";
	}


	private String onlineBackupFileName() {
		return ConfigManager.getInstance().getUserId()+"bookmark_backup_online";
	}





	public void init() {
		String filePath = String.format("%s/%s", JuziApp.getAppContext()
				.getFilesDir(), fileName());
		initFile(filePath);
		List<BookmarkInfo> bookmarkList = queryBookmarkInfo();
		List<String> hostList = new ArrayList<>();
		for (int i = 0; i < bookmarkList.size(); ++i) {
			hostList.add(UrlUtils.getHost(bookmarkList.get(i).url));
		}
		DnsUtils.preloadBookmarkDns(hostList);
		initNative();
	}


    private String backupFileName() {
        return AccountLoginManager.getInstance().isUserLogined() ? onlineBackupFileName()
                : BOOKMARK_BACKUP_FILE_NAME_LOCAL;
    }

    private File backupFile() {
        return AccountLoginManager.getInstance().isUserLogined() ? mOnlineBackupFile : mLocalBackupFile;
    }

    public String localFilePath() {
        return String.format("%s/%s", JuziApp.getAppContext()
                .getFilesDir(), BOOKMARK_FILE_NAME_LOCAL);
    }

    public String onlineFilePath() {
        return String.format("%s/%s", JuziApp.getAppContext()
                .getFilesDir(), onlineBookmarkFileName());
    }

    private synchronized void init(String filePath) {
        initFile(filePath);
        List<BookmarkInfo> bookmarkList = queryBookmarkInfo();
        List<String> hostList = new ArrayList<>();
        for (int i = 0; i < bookmarkList.size(); ++i) {
            hostList.add(UrlUtils.getHost(bookmarkList.get(i).url));
        }
        DnsUtils.preloadBookmarkDns(hostList);
        initNative();
    }

    private void initFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            mStorage.createFile(file);
        } else {
            mStorage.init(file);
        }
        if (AccountLoginManager.getInstance().isUserLogined()) {
            mOnlineFile = file;
        } else {
            mLocalFile = file;
        }
    }

    public void registerObserver(IBookmarkObserver observer) {
        mObserverList.add(observer);
    }

    public void unregisterObserver(IBookmarkObserver observer) {
        mObserverList.remove(observer);
    }

    /**
     * 注意：本函数目前工作在IO线程上
     */
    public void notifyChanged(boolean isAdd) {
        for (IBookmarkObserver observer : mObserverList) {
            observer.notifyBookmarkChanged(isAdd,true);
        }
    }

    public void notifyChanged(boolean isAdd,boolean showTip) {
        for (IBookmarkObserver observer : mObserverList) {
            observer.notifyBookmarkChanged(isAdd,showTip);
        }
    }


    /**
     * 批量导入书签
     *
     * @param title
     * @param url
     */
    public void addBookmarkImport(final String title, final String url) {
        if (isUrlExist(url)) {
            return;
        }
        doAddToFavFun(title, url, true);
    }

    public void finishBookmarkImport() {
        mStorage.saveBookmarkFile(bookmarkFile());
    }

    /**
     * 添加书签
     *
     * @param title
     * @param url
     */
    public void addBookmark(final String title, final String url) {
       addBookmark(title,url,true);
    }

    public void addBookmark(final String title,final String url,boolean isShowTip ){
        if("false".equals(ConfigManager.getInstance().getLoginSyncBookmarkStateByUserId())){//用户账号登录后 还没有同步成功，这时候需要把书签添加到缓存，等同步成功后再添加到文件。
            BookmarkManager.getInstance().addToTemp(title,url);
            return;
        }

        // TODO 如果title是http开始的，就需要重新收藏。
        /*if (!TextUtils.isEmpty(url)) {
			CustomToastUtils.getInstance().showImgToast(R.string.add_bookmark_tips, R.drawable.address_bookmark_star_added);
		}*/
        if (isUrlExist(url)&&isShowTip) {
            CustomToastUtils.getInstance().showImgToast(R.string.already_add_bookmark_tips, R.drawable.address_bookmark_star_added);
            return;
        }

        doAddToFav(title, url);
        notifyChanged(true,isShowTip);
    }

    private void addToTemp(String title, String url) {
        SimpleLog.d(TAG,"==========addToTemp===========title="+title+"===url="+url);
        tempMap.put(title, url);
    }


    public void backupBookmark() {
        String srcPath = BookmarkManager.getInstance().getPath();
        String backupPath = String.format("%s/%s", JuziApp.getAppContext().getFilesDir(), backupFileName());
        File file = new File(backupPath);

        try {
            InputStream input = new FileInputStream(srcPath);
            File dest = new File(backupPath);
            FileUtils.copyFile(input, dest);
            input.close();
            if (AccountLoginManager.getInstance().isUserLogined()) {
                mOnlineBackupFile = file;
            } else {
                mLocalBackupFile = file;
            }
        } catch (FileNotFoundException e) {
            SimpleLog.e(e);
        } catch (IOException e) {
            SimpleLog.e(e);
        }
    }

    public void restoreBookmark() {
        if (backupFile() != null && backupFile().exists()) {
            importBookmark(backupFile(), false);
        }
    }

    public void deleteBookmarkById(final int id) {
        if (mStorage == null) {
            return;
        }
        Runnable r = new Runnable() {

            @Override
            public void run() {
                mStorage.deleteBookmarkById(id);
                mStorage.saveBookmarkFile(bookmarkFile());
                notifyChanged(false);
                initNative();
            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }

    public boolean isEmpty() {
        if (mStorage != null) {
            return mStorage.isEmpty();
        } else {
            return true;
        }
    }

    public void changePosition(int from, int to) {
        mStorage.changeItemPos(from, to);
        mStorage.saveBookmarkFile(bookmarkFile());
    }

    public void saveBookmark() {
        mStorage.saveBookmarkFile(bookmarkFile());
    }

    public void updateBookmarkById(final int id, final String name,
                                   final String url) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mStorage.updateBookmark(id, name, url);
                mStorage.saveBookmarkFile(bookmarkFile());
                // notifyChanged(false);
                initNative();
            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }

    public void deleteBookmarkByUrl(final String url) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                mStorage.deleteBookmarkByUrl(url);
                mStorage.saveBookmarkFile(bookmarkFile());
                notifyChanged(false);
                initNative();
            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }

    /**
     * 移除url 但是不通知更新
     *
     * @param url
     */
    public void deleteBookmarkByUrlNoToast(final String url) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                mStorage.deleteBookmarkByUrl(url);
                mStorage.saveBookmarkFile(bookmarkFile());
                // notifyChanged(false);
                initNative();
            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }

    public void deleteBookmarkByUrlList(final List<String> urlList) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (String url : urlList) {
                    mStorage.deleteBookmarkByUrl(url);
                }
                mStorage.saveBookmarkFile(bookmarkFile());
                notifyChanged(false);
                initNative();
            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }

    public List<BookmarkInfo> queryBookmarkInfo() {
        return mStorage.queryBookmarkInfo();
    }

    public BookmarkInfo queryBookmarkInfoByUrl(String url) {
        return mStorage.queryBookmarkInfoByUrl(url);
    }

    public List<Site> queryBookmarkToSite() {
        List<Site> sites = new ArrayList<>();
        List<BookmarkInfo> result = mStorage.queryBookmarkInfo();
        for (int i = 0; i < result.size(); ++i) {
            BookmarkInfo bookmark = result.get(i);
            String iconPath = String.format("%s/%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
                    CommonData.ICON_DIR_NAME, UrlUtils.getHost(bookmark.url));
            Site site = new Site(bookmark.name, bookmark.url, iconPath);
            sites.add(site);
        }
        return sites;
    }

    public boolean isUrlExist(String url) {
        return mStorage.isUrlExist(url);
    }

    public String toJsonString() {
        return mStorage.toJsonString();
    }

    public void initNative() {
        String bookmarkJson = toJsonString();
        NativeManager.initNativeQueryData(
                NativeManager.NATIVE_QUERY_DATA_TYPE_Bookmark, bookmarkJson);
    }

    public void changeBookmark(String title, String url) {
        if (isUrlExist(url)) {
            deleteBookmarkByUrl(url);
        } else {
            addBookmark(title, url);
        }
    }

    /**
     * 重新添加到收藏
     *
     * @param title
     * @param url
     */
    public void reAddToFav(final String title, final String url) {
        // TODO 如果title是http开始的，就需要重新收藏。
        if (isUrlExist(url)) {
            deleteBookmarkByUrlNoToast(url);
        }
        doAddToFav(title, url);
    }


    private void doAddToFav(final String title, final String url) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                doAddToFavFun(title, url, false);
            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }

    public boolean importBookmark(File file, boolean isImport) {
        boolean isSuccess = false;
        if (mStorage != null) {
            isSuccess = mStorage.importBookmarkFromFile(file, isImport);
            if (isSuccess) {
                saveBookmark();
            }
        }
        return isSuccess;
    }

    //TODO 公共的代码抽取一下
    private void doAddToFavFun(final String title, final String url, final boolean isMult) {
        if (mStorage == null || bookmarkFile() == null || TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
            return;
        }
        SimpleLog.e(TAG, "addbookmark: url" + url);
        byte[] utf8TitleBytes = null;
        if (title != null && title.length() > 0) {
            mStorage.addBookmark(title, url);
            utf8TitleBytes = FileUtils.stringToUtf8Bytes(title);
        } else if (!TextUtils.isEmpty(url)) {
            mStorage.addBookmark(url, url);
            utf8TitleBytes = FileUtils.stringToUtf8Bytes(url);
        }
        if (!isMult) {
            mStorage.saveBookmarkFile(bookmarkFile());
        }
        String unicodeUrl = FileUtils.utf8toUnicode(url);
        if (utf8TitleBytes != null && !TextUtils.isEmpty(unicodeUrl)) {
            NativeManager.addItem(
                    NativeManager.NATIVE_QUERY_DATA_TYPE_Bookmark,
                    utf8TitleBytes, unicodeUrl, Constants.NAVIGATESOURCE_NORMAL,
                    System.currentTimeMillis());
        }
    }

    boolean importBookmark(File file) {
        return importBookmark(file, true);
    }


    public synchronized void syncBookmark(RxFragmentActivity context,boolean isManualSync) {
        if (!AccountLoginManager.getInstance().isUserLogined()) return;
        BookmarkService.startSync(context, isManualSync,ConfigManager.getInstance().getUserToken(),
                ConfigManager.getInstance().getSyncBookmarkTimeStamp());
    }

    public synchronized void logOutAndSync(final RxFragmentActivity context) {
        ConfigManager.getInstance().setLoginSyncBookmarkFinished("true");
        final String userToken = ConfigManager.getInstance().getUserToken();
        final String syncTimeStamp = ConfigManager.getInstance().getSyncBookmarkTimeStamp();
        Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                //拉出本地书签文件
                changeToLocalFile();
                return true;
            }
        }).subscribeOn(Schedulers.io()).compose(context.<Boolean>bindToLifecycle())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                BookmarkService.startLogOutAndSync(context,userToken,
                        syncTimeStamp);
            }
        });

    }


//
//    /**
//     * @param context
//     * @param isManualSync
//     * @param isFirstUpLoad 用户第一次上传书签，服务器没记录，需要把本地书签上传。
//     */
//    private synchronized void startUpLoadBookmark(Context context, boolean isManualSync, boolean isFirstUpLoad) {
//        if (!AccountLoginManager.getInstance().isUserLogined()) return;
//        Intent intent = new Intent(context, BookmarkService.class);
//        intent.setAction(ACTION_UPLOAD);
//        intent.putExtra("isManualSync", isManualSync);
//        intent.putExtra("isFirstUpLoad", isFirstUpLoad);
//        context.startService(intent);
//    }
//
//    private synchronized void startDownLoadBookmark(Context context, SyncBookmarkResult result, boolean isManualSync) {
//        Intent intent = new Intent(context, BookmarkService.class);
//        intent.setAction(ACTION_DOWNLOAD);
//        intent.putExtra("SyncBookmarkResult", result);
//        intent.putExtra("isManualSync", isManualSync);
//        context.startService(intent);
//    }

    public   void prepareOnlineFile() {
        if (mOnlineFile == null || !mOnlineFile.exists())
            initFile(onlineFilePath());
    }



    public  boolean isSyncing() {
        return isSyncing;
    }

    public  void setIsSyncing(boolean isSyncing) {
        this.isSyncing = isSyncing;
    }

    public  synchronized void changeToLocalFile() {
        init(localFilePath());
        notifyChanged(false,false);
    }

    public  synchronized void changeToOnlineFile() {
        if(!AccountLoginManager.getInstance().isUserLogined())return;
        init(onlineFilePath());
        notifyChanged(false,false);
    }

    public synchronized void addTempToUserFile() {
        //把缓存添加到文件
        if(tempMap.isEmpty())return;
        if(!AccountLoginManager.getInstance().isUserLogined())return;
        Set<String> keySet = tempMap.keySet();
        for (String title : keySet) {
            addBookmark(title,tempMap.get(title),false);
        }
        clearTemp();
        notifyChanged(false,false);
    }


    public void clearTemp() {
        tempMap.clear();
    }
}
