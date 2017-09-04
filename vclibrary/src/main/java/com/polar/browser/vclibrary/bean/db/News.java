package com.polar.browser.vclibrary.bean.db;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by James on 2016/8/15.
 */
public class News implements Parcelable {
	private String url;
	private String title;

	public News() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.url);
		dest.writeString(this.title);
	}

	protected News(Parcel in) {
		this.url = in.readString();
		this.title = in.readString();
	}

	public static final Creator<News> CREATOR = new Creator<News>() {
		@Override
		public News createFromParcel(Parcel source) {
			return new News(source);
		}

		@Override
		public News[] newArray(int size) {
			return new News[size];
		}
	};
}
