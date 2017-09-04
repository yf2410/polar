package com.polar.browser.imagebrowse;

import java.io.Serializable;
import java.util.List;

/**
 * Created by duan on 17/3/16.
 */

public class ImageListData implements Serializable{
    public ImageListData(List<ImageInfo> imgs) {
        this.imgs = imgs;
    }

    public List<ImageInfo> imgs;

    public List<ImageInfo> getImgs() {
        return imgs;
    }

    public void setImgs(List<ImageInfo> imgs) {
        this.imgs = imgs;
    }
}
