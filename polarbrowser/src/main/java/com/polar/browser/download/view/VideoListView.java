package com.polar.browser.download.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.bean.VideoInfo;
import com.polar.browser.utils.CursorDataParserUtils;

import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;

/**
 * Created by saifei on 17/1/3.
 */

public class VideoListView extends AbstractFileListView<VideoInfo> {
    public VideoListView(Context context) {
        this(context, null);
    }

    public VideoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public String type() {
        return TYPE_VIDEO;
    }



    @Override
    protected void bindView(View view, int position, VideoInfo info, boolean isScrollState) {
        VideoItem videoItem = (VideoItem) view;
//        setOnScrollListener(videoItem);
        videoItem.bind(info,isScrollState);
    }

    @Override
    protected View newView(Context context, VideoInfo data, ViewGroup parent, int type) {
        return new VideoItem(context);
    }

    @Override
    protected List<VideoInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseVideosFromCursor(cursor,queryHandler);
    }


}
