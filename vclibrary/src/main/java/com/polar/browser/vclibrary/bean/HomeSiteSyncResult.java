package com.polar.browser.vclibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yangfan on 2017/5/24.
 */

public class HomeSiteSyncResult implements Parcelable {
    public String timeStamp;
    public String url;
    public String uId;

    public HomeSiteSyncResult() {

    }

    protected HomeSiteSyncResult(Parcel in) {
        this.timeStamp = in.readString();
        this.url = in.readString();
        this.uId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SettingSyncResult> CREATOR = new Creator<SettingSyncResult>() {
        @Override
        public SettingSyncResult createFromParcel(Parcel source) {
            return new SettingSyncResult(source);
        }

        @Override
        public SettingSyncResult[] newArray(int size) {
            return new SettingSyncResult[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.timeStamp);
        dest.writeString(this.url);
        dest.writeString(this.uId);
    }

    @Override
    public String toString() {
        return "HomeSiteSyncResult{" +
                "timeStamp='" + timeStamp + '\'' +
                ", url='" + url + '\'' +
                ", uId='" + uId + '\'' +
                '}';
    }
}