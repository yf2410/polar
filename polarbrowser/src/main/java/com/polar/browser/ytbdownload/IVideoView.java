package com.polar.browser.ytbdownload;

import android.graphics.Bitmap;
import com.polar.browser.vclibrary.bean.YouTubeVidVo;
import java.util.List;

/**
 * Created by FKQ on 2017/6/28.
 */

public interface IVideoView {

    void showParsingView(int type, String parsMessage);
    void hindParsingView();

    void showVideoBanner(Bitmap bitmap);
    void hindVideoBanner();

    void showVideoDownload();
    void hindVideoDownload();

    void bindVideoData(List<YouTubeVidVo.DownloadInfo> download_links, YouTubeVidVo.VideoInfo info);
}
