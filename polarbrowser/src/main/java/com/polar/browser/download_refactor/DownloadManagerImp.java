
package com.polar.browser.download_refactor;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.download_refactor.db.DownloadProvider.InsertDownloadCallback;
import com.polar.browser.download_refactor.db.DownloadProvider.QueryDownloadListsCallback;
import com.polar.browser.download_refactor.db.DownloadProvider.UpdateDownloadCallback;
import com.polar.browser.download_refactor.db.DownloadProvider.UpdateDownloadStatusCallback;
import com.polar.browser.download_refactor.dinterface.IContinuingStatusChange;
import com.polar.browser.download_refactor.dinterface.IInnDownloadObserver;
import com.polar.browser.download_refactor.dinterface.IProgressChange;
import com.polar.browser.download_refactor.dinterface.IStatusChange;
import com.polar.browser.download_refactor.util.ThreadManager;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadManagerImp implements IInnDownloadObserver,
        IStatusChange,
        IContinuingStatusChange,
        IProgressChange{
    private IInnDownloadObserver mObserver;
    private Map<Long, DownloadInfo> mDownloads = new HashMap<Long, DownloadInfo>(); ///> 延迟的列表 只有真正需要操作DownloadInfo才进行创建,以减少内存
    private SystemFacade mSystemFacade;
    private StorageManager mStorageManager;
    private DownloadProvider mDownloadProvider;
    private Context mContext;
    private boolean mIsRecovery;

    public void init(Context context, IInnDownloadObserver ob,boolean isRecovery) {
        mContext = context;
        mObserver = ob;
        mSystemFacade = new SystemFacade(context);
        mStorageManager = new StorageManager(context);
        mDownloadProvider = DownloadProvider.getInstance();
        mDownloadProvider.init(context);
        mIsRecovery = isRecovery;
        postTask(new Runnable() {         
            @Override
            public void run() {
                _init();
            }
        });
    }

    public void unInit() {
        DownloadExecutor.getInstance().shutdown();
        /* XXX
        for (DownloadInfo info : mDownloads.values()) {
            info.stop();
            info.waitTask(1000);
        }
        */
        mDownloadProvider.unInit();
    }
    
    
    public void startWaitNetworkDownloads(final List<String> allowUrls){
        postTask(new Runnable() {         
            @Override
            public void run() {
                _startWaitNetworkDownloads(allowUrls);
            }
        });
    }
    
    public boolean getDownloadList() {
        postTask(new Runnable() {
            @Override
            public void run() {
                _getDownloadList();
            }
        });
        return true;
    }

    public boolean createDownload(final Request request) {
        postTask(new Runnable() {
            @Override
            public void run() {
                _createDownload(request);
            }
        });
        return true;
    }

    public boolean deleteDownload(final long[] ids,final boolean isDeleteFile) {
        postTask(new Runnable() {
            @Override
            public void run() {
                _deleteDownload(ids,isDeleteFile);
            }
        });
        return true;
    }

    public boolean resumeDownload(final long id) {
        postTask(new Runnable() {
            @Override
            public void run() {
                _resumeDownload(id);
            }
        });
        return true;
    }

    public boolean pauseDownload(final long id) {
        postTask(new Runnable() {
            @Override
            public void run() {
                _pauseDownload(id);
            }
        });
        return true;
    }
    
    public boolean restartDownload( final long id ){
        postTask(new Runnable() {
            @Override
            public void run() {
                _restartDownload(id);
            }
        });
        return true;
    }

    public void deleteObjFile(DownloadItemInfo downloadItemInfo){
        String fileDataPath = VCStoragerManager.getInstance().getDownloadDataDirPath() + downloadItemInfo.getFilename() + ".obj";
        File fileObj = new File(fileDataPath);
        if (fileObj.exists() && !fileObj.delete());
    }

    /**
     * 更改下载文件名
     * @param id
     * @param filePath
     * @return
     */
    public boolean renameDownloadFilePath(final long id, final String filePath, final DownloadProvider.UpdateDownloadCallback callback) {
        postTask(new Runnable() {
            @Override
            public void run() {
                _renameDownloadFilePath(id, filePath, callback);
            }
        });
        return true;
    }

    public interface IGetRunningCount{
        public void onTaskRuning(long countRuning);
    }
    public void hasRunningTask(final IGetRunningCount callback){
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long count = 0;
                for (DownloadInfo info : mDownloads.values()) {
                    if (info.isActiveDownload()) {
                        count++;
                    }
                }
                callback.onTaskRuning(count);
            }
        };
        postTask(runnable);
    }

    public ArrayList<DownloadItemInfo> getDownloadListSyn() {
        _assertDownloadCtrlThread();
        ArrayList<DownloadItemInfo> downloadLists = null;
        try {
            if (mDownloadProvider == null)
                return null;
            downloadLists = mDownloadProvider.getDownloadListSyn();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return downloadLists;
    }
    
    private void _getDownloadList() {
        _assertDownloadCtrlThread();
        try {
            if (mDownloadProvider == null)
                return;
            mDownloadProvider.getDownloadLists(new QueryDownloadListsCallback() {
                @Override
                public void onDownloadLists(ArrayList<DownloadItemInfo> lists) {
                    _assertDownloadCtrlThread();
                    handleDownloadLists(lists);
                }
            },getHandler());
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void _createDownload(final Request request) {
        _assertDownloadCtrlThread();
        mDownloadProvider.insert(request.toContentValues(), new InsertDownloadCallback() {
            @Override
            public void onInsertDownload(long id) {
                _assertDBThread();
                _createDownloadInfoAndStart(id);   
            }
        });
    }

    private void _deleteDownload(final long[] ids, final boolean isDeleteFile) {
        _assertDownloadCtrlThread();
        final ArrayList<DownloadInfo> array = new ArrayList<DownloadInfo>();
        for (int i = 0; i < ids.length; i++) {
            DownloadInfo info = mDownloads.get(ids[i]);
            if(mDownloads.containsKey(ids[i])){
                info = mDownloads.get(ids[i]);
                if (info.mStatus == Downloads.Impl.STATUS_RUNNING) {
                    info.stop();
                }
            }else{
                info = new DownloadInfo(mContext, mSystemFacade, mStorageManager, this,this,this);
                info.mId = ids[i];
            }
            array.add(info);
        }
        mDownloadProvider.postTask(new Runnable() {
            @Override
            public void run() {
                _assertDBThread();
               for (int j = 0; j < array.size(); j++) {
                   DownloadInfo info = array.get(j);
                   mDownloadProvider.getDownloadInfo(info.mId, info);
                   if (200 == info.mStatus) {
                       Statistics.sendOnceStatistics(
                               GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_DEL_COMPLETE);
                   } else {
                       Statistics.sendOnceStatistics(
                               GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_DEL_NOCOMPLETE);
                   }
                   if(isDeleteFile){
                       _deleteFileIfExists(info.mDestFile);
                       _deleteFileIfExists(info.mDownlaodFile);
                   }
                   _deleteFileObjIfExists(info.mDestFile);
                   mDownloadProvider.delete(info.mId);
               }
               
               postTask(new Runnable() {
                   @Override
                   public void run() {
                       _assertDownloadCtrlThread();
                       for (int i = 0; i < ids.length; i++)
                           mDownloads.remove(ids[i]);
                       _this().handleDownloadItemRemoved(true, ids);
                   }
               });
            }
        });
    }


    private void _resumeDownload(final long id) {
        _assertDownloadCtrlThread();
        DownloadInfo info = mDownloads.get(id);
        if(mDownloads.containsKey(id)){
            info = mDownloads.get(id);
        }else{
            info = new DownloadInfo(mContext, mSystemFacade, mStorageManager, this,this,this);
            info.mId = id;
        }
        final DownloadInfo i = info;
        mDownloadProvider.postTask(new Runnable() {
            @Override
            public void run() {
                _startDownload(id,i,false);
            }
        });
    }

    private void _pauseDownload(long id) {
        _assertDownloadCtrlThread();
        DownloadInfo info = mDownloads.get(id);
        if (info == null) {
//            _assert(false);
        }
        else
            info.stop();
    }
    
    private void _restartDownload(long id){
        _assertDownloadCtrlThread();
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, -1);
        values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_RUN);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        values.put(Downloads.Impl.COLUMN_FAILED_CONNECTIONS, 0);
        mDownloadProvider.updateDownload(id, values,new UpdateDownloadCallback() {
            @Override
            public void onUpdateDownload(boolean ret, long id) {
                mDownloadProvider.resertDownloadPath(id);
                resumeDownload(id);    
            }
        });   
    }
    
    private void _createDownloadInfoAndStart( final long id ){
        _assertDBThread();
        final DownloadInfo info = new DownloadInfo(mContext, mSystemFacade, mStorageManager, this,this,this);
        info.mId = id;
        _startDownload(id,info,true);
    }
    
    private void _startDownload(final long id, final DownloadInfo info,final boolean isCreate) {
        _assertDBThread(); 
        if (id != -1) {
            mDownloadProvider.getDownloadInfo(id, info);
            _createDestFile(info.mDestFile);
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_RUN);
            values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
            mDownloadProvider.updateDownload(id, values);
            info.mStatus = Downloads.Impl.STATUS_PENDING;
            info.mControl = Downloads.Impl.CONTROL_RUN;
        }

        postTask(new Runnable() {
            @Override
            public void run() {
                _assertDownloadCtrlThread();
                final boolean activeDownload = info.startDownloadIfReady(DownloadExecutor.getInstance());
                if (activeDownload)
                    ;
                mDownloads.put(id, info);
                if(isCreate)
                    handleDownloadItemAdded(id != -1, id, info.toDownloadItemInfo());
            }
        });
    }

    /*
    XXXX
    bugfix:同时下载的任务达到限制数量后，再重复下载没有对目标文件进行重命名.
            由于上层是判断文件是否存在来进行改名所以创建新任务时提前创建文件,对于已经存在的文件则不进行创建
    !!! 这个临时占坑文件在downloadtask->run 会根据大小接状态判断是新下载或是重新下载 会将此文件再次删除,所以依赖于这些逻辑..暂时这么处理
    */
    private void _createDestFile(String path) {
        if(path == null || path.isEmpty())
            return;
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void _renameDownloadFilePath(final long id, final String filePath, DownloadProvider.UpdateDownloadCallback callback) {
        if (id != -1) {
            if (mDownloadProvider == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_FILE_PATH, filePath);
            mDownloadProvider.updateDownload(id, values, callback, getHandler());
        }
    }

    ///>下层 DownloadInfo DownloadTask 相关回调--------------------------------------------------
    @Override
    public void onDownloadStatusChange(long id, int status) {
        mDownloadProvider.updateDownloadStatus(id, status,new UpdateDownloadStatusCallback() {
            @Override
            public void onUpdateDownloadStatus(boolean ret, long id, int status) {
                _assertDownloadCtrlThread();
                DownloadInfo info = mDownloads.get(id);
                if(info != null)
                    info.mStatus = status;
                
                int uiStatus = UiStatusDefine.translateStatus(status);
                int reason = UiStatusDefine.getReason(status);
                handleDownloadStatus(id,uiStatus,reason);      
            }
        },getHandler());
    }
    
    @Override
    public void onContinuingStatusChange(long id, int continuingState) {
        _asssertUnlimiteThread();
        mObserver.onContinuingStatusChange(id,continuingState);
    }
    
    @Override
    public void onProgressChange(long id, long currentBytes, long totalBytes, long speedBytes) {
        _asssertUnlimiteThread();
        handleDownloadProgress(id, currentBytes, totalBytes, speedBytes);   
    }
    
    
    ///>-----------------------------------------------------------------------------------
    
    ///>通知上层管理模块----------------------------------------------------------------
    @Override
    public void handleDownloadStatus(final long id, final int status, final int reason) {
        _asssertUnlimiteThread();
        mObserver.handleDownloadStatus(id, status, reason);
        if(status == UiStatusDefine.STATUS_SUCCESSFUL && _isDownloadProtection()){
            _downloadComplete(id);
            Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
            intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, true);
            JuziApp.getInstance().sendBroadcast(intent);
        }    
    }
    
    

    @Override
    public void handleDownloadProgress(final long id, final long currentBytes,
            final long totalBytes, final long speedBytes) {
        _asssertUnlimiteThread();
        mObserver.handleDownloadProgress(id, currentBytes, totalBytes, speedBytes);
    }

    @Override
    public void handleDownloadLists(final ArrayList<DownloadItemInfo> lists) {
        _asssertUnlimiteThread();
        mObserver.handleDownloadLists(lists);
    }

    @Override
    public void handleDownloadItemAdded(final boolean ret, final long id,
            final DownloadItemInfo info) {
        _asssertUnlimiteThread();
        mObserver.handleDownloadItemAdded(ret, id, info);
    }

    @Override
    public void handleDownloadItemRemoved(final boolean ret, final long[] id) {
        _asssertUnlimiteThread();
        mObserver.handleDownloadItemRemoved(ret, id);
    }
    

    @Override
    public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval) {
        _asssertUnlimiteThread();
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_VIRUSCHECK, virusStatus);
        mDownloadProvider.updateDownload(id, values, null); 
        mObserver.handleDownloadVirusStatus(id, virusStatus, md5, interval);
    }
    
    ///---------------------------------------------------------------------
    
    ///>外围补充逻辑------------------------------------------------------------
    private void _init(){
        _assertDownloadCtrlThread();
        mDownloadProvider.getDownloadLists(new QueryDownloadListsCallback() {
            @Override
            public void onDownloadLists(ArrayList<DownloadItemInfo> lists) {
                _startPreUnfinishedDownloads(lists);
                handleDownloadLists(lists);
            }
        });
    }
    
    private void _startPreUnfinishedDownloads(ArrayList<DownloadItemInfo> lists){
        _asssertUnlimiteThread();
        boolean bFlag = true;
        for (DownloadItemInfo info : lists) {
            if( (info.mStatus == UiStatusDefine.STATUS_RUNNING || 
                    info.mStatus == UiStatusDefine.STATUS_PENDING || 
                    (info.mStatus == UiStatusDefine.STATUS_PAUSED && (info.mReason == UiStatusDefine.PAUSED_WAITING_FOR_NETWORK || info.mReason == UiStatusDefine.PAUSED_QUEUED_FOR_WIFI))) && 
                    _isRecovery()){
                if(bFlag){
                    startPreUnfinishedDownloads();
                    bFlag = false;
                }
                _this().resumeDownload(info.mId);      
            }else if( info.mStatus == UiStatusDefine.STATUS_PENDING || 
                      info.mStatus == UiStatusDefine.STATUS_RUNNING  ||
                      info.mStatus == UiStatusDefine.STATUS_PAUSED    
                    ) {
                mDownloadProvider.updateDownloadStatus(info.mId, Downloads.Impl.STATUS_PAUSED_BY_APP);
            }else{
                
            }
        }
    }
    
    
    private void _startWaitNetworkDownloads(final List<String> allowUrls){
        _assertDownloadCtrlThread();
        mDownloadProvider.getDownloadLists(new QueryDownloadListsCallback() {
            @Override
            public void onDownloadLists(ArrayList<DownloadItemInfo> lists) {
                _startWaitNetworkDownloads(lists,allowUrls);
            }
        });
    }
    
    private void _startWaitNetworkDownloads(ArrayList<DownloadItemInfo> lists,List<String> allowUrls){
        _asssertUnlimiteThread();
        for (DownloadItemInfo info : lists) {
            if( info.mStatus == UiStatusDefine.STATUS_PAUSED && 
                    ( info.mReason == UiStatusDefine.PAUSED_WAITING_FOR_NETWORK || info.mReason == UiStatusDefine.PAUSED_QUEUED_FOR_WIFI)){
                if( (allowUrls!=null && allowUrls.contains(info.mUrl)) || allowUrls==null )
                    _this().resumeDownload(info.mId);      
            }
        }
    }
    
    
    private  boolean _isRecovery(){
        return mIsRecovery;
    } 

    private boolean _isDownloadProtection(){
    	// TODO Fix me
//        return SettingsModel.getInstance().getDownloadsProtection();
    	return true;
    }
    
    private void _downloadComplete( final long id ){
        _assertDownloadCtrlThread();
        DownloadInfo info = mDownloads.get(id);
        if(info != null){
            _scanVirus(info); 
            DownloadUtil.requestMediaScan(info.mDestFile);
        }
    }
    
    @SuppressLint("DefaultLocale")
	private void _scanVirus(final DownloadInfo info){
        if (info.mDestFile !=null && !info.mDestFile.isEmpty() && info.mDestFile.toLowerCase().endsWith(".apk")) {
        	// TODO 扫描病毒？ 暂时无用 注释 20160726
//            VirusScan.scan(info.mId, info.mDestFile, this);
        }
    }
    
    private void _deleteFileIfExists(String path) {
        if (!TextUtils.isEmpty(path)) {
            final File file = new File(path);
            if (file.exists() && !file.delete()) {
                
            }
        }
    }

    private void _deleteFileObjIfExists(String path) {
        if (!TextUtils.isEmpty(path)) {
            final File file = new File(path);
            String name = file.getName();
            String fileDataPath = VCStoragerManager.getInstance().getDownloadDataDirPath() + name + ".obj";
            File fileObj = new File(fileDataPath);
            if (fileObj.exists() && !fileObj.delete()) {

            }
        }
    }
    
    ///>---------------------------------------------------------------------
    
    ///>辅助类----------------------------------------------------------------
    public void postTask(Runnable r) {
        ThreadManager.post(ThreadManager.THREAD_DOWNLOAD_CTRL, r);
    }

    private Handler getHandler() {
        return ThreadManager.getHandler(ThreadManager.THREAD_DOWNLOAD_CTRL);
    }
    
    private void _asssertUnlimiteThread(){
        
    }
    
    private void _assertDBThread() {
        _assert(ThreadManager.getHandler(ThreadManager.THREAD_DB).getLooper().getThread().getId() == Thread
                .currentThread().getId());
    }

    private void _assertDownloadCtrlThread() {
        _assert(ThreadManager.getHandler(ThreadManager.THREAD_DOWNLOAD_CTRL).getLooper()
                .getThread().getId() == Thread.currentThread().getId());
    }

    private void _assert(boolean check) {
        if(AppEnv.DEBUG){
            if (!check)
                throw new RuntimeException();
        }
    }
    
    private DownloadManagerImp _this(){
        return DownloadManagerImp.this;
    }
    ///---------------------------------------------------------------------

    @Override
    public void startPreUnfinishedDownloads() {
        mObserver.startPreUnfinishedDownloads();
    }
    
    
}
