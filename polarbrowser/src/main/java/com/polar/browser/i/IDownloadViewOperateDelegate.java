package com.polar.browser.i;

import android.content.ContentValues;
import android.net.Uri;

/**
 * Created by duan on 16/9/27.
 */
public interface IDownloadViewOperateDelegate {

    void edit();
    void complete();
    void clear();
    void share();
    void delete();
    void more();

    interface IEditCallback extends IDownloadViewOperateDelegate{
        void onExitEditMode();
        void onIntoEditMode();
        void setAllChecked(boolean allChecked);
        int getCheckedCount();
        int getDataCount();
        void onFilePathChanged(int token, Object cookie, Uri uri,
                               ContentValues values, String selection, String[] selectionArgs);
        void onFileCountChanged();
    }



}
