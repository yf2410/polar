package com.polar.browser.vclibrary.bean.db;

import android.os.Parcel;
import android.os.Parcelable;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

/**
 * Created by James on 2016/7/12.
 */
@DatabaseTable(tableName = "systemnews")
public class SystemNews implements Parcelable{
	public static final String COLUMN_READ = "read";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_RECEIVED_TIME = "receivedTime";
	@DatabaseField(generatedId = true)
	private long id;
	/**
	 * 已读状态,已读为true
	 */
	@DatabaseField
	private boolean read;
	@DatabaseField
	private String title;
	@DatabaseField
	private String contentURL;
	@DatabaseField
	private Date receivedTime;

	public SystemNews() {
	}

	/**
	 * 构造未读消息
	 *
	 * @param title
	 * @param contentURL
	 * @param receivedTime
	 */
	public SystemNews(String title, String contentURL, Date receivedTime) {
		this(false, title, contentURL, receivedTime);
	}

	public SystemNews(boolean read, String title, String contentURL, Date receivedTime) {
		this.read = read;
		this.title = title;
		this.contentURL = contentURL;
		this.receivedTime = receivedTime;
	}

	public Date getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(Date receivedTime) {
		this.receivedTime = receivedTime;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentURL() {
		return contentURL;
	}

	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SystemNews{");
		sb.append("id=").append(id);
		sb.append(", read=").append(read);
		sb.append(", title='").append(title).append('\'');
		sb.append(", contentURL='").append(contentURL).append('\'');
		sb.append(", receivedTime=").append(receivedTime);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeByte(this.read ? (byte) 1 : (byte) 0);
		dest.writeString(this.title);
		dest.writeString(this.contentURL);
		dest.writeLong(this.receivedTime != null ? this.receivedTime.getTime() : -1);
	}

	protected SystemNews(Parcel in) {
		this.id = in.readLong();
		this.read = in.readByte() != 0;
		this.title = in.readString();
		this.contentURL = in.readString();
		long tmpReceivedTime = in.readLong();
		this.receivedTime = tmpReceivedTime == -1 ? null : new Date(tmpReceivedTime);
	}

	public static final Creator<SystemNews> CREATOR = new Creator<SystemNews>() {
		@Override
		public SystemNews createFromParcel(Parcel source) {
			return new SystemNews(source);
		}

		@Override
		public SystemNews[] newArray(int size) {
			return new SystemNews[size];
		}
	};
}
