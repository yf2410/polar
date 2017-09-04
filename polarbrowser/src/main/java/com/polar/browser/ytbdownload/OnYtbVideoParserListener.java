package com.polar.browser.ytbdownload;

import com.polar.browser.vclibrary.bean.YouTubeVidVo;

import java.util.List;

/**
 * Created by FKQ on 2017/6/28.
 */

public interface OnYtbVideoParserListener {

    void onParserSuccess(List<YouTubeVidVo.DownloadInfo> download_links, YouTubeVidVo.VideoInfo info);

    void onParserFail(String message);

    void onParserError(Throwable t);
}
