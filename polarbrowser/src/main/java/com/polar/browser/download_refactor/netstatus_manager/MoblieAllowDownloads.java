package com.polar.browser.download_refactor.netstatus_manager;

import java.util.ArrayList;
import java.util.List;

public class MoblieAllowDownloads {
    private static MoblieAllowDownloads sObj; 
    public static MoblieAllowDownloads getInstance(){
        if( sObj == null )
            sObj = new MoblieAllowDownloads();
        return sObj;
    }
    private List <String> mMoblieAllowDownloads = new ArrayList<String>();
    public void addAllowMoblieNetDownload( String url ){
        mMoblieAllowDownloads.add(url);
    }
    
    public boolean isAllowMoblieNetDownload(String url){
        return mMoblieAllowDownloads.contains(url);
    }
    
    public List<String> getMoblieAllowDownloads(){
        List <String> mTempList = new ArrayList<String>(mMoblieAllowDownloads);
        return mTempList;
    }
}
