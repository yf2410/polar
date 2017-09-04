package com.polar.browser.download_refactor;

import android.content.Context;

import com.polar.browser.download_refactor.DownloadManagerImp.IGetRunningCount;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.download_refactor.dinterface.IDownloadObserver;
import com.polar.browser.download_refactor.dinterface.IInnDownloadObserver;
import com.polar.browser.download_refactor.netstatus_manager.ManagerUIHandler.IDelegate;
import com.polar.browser.download_refactor.netstatus_manager.MoblieAllowDownloads;
import com.polar.browser.download_refactor.ui.DownloadNotify;
import com.polar.browser.download_refactor.util.ListenerList;
import com.polar.browser.download_refactor.util.ThreadManager;
import com.polar.browser.utils.SimpleLog;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import com.polar.browser.download_refactor.util.SettingsModel;

public class DownloadManager implements IInnDownloadObserver, IDelegate{ 
    private ListenerList<IDownloadObserver> mObservers = new ListenerList<IDownloadObserver>();   
    private DownloadManagerImp mDownloadManagerImp = new DownloadManagerImp();
    private Map<Long,DownloadItemInfo> mDownloadItemInfos = new HashMap<Long,DownloadItemInfo>();
    private boolean mInit = false;
    private Context mContext;
    private static DownloadManager sObjDownloadManager;
    private static final String TAG = "DownloadManager";
    /** 是否开启仅wifi下载 **/
    public boolean isOnlyWifiDownload = true;
    public static DownloadManager getInstance(){
        if (sObjDownloadManager == null) {
            synchronized (DownloadManager.class) {
                if (sObjDownloadManager == null) {
                    sObjDownloadManager = new DownloadManager();
                }
            }
        }
        return sObjDownloadManager;
    }
    
    public void init(Context context){
//        mDownloadManagerImp.init(context, this,!SettingsModel.getInstance().isNormalExit());
//    	SettingsModel.getInstance().setNormalExit(false);
    	// TODO Fix me
        mDownloadManagerImp.init(context, this, true);
        mInit = true;
        mContext = context;
    }

    public void unInit(){
    	
    	// TODO Fix me
//        SettingsModel.getInstance().setNormalExit(true);
        if (mInit) {
            mObservers.begin();
            Iterator<IDownloadObserver> it = mObservers.iterator();
            while (it.hasNext()) {
                IDownloadObserver listener = it.next();
                if (listener != null) {
                    mObservers.remove(listener);
                }
            }
            mObservers.end();

            mDownloadManagerImp.unInit();
        }
    }
    
    public void addObserver(final IDownloadObserver ob){
        if(ob!=null) {
            postUITask(new Runnable() {
                @Override
                public void run() {
                    mObservers.add(ob);
                }
            });
        }

    }
    
    public void removeObserver(final IDownloadObserver ob ){
        if(ob!=null) {
            postUITask(new Runnable() {
                @Override
                public void run() {
                    mObservers.remove(ob);
                }
            });
        }

    }
     
    public boolean getDownloadItemList(){
        return mDownloadManagerImp.getDownloadList();
    }

    public ArrayList<DownloadItemInfo> getDownloadListSyn(){
        return mDownloadManagerImp.getDownloadListSyn();
    }
    
    public DownloadItemInfo getDownloadItem( final long id ){
        if(mDownloadItemInfos.containsKey(id))
            return mDownloadItemInfos.get(id);
        return null;
    }
    
    public DownloadItemInfo getDownloadItem( final String url ){
        for (Map.Entry<Long, DownloadItemInfo> entry : mDownloadItemInfos.entrySet()) {
            DownloadItemInfo info = entry.getValue();
            if (info == null)
                return null;
            if (null != url && url.equals(info.mUrl))
                return info;
        }
        return null;
    }
    
    public boolean createDownload( final Request request ){
        return mDownloadManagerImp.createDownload(request);  
    }
    
    public boolean deleteDownload( final long[] ids,boolean isDeleteFile ){
        return mDownloadManagerImp.deleteDownload(ids,isDeleteFile);  
    }
    
    public boolean resumeDownload( final long id ){
        return mDownloadManagerImp.resumeDownload(id);  
    }

    public boolean pauseDownload( final long id ){
        return mDownloadManagerImp.pauseDownload(id);
    }
    
    public boolean restartDownload( final long id ){
        return mDownloadManagerImp.restartDownload(id);
    }
    
