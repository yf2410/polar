package com.polar.browser.video.share;

import android.net.Uri;

/**
 * Created by yd_lzk on 2017/1/16.
 */

public class ShareContent {

    public static final int TYPE_SHARE_TEXT = 1;
    public static final int TYPE_SHARE_PICTURE = 2;
    public static final int TYPE_SHARE_MIX = 3; //既有文本又有图片

    private boolean isLocal;
    private String title;
    private String shareUrl;
    private Uri imgUri;
    private String content;  //组合内容
    private int type = TYPE_SHARE_TEXT;

    public ShareContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public ShareContent(String title) {
        this.title = title;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    @Override
    public String toString() {
        return "ShareContent{" +
                "isLocal=" + isLocal +
                ", title='" + title + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                ", imgUri=" + imgUri +
                ", content='" + content + '\'' +
                ", type=" + type +
                '}';
    }
}
