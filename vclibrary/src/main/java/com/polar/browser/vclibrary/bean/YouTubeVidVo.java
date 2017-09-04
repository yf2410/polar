package com.polar.browser.vclibrary.bean;

import java.util.List;

/**
 * Created by FKQ on 2017/6/28.
 */

public class YouTubeVidVo {

    private String error;
    private VideoInfo info;
    private List<DownloadInfo> download_links;

    public YouTubeVidVo() {
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public VideoInfo getInfo() {
        return info;
    }

    public void setInfo(VideoInfo info) {
        this.info = info;
    }

    public List<DownloadInfo> getDownload_links() {
        return download_links;
    }

    public void setDownload_links(List<DownloadInfo> download_links) {
        this.download_links = download_links;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("YouTubeVidVo{");
        sb.append("error='").append(error).append('\'');
        sb.append(", info=").append(info);
        sb.append(", download_links=").append(download_links);
        sb.append('}');
        return sb.toString();
    }

    public class VideoInfo {
        private String title;
        private String image;
        private String url;
        private String domain;
        private String user;
        private String duration;

        public VideoInfo() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }
    }

    public class DownloadInfo {
        private String url;
        private String title;
        private String type;
        private String quality;
        private String size;

        public DownloadInfo() {
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }
}
