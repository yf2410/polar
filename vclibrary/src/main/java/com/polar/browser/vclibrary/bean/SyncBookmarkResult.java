package com.polar.browser.vclibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by saifei on 17/4/5.
 * 同步或者上传书签结果 bean
 */

public class SyncBookmarkResult implements Parcelable {


    private String timeStamp;
    private String url;
    private String uId;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuId() {
        return uId;
    }

    public SyncBookmarkResult() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.timeStamp);
        dest.writeString(this.url);
        dest.writeString(this.uId);
    }

    protected SyncBookmarkResult(Parcel in) {
        this.timeStamp = in.readString();
        this.url = in.readString();
        this.uId = in.readString();
    }

    public static final Creator<SyncBookmarkResult> CREATOR = new Creator<SyncBookmarkResult>() {
        @Override
        public SyncBookmarkResult createFromParcel(Parcel source) {
            return new SyncBookmarkResult(source);
        }

        @Override
        public SyncBookmarkResult[] newArray(int size) {
            return new SyncBookmarkResult[size];
        }
    };

    @Override
    public String toString() {
        return "SyncBookmarkResult{" +
                "timeStamp='" + timeStamp + '\'' +
                ", url='" + url + '\'' +
                ", uId='" + uId + '\'' +
                '}';
    }
}