    public boolean isContuningDownloadSupported(final long id){
        if(mDownloadItemInfos.containsKey(id)){
            return mDownloadItemInfos.get(id).isContuningDownloadSupported();
        }
        return false;  
    }
    
        
    public void hasRunningTask(final IGetRunningCount callback) {
        mDownloadManagerImp.hasRunningTask( new IGetRunningCount() {
            @Override
            public void onTaskRuning(final long countRuning) {
                postUITask(new Runnable(){
                    @Override
                    public void run() {
                        callback.onTaskRuning(countRuning);
                    }
                });  
            }
        });
    }      

    public boolean renameDownloadFilePath(final long id, final String filePath, DownloadProvider.UpdateDownloadCallback callback) {
        return mDownloadManagerImp.renameDownloadFilePath(id, filePath, callback);
    }

    @Override
    public void handleDownloadLists(final ArrayList<DownloadItemInfo> lists) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                ///>XXX
                mDownloadItemInfos.clear();
                if (lists != null && lists.size() != 0) {
                    for (DownloadItemInfo info : lists) {
                        mDownloadItemInfos.put(info.mId, info);
                    }
                }
                
                mObservers.begin();
                try {
                  Iterator<IDownloadObserver> it = mObservers.iterator();
                  while(it.hasNext()) {
                      IDownloadObserver listener = it.next();
                     if(listener != null) {
                        listener.handleDownloadLists(lists);
                      }
                  }
                } catch (ConcurrentModificationException e) {
                    SimpleLog.e(e);
                }  finally {
                    mObservers.end();
                }
            
            }
        });
    }
    
    @Override
    public void handleDownloadItemAdded(final boolean ret, final long id,final DownloadItemInfo info) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                ///>XXX
                if(mDownloadItemInfos.containsKey(id))
                    return;
                
                mDownloadItemInfos.put(id, info); 
                mObservers.begin();
                try {
                  Iterator<IDownloadObserver> it = mObservers.iterator();
                  while(it.hasNext()) {
                      IDownloadObserver listener = it.next();
                     if(listener != null) {
                        listener.handleDownloadItemAdded(ret,id,info);
                      }
                  }
                } catch (ConcurrentModificationException e) {
                    SimpleLog.e(e);
                }  finally {
                    mObservers.end();
                }
            }
        });
    }

    @Override
    public void handleDownloadItemRemoved(final boolean ret, final long[] ids) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                for (int i = 0; i < ids.length; i++) {
                    DownloadItemInfo info = mDownloadItemInfos.get(ids[i]);
                    if(info!=null){
                        mDownloadItemInfos.remove(ids[i]);
                    }
                }
                
                mObservers.begin();
                try {
                  Iterator<IDownloadObserver> it = mObservers.iterator();
                  while(it.hasNext()) {
                      IDownloadObserver listener = it.next();
                     if(listener != null) {
                        listener.handleDownloadItemRemoved(ret,ids);
                      }
                  }
                } catch (ConcurrentModificationException e) {
                    SimpleLog.e(e);
                }  finally {
                    mObservers.end();
                }
                // 删除条目后 取消下载通知栏通知
                DownloadNotify.cancelNotify(ids);
            }
        });
    }

    @Override
    public void handleDownloadStatus(final long id, final int status, final int reason) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                ///>XXX
                DownloadItemInfo info = mDownloadItemInfos.get(id);
                if(info!=null){
                    info.mStatus = status;
                    info.mReason = reason;
                    
                    mObservers.begin();
                    try {
                      Iterator<IDownloadObserver> it = mObservers.iterator();
                      while(it.hasNext()) {
                          IDownloadObserver listener = it.next();
                         if(listener != null) {
                            listener.handleDownloadStatus(id,status,reason);
                          }
                      }
                    } catch (ConcurrentModificationException e) {
                        SimpleLog.e(e);
                    }  finally {
                        mObservers.end();
                    }
                }          
            }
        });
    }
    
    
    @Override
    public void handleDownloadProgress(final long id, final long currentBytes, final long totalBytes, final long speedBytes) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                ///>XXX
                DownloadItemInfo info = mDownloadItemInfos.get(id);
                if(info!=null){
                    info.mCurrentBytes = currentBytes;
                    info.mTotalBytes = totalBytes;
                    
                    mObservers.begin();
                    try {
                      Iterator<IDownloadObserver> it = mObservers.iterator();
                      while(it.hasNext()) {
                          IDownloadObserver listener = it.next();
                         if(listener != null) {
                            listener.handleDownloadProgress(id,currentBytes,totalBytes,speedBytes);
                          }
                      }
                    } catch (ConcurrentModificationException e) {
                        SimpleLog.e(e);
                    }  finally {
                        mObservers.end();
                    }
                }
            }
        });
    }
        
    private void postUITask( Runnable r ){
        ThreadManager.post(ThreadManager.THREAD_UI, r);
    }
    
    @Override
    public void handleDownloadVirusStatus(final long id, final int virusStatus, final String md5, final long interval) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                DownloadItemInfo info = mDownloadItemInfos.get(id);
                if(info!=null){
                    info.mVirusStatus = virusStatus;
                    
                    mObservers.begin();
                    try {
                      Iterator<IDownloadObserver> it = mObservers.iterator();
                      while(it.hasNext()) {
                          IDownloadObserver listener = it.next();
                         if(listener != null) {
                            listener.handleDownloadVirusStatus(id,virusStatus,md5,interval);
                          }
                      }
                    } finally {
                        mObservers.end();
                    }
                }    
            }
        });
        
    }
    
    @Override
    public void onContinuingStatusChange(final long id, final int continuingState) {
        postUITask(new Runnable() { 
            @Override
            public void run() {
                DownloadItemInfo info = mDownloadItemInfos.get(id);
                if(info!=null){
                    info.mContinuingState = continuingState;
                }
            }
        });
        
    }

    @Override
    public void startPreUnfinishedDownloads() {
        ///> XXX
        postUITask(new Runnable(){
            @Override
            public void run() {
//                SmartToast.showLongSingletonToast(mContext,
//                        R.string.s_download_text_auto_start_hint);
                }
        });  

    }

    @Override
    public void onDidFromWifiToMobile() {
    	// TODO Fix me
        if(!isOnlyWifiDownload) {
            mDownloadManagerImp.startWaitNetworkDownloads(null);
        } else {
            mDownloadManagerImp.startWaitNetworkDownloads(MoblieAllowDownloads.getInstance().getMoblieAllowDownloads());
            pauseAllDownloadingTasks();
        }

//        Toast.makeText(mContext, "Wifi To Mobile", Toast.LENGTH_SHORT).show();
//        SimpleLog.e(TAG, "Wifi To Mobile");

//        pauseAllDownloadingTasks();
//        Intent intent = new Intent(mContext, DownloadNetChangeDialog2.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (mContext instanceof ContextWrapper) {
//            ((ContextWrapper) mContext).startActivity(intent);
//        }

    }

    @Override
    public void onDidFromNullToMobile() {
    	// TODO Fix me
        if(!isOnlyWifiDownload) {
            mDownloadManagerImp.startWaitNetworkDownloads(null);
        } else {
            mDownloadManagerImp.startWaitNetworkDownloads(MoblieAllowDownloads.getInstance().getMoblieAllowDownloads());
            pauseAllDownloadingTasks();
        }

//        Toast.makeText(mContext, "Null To Mobile", Toast.LENGTH_SHORT).show();
//        SimpleLog.e(TAG, "Null To Mobile");

//        pauseAllDownloadingTasks();
//        Intent intent = new Intent(mContext, DownloadNetChangeDialog2.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (mContext instanceof ContextWrapper) {
//            ((ContextWrapper) mContext).startActivity(intent);
//        }
    }

    @Override
    public void onDidFromNullToWifi() {
//        Toast.makeText(mContext, "Null To Wifi", Toast.LENGTH_SHORT).show();
//        SimpleLog.e(TAG, "Null To Wifi");
        mDownloadManagerImp.startWaitNetworkDownloads(null);
    }

    @Override
    public void onDidFromMobileToWifi() {
//        Toast.makeText(mContext, "Mobile To Wifi", Toast.LENGTH_SHORT).show();
//        SimpleLog.e(TAG, "Mobile To Wifi");
        mDownloadManagerImp.startWaitNetworkDownloads(null);
    }

    @Override
    public void onDidNoAvailableNetwork() {
//        Toast.makeText(mContext, "NoAvailableNetwork", Toast.LENGTH_SHORT).show();
//        SimpleLog.e(TAG, "NoAvailableNetwork");
    }

    public void startWaitNetworkDownloads() {
        mDownloadManagerImp.startWaitNetworkDownloads(null);
    }

    private void pauseAllDownloadingTasks() {
        if (mDownloadItemInfos == null) {
            return;
        }
        DownloadItemInfo info = null;
        Iterator iter = mDownloadItemInfos.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, DownloadItemInfo> entry = (Map.Entry<Long, DownloadItemInfo>) iter.next();
            info = entry.getValue();
            if (info == null)
                continue;
            if (info.mStatus == UiStatusDefine.STATUS_PENDING ||
                    info.mStatus == UiStatusDefine.STATUS_RUNNING ||
                    info.mStatus == UiStatusDefine.STATUS_PAUSED) {
                mDownloadManagerImp.pauseDownload(info.mId);
            }
        }

    }

    public void deleteObjFile(DownloadItemInfo downloadItemInfo) {
        mDownloadManagerImp.deleteObjFile(downloadItemInfo);
    }
}