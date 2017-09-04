package com.polar.browser.bean;

import com.polar.browser.download_refactor.DownloadItemInfo;

import java.io.Serializable;

public class DownloadInfo implements Serializable {

	/**
	 * DownloadListener中
	 * <p/>
	 * String url, String userAgent, String contentDisposition, String mimetype,
	 * long contentLength
	 */

	private static final long serialVersionUID = -7656568547258006330L;

	public String pageUrl;
	public String cookies;
	public String url;
	public String userAgent;
	public String contentDisposition;
	public String mimetype;
	public long contentLength;
	public String fileName;
	public String destination;
	public int status;
	public long currentBytes;
	public long speed;
	public long createDate;
	public long finishDate;
	public boolean isNeedConfirm;

	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getContentDisposition() {
		return contentDisposition;
	}

	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getCurrentBytes() {
		return currentBytes;
	}

	public void setCurrentBytes(long currentBytes) {
		this.currentBytes = currentBytes;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public long getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(long finishDate) {
		this.finishDate = finishDate;
	}

	@Override
	public String toString() {
		return "DownloadInfo{" +
				"pageUrl='" + pageUrl + '\'' +
				", cookies='" + cookies + '\'' +
				", url='" + url + '\'' +
				", userAgent='" + userAgent + '\'' +
				", contentDisposition='" + contentDisposition + '\'' +
				", mimetype='" + mimetype + '\'' +
				", contentLength=" + contentLength +
				", fileName='" + fileName + '\'' +
				", destination='" + destination + '\'' +
				", status=" + status +
				", currentBytes=" + currentBytes +
				", speed=" + speed +
				", createDate=" + createDate +
				", finishDate=" + finishDate +
				'}';
	}

	/**
	 * 转换成DownloadItemInfo对象
	 *
	 * @return
	 */
	public DownloadItemInfo transfer2DB() {
		DownloadItemInfo info = new DownloadItemInfo();
		info.mUrl = url;
		info.mReferer = pageUrl;
		info.mMediaType = mimetype;
		info.mStatus = status;
		info.mFilePath = destination;
		info.mCurrentBytes = currentBytes;
		info.mTotalBytes = contentLength;
		info.mCookie = cookies;
		info.mUserAgent = userAgent;
		info.mFinishDate = finishDate;

		return info;
	}
}
