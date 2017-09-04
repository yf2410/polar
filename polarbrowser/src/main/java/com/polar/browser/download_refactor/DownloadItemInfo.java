package com.polar.browser.download_refactor;

import android.net.Uri;

import com.polar.browser.download_refactor.DownloadTask.TaskState;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class DownloadItemInfo implements Serializable{

    private static final long serialVersionUID = 6562688803376533294L;
    public long mId;
    public String mUrl;
    public String mReferer;
    public String mMediaType;
    public Date mDate;
    public int mStatus;
    public int mReason;
    public String mFilePath;
    public long mCurrentBytes;
    public long mTotalBytes;
    public int mVirusStatus;
    public int mContinuingState;
    public String mCookie;
    public String mUserAgent;
    public long mFinishDate;

    // 0729 是否选中
    public boolean isChecked;
    // 0729 是否是编辑状态
    public boolean isEditing;
    
    public boolean isContuningDownloadSupported() {
        if (mContinuingState == TaskState.CONTINUING_STATE_NEGATIVE)
            return false;
        if (mContinuingState == TaskState.CONTINUING_STATE_POSITIVE
                || mContinuingState == TaskState.CONTINUING_STATE_SPLITED)
            return true;
        // 任务尚未开始，默认为支持断点续传
        if (mCurrentBytes == 0 && mTotalBytes == -1)
            return true;
        // 当ContinuingState为其他状态时，仅根据mTotalBytes判断，特殊情况下这个判断可能不准确
        return mTotalBytes > 0;
    }

    public String getFilename() {
        return new File(mFilePath).getName();
    }

    public String getFileUri() {
        return Uri.fromFile(new File(mFilePath)).toString();
    }
    
    public boolean isProgressValid() {
        return mTotalBytes > 0;
    }
    public float getProgress() {
        if (mTotalBytes <= 0) {
            return 0.f;
        }
        if (mCurrentBytes <= 0)
            return 0.f;
        return (float)((double)mCurrentBytes / mTotalBytes);
    }
    public float getProgress100() {
        return getProgress() * 100;
    }

    @Override
    public String toString() {
        return "DownloadItemInfo{" +
                "mId=" + mId +
                ", mUrl='" + mUrl + '\'' +
                ", mReferer='" + mReferer + '\'' +
                ", mMediaType='" + mMediaType + '\'' +
                ", mDate=" + mDate +
                ", mStatus=" + mStatus +
                ", mReason=" + mReason +
                ", mFilePath='" + mFilePath + '\'' +
                ", mCurrentBytes=" + mCurrentBytes +
                ", mTotalBytes=" + mTotalBytes +
                ", mVirusStatus=" + mVirusStatus +
                ", mContinuingState=" + mContinuingState +
                ", mCookie='" + mCookie + '\'' +
                ", mUserAgent='" + mUserAgent + '\'' +
                ", mFinishDate=" + mFinishDate +
                ", isChecked=" + isChecked +
                ", isEditing=" + isEditing +
                '}';
    }
}