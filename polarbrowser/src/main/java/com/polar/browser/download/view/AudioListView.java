package com.polar.browser.download.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.bean.MusicInfo;
import com.polar.browser.utils.CursorDataParserUtils;

import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;

/**
 * Created by saifei on 17/1/5.
 */

public class AudioListView extends AbstractFileListView<MusicInfo> {

    public AudioListView(Context context) {
        super(context);
    }

    public AudioListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String type() {
        return TYPE_AUDIO;
    }

    @Override
    protected void bindView(View view, int position, MusicInfo data, boolean isScrollState) {
        AudioItem item  = (AudioItem) view;
        item.bind(data);
    }

    @Override
    protected View newView(Context context, MusicInfo data, ViewGroup parent, int type) {
        return new AudioItem(context);
    }

    @Override
    protected List<MusicInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseAudiosFromCursor(cursor, queryHandler);
    }
}
